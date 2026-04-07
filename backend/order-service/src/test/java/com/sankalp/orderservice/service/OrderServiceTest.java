package com.sankalp.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.sankalp.orderservice.dto.CreateOrderRequest;
import com.sankalp.orderservice.dto.MarketPriceResponse;
import com.sankalp.orderservice.entity.OrderSide;
import com.sankalp.orderservice.entity.OrderStatus;
import com.sankalp.orderservice.entity.OrderType;
import com.sankalp.orderservice.entity.StockOrder;
import com.sankalp.orderservice.entity.StockOrderExecution;
import com.sankalp.orderservice.repository.OrderOutboxEventRepository;
import com.sankalp.orderservice.repository.StockOrderExecutionRepository;
import com.sankalp.orderservice.repository.StockOrderRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private StockOrderRepository stockOrderRepository;

	@Mock
	private StockOrderExecutionRepository stockOrderExecutionRepository;

	@Mock
	private OrderOutboxEventRepository outboxEventRepository;

	@Mock
	private RestTemplate restTemplate;

	private OrderService orderService;

	@BeforeEach
	void setUp() {
		orderService = new OrderService(
				stockOrderRepository,
				stockOrderExecutionRepository,
				outboxEventRepository,
				new ObjectMapper(),
				restTemplate,
				"http://localhost:8089/api/market",
				"http://localhost:8088/api/wallets",
				"http://localhost:8087/api/portfolios");

		Mockito.lenient().when(stockOrderRepository.save(any(StockOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
		Mockito.lenient().when(stockOrderExecutionRepository.save(any(StockOrderExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));
		Mockito.lenient().when(outboxEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		Mockito.lenient().when(restTemplate.postForEntity(anyString(), any(), eq(Object.class))).thenReturn(ResponseEntity.ok(new Object()));
		Mockito.lenient().when(restTemplate.getForEntity(contains("/status"), eq(Map.class)))
				.thenReturn(ResponseEntity.ok(Map.of("marketOpen", true)));
	}

	@Test
	void cancelOrder_releasesReservedFundsAndMarksCancelled() {
		StockOrder order = new StockOrder();
		ReflectionTestUtils.setField(order, "id", 11);
		order.setUserId(101);
		order.setPortfolioId(55);
		order.setStockId(7);
		order.setQuantity(4);
		order.setOrderType(OrderType.LIMIT);
		order.setSide(OrderSide.BUY);
		order.setStatus(OrderStatus.RESERVED);
		order.setReservedAmount(new BigDecimal("1200.00"));
		order.setIdempotencyKey("BUY-CANCEL-11");

		when(stockOrderRepository.findById(11)).thenReturn(java.util.Optional.of(order));
		when(restTemplate.getForEntity(contains("/prices/live"), eq(MarketPriceResponse[].class)))
				.thenReturn(ResponseEntity.ok(new MarketPriceResponse[0]));

		var response = orderService.cancelOrder(11);

		assertEquals(OrderStatus.CANCELLED, response.status());
		assertEquals(BigDecimal.ZERO.setScale(2), response.reservedAmount().setScale(2));
		assertEquals("BUY-CANCEL-11", response.idempotencyKey());
	}

	@Test
	void evaluatePendingOrders_executesTriggeredSellLimitOrder() {
		StockOrder order = new StockOrder();
		ReflectionTestUtils.setField(order, "id", 22);
		order.setUserId(202);
		order.setPortfolioId(77);
		order.setStockId(99);
		order.setQuantity(3);
		order.setOrderType(OrderType.LIMIT);
		order.setSide(OrderSide.SELL);
		order.setStatus(OrderStatus.TRIGGER_PENDING);
		order.setOrderPrice(new BigDecimal("100.00"));
		order.setIdempotencyKey("SELL-LIMIT-22");

		when(stockOrderRepository.findByStatusInOrderByCreatedAtAsc(any()))
				.thenReturn(List.of(order));
		when(stockOrderRepository.findById(22)).thenReturn(java.util.Optional.of(order));
		when(restTemplate.getForEntity(contains("/prices/live"), eq(MarketPriceResponse[].class)))
				.thenReturn(ResponseEntity.ok(new MarketPriceResponse[] {
					new MarketPriceResponse(99, "TEST", 1, 120.0, 100.0, 20.0, Instant.now())
				}));

		int executed = orderService.evaluatePendingOrders();

		assertEquals(1, executed);
		assertEquals(OrderStatus.EXECUTED, order.getStatus());
		assertNotNull(order.getExecutedAt());
		assertEquals(new BigDecimal("120.00").setScale(2), order.getExecutedPrice().setScale(2));
	}

	@Test
	void createOrder_requiresTrailingStopAmount() {
		CreateOrderRequest request = new CreateOrderRequest();
		request.setUserId(1);
		request.setPortfolioId(2);
		request.setStockId(3);
		request.setQuantity(1);
		request.setOrderType(OrderType.TRAILING_STOP);
		request.setSide(OrderSide.SELL);
		request.setIdempotencyKey("REQ-TS-1");

		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
	}
}
