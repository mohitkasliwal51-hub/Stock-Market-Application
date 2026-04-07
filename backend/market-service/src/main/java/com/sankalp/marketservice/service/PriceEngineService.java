package com.sankalp.marketservice.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sankalp.marketservice.dto.LivePriceResponse;
import com.sankalp.marketservice.entity.Stock;
import com.sankalp.marketservice.entity.StockPrice;
import com.sankalp.marketservice.repository.StockPriceRepository;
import com.sankalp.marketservice.repository.StockRepository;

import jakarta.annotation.PostConstruct;

@Service
public class PriceEngineService {

	private final StockRepository stockRepository;
	private final StockPriceRepository stockPriceRepository;
	private final MarketHoursService marketHoursService;

	private final double maxChangePerSecond;
	private final double maxChangePerDay;
	private final double seedPrice;
	private final boolean simulationEnabled;

	private final Map<Integer, PriceState> stateByStockId = new ConcurrentHashMap<>();

	public PriceEngineService(
			StockRepository stockRepository,
			StockPriceRepository stockPriceRepository,
			MarketHoursService marketHoursService,
			@Value("${market.max-change-per-second:5}") double maxChangePerSecond,
			@Value("${market.max-change-per-day:20}") double maxChangePerDay,
			@Value("${market.seed-price:100}") double seedPrice,
			@Value("${market.simulation.enabled:true}") boolean simulationEnabled) {
		this.stockRepository = stockRepository;
		this.stockPriceRepository = stockPriceRepository;
		this.marketHoursService = marketHoursService;
		this.maxChangePerSecond = maxChangePerSecond;
		this.maxChangePerDay = maxChangePerDay;
		this.seedPrice = seedPrice;
		this.simulationEnabled = simulationEnabled;
	}

	@PostConstruct
	public void initialize() {
		reloadFromDatabase();
	}

	public synchronized void reloadFromDatabase() {
		List<Stock> stocks = stockRepository.findAll();
		Map<Integer, StockPrice> latestPriceByStock = stockPriceRepository.findLatestPricesForAllStocks().stream()
				.filter(sp -> sp.getStock() != null)
				.collect(Collectors.toMap(
						sp -> sp.getStock().getId(),
						Function.identity(),
						(existing, replacement) -> replacement));

		LocalDate today = ZonedDateTime.now(marketHoursService.marketZone()).toLocalDate();
		Map<Integer, PriceState> rebuilt = new ConcurrentHashMap<>();

		for (Stock stock : stocks) {
			StockPrice latest = latestPriceByStock.get(stock.getId());
			double startingPrice = latest == null ? seedPrice : latest.getPrice();
			Instant lastUpdated = latest == null ? Instant.now() : latest.getTimestamp().toInstant();

			rebuilt.put(stock.getId(), new PriceState(stock, startingPrice, startingPrice, today, lastUpdated));
		}

		stateByStockId.clear();
		stateByStockId.putAll(rebuilt);
	}

	@Scheduled(fixedRateString = "${market.simulation.tick-ms:1000}")
	public void tick() {
		if (!simulationEnabled) {
			return;
		}

		ZonedDateTime now = ZonedDateTime.now(marketHoursService.marketZone());
		if (!marketHoursService.isMarketOpen(now)) {
			return;
		}

		if (stateByStockId.isEmpty()) {
			reloadFromDatabase();
			if (stateByStockId.isEmpty()) {
				return;
			}
		}

		LocalDate today = now.toLocalDate();
		Timestamp timestamp = Timestamp.from(now.toInstant());
		List<StockPrice> ticksToPersist = new ArrayList<>();

		for (Map.Entry<Integer, PriceState> entry : stateByStockId.entrySet()) {
			PriceState current = normalizeTradingDay(entry.getValue(), today);
			double randomDelta = ThreadLocalRandom.current().nextDouble(-maxChangePerSecond, maxChangePerSecond);
			double nextPrice = PriceConstraintEngine.nextPrice(
					current.currentPrice(),
					current.dayOpeningPrice(),
					maxChangePerSecond,
					maxChangePerDay,
					randomDelta);

			PriceState nextState = new PriceState(
					current.stock(),
					nextPrice,
					current.dayOpeningPrice(),
					today,
					now.toInstant());

			stateByStockId.put(entry.getKey(), nextState);
			ticksToPersist.add(new StockPrice(nextPrice, timestamp, current.stock()));
		}

		stockPriceRepository.saveAll(ticksToPersist);
	}

	public List<LivePriceResponse> getLivePrices(Integer exchangeId) {
		return stateByStockId.values().stream()
				.filter(state -> exchangeId == null || Objects.equals(state.stock().getStockExchangeId(), exchangeId))
				.map(state -> new LivePriceResponse(
						state.stock().getId(),
						state.stock().getStockCode(),
						state.stock().getStockExchangeId(),
						state.currentPrice(),
						state.dayOpeningPrice(),
						roundToTwoDecimals(state.currentPrice() - state.dayOpeningPrice()),
						state.lastUpdated()))
				.sorted((a, b) -> Integer.compare(a.stockId(), b.stockId()))
				.toList();
	}

	private PriceState normalizeTradingDay(PriceState priceState, LocalDate today) {
		if (priceState.tradingDay().equals(today)) {
			return priceState;
		}

		return new PriceState(
				priceState.stock(),
				priceState.currentPrice(),
				priceState.currentPrice(),
				today,
				priceState.lastUpdated());
	}

	private double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	private record PriceState(
			Stock stock,
			double currentPrice,
			double dayOpeningPrice,
			LocalDate tradingDay,
			Instant lastUpdated) {
	}
}
