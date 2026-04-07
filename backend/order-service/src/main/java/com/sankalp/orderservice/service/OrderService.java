package com.sankalp.orderservice.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.sankalp.orderservice.dto.CreateOrderRequest;
import com.sankalp.orderservice.dto.ExecuteOrderRequest;
import com.sankalp.orderservice.dto.MarketPriceResponse;
import com.sankalp.orderservice.dto.OrderExecutionResponse;
import com.sankalp.orderservice.dto.OrderResponse;
import com.sankalp.orderservice.entity.OrderEventType;
import com.sankalp.orderservice.entity.OrderSide;
import com.sankalp.orderservice.entity.OrderStatus;
import com.sankalp.orderservice.entity.OrderType;
import com.sankalp.orderservice.entity.StockOrder;
import com.sankalp.orderservice.entity.StockOrderExecution;
import com.sankalp.orderservice.repository.StockOrderExecutionRepository;
import com.sankalp.orderservice.repository.StockOrderRepository;

@Service
public class OrderService {

	private final StockOrderRepository stockOrderRepository;
	private final StockOrderExecutionRepository stockOrderExecutionRepository;
	private final RestTemplate restTemplate;
	private final String marketBaseUrl;
	private final String walletBaseUrl;
	private final String portfolioBaseUrl;

	public OrderService(StockOrderRepository stockOrderRepository,
			StockOrderExecutionRepository stockOrderExecutionRepository,
			RestTemplate restTemplate,
			@Value("${hooks.market.base-url:http://localhost:8089/api/market}") String marketBaseUrl,
			@Value("${hooks.wallet.base-url:http://localhost:8088/api/wallets}") String walletBaseUrl,
			@Value("${hooks.portfolio.base-url:http://localhost:8087/api/portfolios}") String portfolioBaseUrl) {
		this.stockOrderRepository = stockOrderRepository;
		this.stockOrderExecutionRepository = stockOrderExecutionRepository;
		this.restTemplate = restTemplate;
		this.marketBaseUrl = marketBaseUrl;
		this.walletBaseUrl = walletBaseUrl;
		this.portfolioBaseUrl = portfolioBaseUrl;
	}

	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		validateOrderRequest(request);

		StockOrder order = new StockOrder();
		order.setUserId(request.getUserId());
		order.setPortfolioId(request.getPortfolioId());
		order.setStockId(request.getStockId());
		order.setQuantity(request.getQuantity());
		order.setOrderType(request.getOrderType());
		order.setSide(request.getSide());
		order.setOrderPrice(request.getOrderPrice());
		order.setTriggerPrice(request.getTriggerPrice());
		order.setTrailAmount(request.getTrailAmount());
		boolean marketOpen = isMarketOpen();
		if (request.getOrderType() == OrderType.MARKET && !marketOpen) {
			order.setStatus(OrderStatus.OPEN);
		} else {
			order.setStatus(isTriggerOrder(request.getOrderType()) ? OrderStatus.TRIGGER_PENDING : OrderStatus.CREATED);
		}

		BigDecimal marketPrice = currentMarketPrice(request.getStockId()).orElse(null);
		if (request.getOrderType() == OrderType.TRAILING_STOP && marketPrice != null) {
			order.setReferencePrice(marketPrice);
		}

		StockOrder saved = stockOrderRepository.save(order);
		recordEvent(saved, OrderEventType.CREATED, marketPrice, saved.getReferencePrice(), "Order created");

		if (request.getSide() == OrderSide.BUY) {
			BigDecimal reserveAmount = resolveReserveAmount(request, marketPrice);
			walletHook("/hooks/order/reserve", request.getUserId(), reserveAmount, refFor(saved), "Reserve for buy order");
			saved.setReservedAmount(reserveAmount);
			saved = stockOrderRepository.save(saved);
		}

		if (marketOpen && shouldExecuteImmediately(saved, marketPrice)) {
			return executeOrderInternal(saved.getId(), marketPrice, "MARKET_OR_TRIGGERED_ON_CREATE");
		}

		return toResponse(saved);
	}

	@Transactional
	public OrderResponse executeOrder(Integer orderId, ExecuteOrderRequest request) {
		return executeOrderInternal(orderId, request.getExecutionPrice(), "MANUAL_EXECUTION");
	}

	@Transactional
	public OrderResponse cancelOrder(Integer orderId) {
		StockOrder order = stockOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

		if (isFinalized(order.getStatus())) {
			throw new IllegalArgumentException("Order already finalized");
		}

		if (order.getSide() == OrderSide.BUY && order.getReservedAmount() != null && order.getReservedAmount().compareTo(BigDecimal.ZERO) > 0) {
			walletHook("/hooks/order/release", order.getUserId(), order.getReservedAmount(), refFor(order), "Release reservation on cancel");
			order.setReservedAmount(BigDecimal.ZERO);
		}

		order.setStatus(OrderStatus.CANCELLED);
		StockOrder saved = stockOrderRepository.save(order);
		recordEvent(saved, OrderEventType.CANCELLED, currentMarketPrice(saved.getStockId()).orElse(null), saved.getReferencePrice(), "Order cancelled");
		return toResponse(saved);
	}

	@Transactional
	public int evaluatePendingOrders() {
		if (!isMarketOpen()) {
			return 0;
		}

		Map<Integer, BigDecimal> prices = currentMarketPrices();
		int executed = 0;
		for (StockOrder order : stockOrderRepository.findByStatusInOrderByCreatedAtAsc(List.of(
				OrderStatus.CREATED,
				OrderStatus.OPEN,
				OrderStatus.RESERVED,
				OrderStatus.TRIGGER_PENDING))) {
			BigDecimal currentPrice = prices.get(order.getStockId());
			if (currentPrice == null) {
				continue;
			}

			recordEvent(order, OrderEventType.EVALUATED, currentPrice, order.getReferencePrice(), "Periodic trigger evaluation");
			if (shouldExecute(order, currentPrice)) {
				recordEvent(order, OrderEventType.TRIGGERED, currentPrice, order.getReferencePrice(), "Execution trigger met");
				executeOrderInternal(order.getId(), currentPrice, "SCHEDULED_TRIGGER");
				executed++;
			}
		}
		return executed;
	}

	@Scheduled(fixedDelayString = "${order.evaluator.fixed-delay-ms:5000}")
	public void scheduledEvaluation() {
		evaluatePendingOrders();
	}

	@Transactional(readOnly = true)
	public List<OrderExecutionResponse> getExecutionHistory(Integer orderId) {
		return stockOrderExecutionRepository.findByOrderIdOrderByEventAtDesc(orderId).stream()
				.map(this::toExecutionResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(Integer orderId) {
		return toResponse(stockOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId)));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> listOrders(Integer userId) {
		return stockOrderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(this::toResponse)
				.toList();
	}

	private OrderResponse executeOrderInternal(Integer orderId, BigDecimal executionPrice, String source) {
		StockOrder order = stockOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

		if (isFinalized(order.getStatus())) {
			throw new IllegalArgumentException("Order already finalized");
		}

		validateExecution(order, executionPrice);
		order.setStatus(OrderStatus.EXECUTING);
		stockOrderRepository.save(order);

		try {
			if (order.getSide() == OrderSide.BUY) {
				handleBuyExecution(order, executionPrice);
			} else {
				handleSellExecution(order, executionPrice);
			}

			order.setExecutedPrice(executionPrice);
			order.setExecutedAt(new Timestamp(System.currentTimeMillis()));
			order.setStatus(OrderStatus.EXECUTED);
			StockOrder saved = stockOrderRepository.save(order);
			recordEvent(saved, OrderEventType.EXECUTED, executionPrice, saved.getReferencePrice(), source);
			return toResponse(saved);
		} catch (RuntimeException ex) {
			order.setStatus(OrderStatus.REJECTED);
			StockOrder saved = stockOrderRepository.save(order);
			recordEvent(saved, OrderEventType.REJECTED, executionPrice, saved.getReferencePrice(), ex.getMessage());
			throw ex;
		}
	}

	private void handleBuyExecution(StockOrder order, BigDecimal executionPrice) {
		BigDecimal debitAmount = executionPrice.multiply(BigDecimal.valueOf(order.getQuantity()));
		walletHook("/hooks/order/debit", order.getUserId(), debitAmount, refFor(order), "Debit reserved for executed buy");

		if (order.getReservedAmount() != null && order.getReservedAmount().compareTo(debitAmount) > 0) {
			BigDecimal release = order.getReservedAmount().subtract(debitAmount);
			walletHook("/hooks/order/release", order.getUserId(), release, refFor(order), "Release unspent reserve");
		}

		portfolioTradeHook(order, executionPrice, "BUY");
	}

	private void handleSellExecution(StockOrder order, BigDecimal executionPrice) {
		portfolioTradeHook(order, executionPrice, "SELL");

		BigDecimal credit = executionPrice.multiply(BigDecimal.valueOf(order.getQuantity()));
		walletHook("/hooks/order/credit", order.getUserId(), credit, refFor(order), "Credit proceeds for sell order");
	}

	private void validateOrderRequest(CreateOrderRequest request) {
		if (request.getOrderType() == OrderType.TRAILING_STOP && request.getTrailAmount() == null) {
			throw new IllegalArgumentException("trailAmount is required for TRAILING_STOP orders");
		}
		if (request.getOrderType() == OrderType.STOP_LOSS && request.getTriggerPrice() == null) {
			throw new IllegalArgumentException("triggerPrice is required for STOP_LOSS orders");
		}
		if ((request.getOrderType() == OrderType.LIMIT || request.getOrderType() == OrderType.TAKE_PROFIT)
				&& request.getOrderPrice() == null) {
			throw new IllegalArgumentException("orderPrice is required for LIMIT and TAKE_PROFIT orders");
		}
	}

	private void validateExecution(StockOrder order, BigDecimal executionPrice) {
		if (order.getOrderType() == OrderType.MARKET) {
			return;
		}

		validateTriggerAndLimit(order, executionPrice);
	}

	private void validateTriggerAndLimit(StockOrder order, BigDecimal executionPrice) {
		if (order.getOrderType() == OrderType.LIMIT && order.getOrderPrice() != null) {
			boolean limitCondition = order.getSide() == OrderSide.BUY
					? executionPrice.compareTo(order.getOrderPrice()) <= 0
					: executionPrice.compareTo(order.getOrderPrice()) >= 0;
			if (!limitCondition) {
				throw new IllegalArgumentException("Limit condition not met for execution");
			}
		}

		if (order.getOrderType() == OrderType.TAKE_PROFIT && order.getOrderPrice() != null) {
			boolean takeProfitCondition = order.getSide() == OrderSide.SELL
					? executionPrice.compareTo(order.getOrderPrice()) >= 0
					: executionPrice.compareTo(order.getOrderPrice()) <= 0;
			if (!takeProfitCondition) {
				throw new IllegalArgumentException("Take-profit condition not met for execution");
			}
		}

		if (order.getOrderType() == OrderType.STOP_LOSS) {
			if (order.getTriggerPrice() == null) {
				throw new IllegalArgumentException("STOP_LOSS order missing triggerPrice");
			}
			boolean triggered = order.getSide() == OrderSide.SELL
					? executionPrice.compareTo(order.getTriggerPrice()) <= 0
					: executionPrice.compareTo(order.getTriggerPrice()) >= 0;
			if (!triggered) {
				throw new IllegalArgumentException("Stop-loss trigger not met");
			}
		}

		if (order.getOrderType() == OrderType.TRAILING_STOP) {
			if (order.getTrailAmount() == null) {
				throw new IllegalArgumentException("TRAILING_STOP order missing trailAmount");
			}
			BigDecimal reference = order.getReferencePrice() == null ? executionPrice : order.getReferencePrice();
			if (!isTrailingStopTriggered(order.getSide(), reference, executionPrice, order.getTrailAmount())) {
				throw new IllegalArgumentException("Trailing stop trigger not met");
			}
		}
	}

	private boolean shouldExecuteImmediately(StockOrder order, BigDecimal marketPrice) {
		if (marketPrice == null) {
			return false;
		}

		return switch (order.getOrderType()) {
			case MARKET -> true;
			case LIMIT, TAKE_PROFIT, STOP_LOSS, TRAILING_STOP -> shouldExecute(order, marketPrice);
		};
	}

	private boolean shouldExecute(StockOrder order, BigDecimal currentPrice) {
		return switch (order.getOrderType()) {
			case MARKET -> true;
			case LIMIT -> order.getOrderPrice() != null && (order.getSide() == OrderSide.BUY
					? currentPrice.compareTo(order.getOrderPrice()) <= 0
					: currentPrice.compareTo(order.getOrderPrice()) >= 0);
			case STOP_LOSS -> order.getTriggerPrice() != null && (order.getSide() == OrderSide.SELL
					? currentPrice.compareTo(order.getTriggerPrice()) <= 0
					: currentPrice.compareTo(order.getTriggerPrice()) >= 0);
			case TAKE_PROFIT -> order.getOrderPrice() != null && (order.getSide() == OrderSide.SELL
					? currentPrice.compareTo(order.getOrderPrice()) >= 0
					: currentPrice.compareTo(order.getOrderPrice()) <= 0);
			case TRAILING_STOP -> {
				BigDecimal reference = normalizeReference(order, currentPrice);
				yield isTrailingStopTriggered(order.getSide(), reference, currentPrice, order.getTrailAmount());
			}
		};
	}

	private boolean isTrailingStopTriggered(OrderSide side, BigDecimal referencePrice, BigDecimal currentPrice, BigDecimal trailAmount) {
		if (trailAmount == null) {
			return false;
		}
		if (side == OrderSide.SELL) {
			BigDecimal peak = referencePrice.max(currentPrice);
			return currentPrice.compareTo(peak.subtract(trailAmount)) <= 0;
		}
		BigDecimal trough = referencePrice.min(currentPrice);
		return currentPrice.compareTo(trough.add(trailAmount)) >= 0;
	}

	private BigDecimal normalizeReference(StockOrder order, BigDecimal currentPrice) {
		BigDecimal existing = order.getReferencePrice() == null ? currentPrice : order.getReferencePrice();
		BigDecimal updated = order.getSide() == OrderSide.SELL ? existing.max(currentPrice) : existing.min(currentPrice);
		if (order.getReferencePrice() == null || updated.compareTo(order.getReferencePrice()) != 0) {
			order.setReferencePrice(updated);
			stockOrderRepository.save(order);
		}
		return updated;
	}

	private BigDecimal resolveReserveAmount(CreateOrderRequest request, BigDecimal marketPrice) {
		if (request.getSide() != OrderSide.BUY) {
			return BigDecimal.ZERO;
		}

		BigDecimal reserveBase = switch (request.getOrderType()) {
			case LIMIT, TAKE_PROFIT -> Optional.ofNullable(request.getOrderPrice()).orElseGet(() -> defaultPrice(marketPrice));
			case STOP_LOSS -> Optional.ofNullable(request.getTriggerPrice()).orElseGet(() -> defaultPrice(marketPrice));
			case TRAILING_STOP, MARKET -> defaultPrice(marketPrice);
		};

		return reserveBase.multiply(BigDecimal.valueOf(request.getQuantity()));
	}

	private BigDecimal defaultPrice(BigDecimal marketPrice) {
		if (marketPrice == null) {
			throw new IllegalArgumentException("Live market price is unavailable for buy reservation");
		}
		return marketPrice;
	}

	private Optional<BigDecimal> currentMarketPrice(Integer stockId) {
		return currentMarketPrices().entrySet().stream()
				.filter(entry -> entry.getKey().equals(stockId))
				.map(Map.Entry::getValue)
				.findFirst();
	}

	private Map<Integer, BigDecimal> currentMarketPrices() {
		ResponseEntity<MarketPriceResponse[]> response = restTemplate.getForEntity(
				marketBaseUrl + "/prices/live",
				MarketPriceResponse[].class);

		MarketPriceResponse[] body = response.getBody();
		if (body == null) {
			return Map.of();
		}

		return Arrays.stream(body)
				.collect(Collectors.toMap(
						MarketPriceResponse::stockId,
						price -> BigDecimal.valueOf(price.currentPrice()),
						(existing, replacement) -> replacement));
	}

	private boolean isMarketOpen() {
		try {
			ResponseEntity<Map> response = restTemplate.getForEntity(marketBaseUrl + "/status", Map.class);
			Object marketOpen = response.getBody() == null ? null : response.getBody().get("marketOpen");
			return Boolean.TRUE.equals(marketOpen);
		} catch (RuntimeException ex) {
			return true;
		}
	}

	private void walletHook(String path, Integer userId, BigDecimal amount, String ref, String description) {
		Map<String, Object> body = Map.of(
				"userId", userId,
				"amount", amount,
				"orderRef", ref,
				"description", description);
		restTemplate.postForEntity(walletBaseUrl + path, body, Object.class);
	}

	private void portfolioTradeHook(StockOrder order, BigDecimal executionPrice, String side) {
		Map<String, Object> body = Map.of(
				"stockId", order.getStockId(),
				"quantity", order.getQuantity(),
				"executionPrice", executionPrice,
				"side", side,
				"orderRef", refFor(order));

		restTemplate.postForEntity(
				portfolioBaseUrl + "/" + order.getPortfolioId() + "/hooks/trade",
				body,
				Object.class);
	}

	private boolean isTriggerOrder(OrderType orderType) {
		return orderType == OrderType.LIMIT
				|| orderType == OrderType.STOP_LOSS
				|| orderType == OrderType.TAKE_PROFIT
				|| orderType == OrderType.TRAILING_STOP;
	}

	private boolean isFinalized(OrderStatus status) {
		return status == OrderStatus.EXECUTED || status == OrderStatus.CANCELLED || status == OrderStatus.REJECTED;
	}

	private String refFor(StockOrder order) {
		return "ORD-" + (order.getId() == null ? "NEW" : order.getId());
	}

	private OrderResponse toResponse(StockOrder order) {
		return new OrderResponse(
				order.getId(),
				order.getUserId(),
				order.getPortfolioId(),
				order.getStockId(),
				order.getQuantity(),
				order.getOrderType(),
				order.getSide(),
				order.getStatus(),
				order.getOrderPrice(),
				order.getTriggerPrice(),
				order.getTrailAmount(),
				order.getReferencePrice(),
				order.getReservedAmount(),
				order.getExecutedPrice(),
				order.getCreatedAt(),
				order.getExecutedAt());
	}

	private OrderExecutionResponse toExecutionResponse(StockOrderExecution execution) {
		return new OrderExecutionResponse(
				execution.getId(),
				execution.getEventType(),
				execution.getMarketPrice(),
				execution.getReferencePrice(),
				execution.getNote(),
				execution.getEventAt());
	}

	private void recordEvent(StockOrder order, OrderEventType eventType, BigDecimal marketPrice, BigDecimal referencePrice, String note) {
		StockOrderExecution execution = new StockOrderExecution();
		execution.setOrder(order);
		execution.setEventType(eventType);
		execution.setMarketPrice(marketPrice);
		execution.setReferencePrice(referencePrice);
		execution.setNote(note == null || note.isBlank() ? eventType.name() : note);
		stockOrderExecutionRepository.save(execution);
	}
}
