package com.sankalp.portfolioservice.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sankalp.portfolioservice.dto.AddPositionRequest;
import com.sankalp.portfolioservice.dto.CreatePortfolioRequest;
import com.sankalp.portfolioservice.dto.PortfolioTradeRequest;
import com.sankalp.portfolioservice.dto.PortfolioPositionResponse;
import com.sankalp.portfolioservice.dto.PortfolioResponse;
import com.sankalp.portfolioservice.service.PortfolioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolios")
@Validated
public class PortfolioController {

	private final PortfolioService portfolioService;

	public PortfolioController(PortfolioService portfolioService) {
		this.portfolioService = portfolioService;
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("service", "portfolio-service", "status", "UP"));
	}

	@PostMapping
	public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
		return ResponseEntity.ok(portfolioService.createPortfolio(request));
	}

	@GetMapping
	public ResponseEntity<List<PortfolioResponse>> listPortfolios(@RequestParam Integer userId) {
		return ResponseEntity.ok(portfolioService.getPortfoliosByUser(userId));
	}

	@GetMapping("/{portfolioId}")
	public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Integer portfolioId) {
		return ResponseEntity.ok(portfolioService.getPortfolio(portfolioId));
	}

	@PostMapping("/{portfolioId}/positions")
	public ResponseEntity<PortfolioPositionResponse> addPosition(
			@PathVariable Integer portfolioId,
			@Valid @RequestBody AddPositionRequest request) {
		return ResponseEntity.ok(portfolioService.addPosition(portfolioId, request));
	}

	@GetMapping("/{portfolioId}/positions")
	public ResponseEntity<List<PortfolioPositionResponse>> getPositions(@PathVariable Integer portfolioId) {
		return ResponseEntity.ok(portfolioService.getPositions(portfolioId));
	}

	@PostMapping("/{portfolioId}/hooks/trade")
	public ResponseEntity<PortfolioPositionResponse> applyTrade(
			@PathVariable Integer portfolioId,
			@Valid @RequestBody PortfolioTradeRequest request) {
		return ResponseEntity.ok(portfolioService.applyTrade(portfolioId, request));
	}
}
