package com.sankalp.orderservice.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.sankalp.orderservice.dto.CreateOrderRequest;
import com.sankalp.orderservice.dto.ExecuteOrderRequest;
import com.sankalp.orderservice.dto.OrderResponse;
import com.sankalp.orderservice.entity.OrderSide;
import com.sankalp.orderservice.entity.OrderStatus;
import com.sankalp.orderservice.entity.OrderType;
import com.sankalp.orderservice.entity.StockOrder;
import com.sankalp.orderservice.repository.StockOrderRepository;

@Service
public class OrderService {

	private final StockOrderRepository stockOrderRepository;
	private final RestTemplate restTemplate;
	private final String walletBaseUrl;
	private final String portfolioBaseUrl;

	public OrderService(StockOrderRepository stockOrderRepository,
			RestTemplate restTemplate,
			@Value("${hooks.wallet.base-url:http://localhost:8088/api/wallets}") String walletBaseUrl,
			@Value("${hooks.portfolio.base-url:http://localhost:8087/api/portfolios}") String portfolioBaseUrl) {
		this.stockOrderRepository = stockOrderRepository;
		this.restTemplate = restTemplate;
		this.walletBaseUrl = walletBaseUrl;
		this.portfolioBaseUrl = portfolioBaseUrl;
	}

	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		StockOrder order = new StockOrder();
		order.setUserId(request.getUserId());
		order.setPortfolioId(request.getPortfolioId());
		order.setStockId(request.getStockId());
		order.setQuantity(request.getQuantity());
		order.setOrderType(request.getOrderType());
		order.setSide(request.getSide());
		order.setOrderPrice(request.getOrderPrice());
		order.setTriggerPrice(request.getTriggerPrice());
		order.setStatus(OrderStatus.CREATED);

		if (request.getOrderType() == OrderType.STOP_LOSS) {
			if (request.getTriggerPrice() == null) {
				throw new IllegalArgumentException("triggerPrice is required for STOP_LOSS orders");
			}
			order.setStatus(OrderStatus.TRIGGER_PENDING);
		}

		if (request.getSide() == OrderSide.BUY) {
			BigDecimal reserveAmount = resolveReserveAmount(request);
			walletHook("/hooks/order/reserve", request.getUserId(), reserveAmount, refFor(order), "Reserve for buy order");
			order.setReservedAmount(reserveAmount);
			order.setStatus(OrderStatus.RESERVED);
		}

		StockOrder saved = stockOrderRepository.save(order);
		return toResponse(saved);
	}

	@Transactional
	public OrderResponse executeOrder(Integer orderId, ExecuteOrderRequest request) {
		StockOrder order = stockOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

		if (order.getStatus() == OrderStatus.EXECUTED || order.getStatus() == OrderStatus.CANCELLED) {
			throw new IllegalArgumentException("Order already finalized");
		}

		BigDecimal executionPrice = request.getExecutionPrice();
		validateTriggerAndLimit(order, executionPrice);

		try {
			if (order.getSide() == OrderSide.BUY) {
				handleBuyExecution(order, executionPrice);
			} else {
				handleSellExecution(order, executionPrice);
			}

			order.setExecutedPrice(executionPrice);
			order.setExecutedAt(new Timestamp(System.currentTimeMillis()));
			order.setStatus(OrderStatus.EXECUTED);
			return toResponse(stockOrderRepository.save(order));
		} catch (RuntimeException ex) {
			order.setStatus(OrderStatus.REJECTED);
			stockOrderRepository.save(order);
			throw ex;
		}
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

	private void validateTriggerAndLimit(StockOrder order, BigDecimal executionPrice) {
		if (order.getOrderType() == OrderType.LIMIT && order.getOrderPrice() != null) {
			boolean limitCondition = (order.getSide() == OrderSide.BUY)
					? executionPrice.compareTo(order.getOrderPrice()) <= 0
					: executionPrice.compareTo(order.getOrderPrice()) >= 0;
			if (!limitCondition) {
				throw new IllegalArgumentException("Limit condition not met for execution");
			}
		}

		if (order.getOrderType() == OrderType.STOP_LOSS) {
			if (order.getTriggerPrice() == null) {
				throw new IllegalArgumentException("STOP_LOSS order missing triggerPrice");
			}
			boolean triggered = (order.getSide() == OrderSide.SELL)
					? executionPrice.compareTo(order.getTriggerPrice()) <= 0
					: executionPrice.compareTo(order.getTriggerPrice()) >= 0;
			if (!triggered) {
				throw new IllegalArgumentException("Stop-loss trigger not met");
			}
		}
	}

	private BigDecimal resolveReserveAmount(CreateOrderRequest request) {
		if (request.getOrderPrice() == null) {
			throw new IllegalArgumentException("orderPrice is required for BUY reservation");
		}
		return request.getOrderPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
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
				order.getReservedAmount(),
				order.getExecutedPrice(),
				order.getCreatedAt(),
				order.getExecutedAt());
	}
}
