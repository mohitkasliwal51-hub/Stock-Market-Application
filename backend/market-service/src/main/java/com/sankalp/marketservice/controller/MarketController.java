package com.sankalp.marketservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sankalp.marketservice.dto.LivePriceResponse;
import com.sankalp.marketservice.dto.MarketStatusResponse;
import com.sankalp.marketservice.service.MarketHoursService;
import com.sankalp.marketservice.service.PriceEngineService;

@RestController
@RequestMapping("/api/market")
public class MarketController {

	private final MarketHoursService marketHoursService;
	private final PriceEngineService priceEngineService;

	public MarketController(MarketHoursService marketHoursService, PriceEngineService priceEngineService) {
		this.marketHoursService = marketHoursService;
		this.priceEngineService = priceEngineService;
	}

	@GetMapping("/status")
	public ResponseEntity<MarketStatusResponse> status() {
		return ResponseEntity.ok(marketHoursService.getCurrentStatus());
	}

	@GetMapping("/prices/live")
	public ResponseEntity<List<LivePriceResponse>> livePrices(@RequestParam(required = false) Integer exchangeId) {
		return ResponseEntity.ok(priceEngineService.getLivePrices(exchangeId));
	}

	@PostMapping("/prices/reload")
	public ResponseEntity<String> reloadFromDatabase() {
		priceEngineService.reloadFromDatabase();
		return ResponseEntity.ok("Market simulation state reloaded");
	}
}
