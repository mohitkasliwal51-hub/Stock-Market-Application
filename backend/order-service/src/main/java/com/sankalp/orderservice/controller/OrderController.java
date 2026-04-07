package com.sankalp.orderservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sankalp.orderservice.dto.CreateOrderRequest;
import com.sankalp.orderservice.dto.ExecuteOrderRequest;
import com.sankalp.orderservice.dto.OrderResponse;
import com.sankalp.orderservice.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("service", "order-service", "status", "UP"));
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		return ResponseEntity.ok(orderService.createOrder(request));
	}

	@PostMapping("/{orderId}/execute")
	public ResponseEntity<OrderResponse> executeOrder(
			@PathVariable Integer orderId,
			@Valid @RequestBody ExecuteOrderRequest request) {
		return ResponseEntity.ok(orderService.executeOrder(orderId, request));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable Integer orderId) {
		return ResponseEntity.ok(orderService.getOrder(orderId));
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> listOrders(@RequestParam Integer userId) {
		return ResponseEntity.ok(orderService.listOrders(userId));
	}
}
