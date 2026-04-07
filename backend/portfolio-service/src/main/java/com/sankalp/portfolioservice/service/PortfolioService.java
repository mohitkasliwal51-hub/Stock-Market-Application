package com.sankalp.portfolioservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sankalp.portfolioservice.dto.AddPositionRequest;
import com.sankalp.portfolioservice.dto.CreatePortfolioRequest;
import com.sankalp.portfolioservice.dto.PortfolioTradeRequest;
import com.sankalp.portfolioservice.dto.TradeSide;
import com.sankalp.portfolioservice.dto.PortfolioPositionResponse;
import com.sankalp.portfolioservice.dto.PortfolioResponse;
import com.sankalp.portfolioservice.entity.Portfolio;
import com.sankalp.portfolioservice.entity.PortfolioPosition;
import com.sankalp.portfolioservice.repository.PortfolioPositionRepository;
import com.sankalp.portfolioservice.repository.PortfolioRepository;

@Service
public class PortfolioService {

	private final PortfolioRepository portfolioRepository;
	private final PortfolioPositionRepository positionRepository;

	public PortfolioService(PortfolioRepository portfolioRepository, PortfolioPositionRepository positionRepository) {
		this.portfolioRepository = portfolioRepository;
		this.positionRepository = positionRepository;
	}

	@Transactional
	public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
		Portfolio portfolio = new Portfolio();
		portfolio.setUserId(request.getUserId());
		portfolio.setPortfolioName(request.getPortfolioName().trim());

		Portfolio saved = portfolioRepository.save(portfolio);
		return toResponse(saved, List.of());
	}

	@Transactional(readOnly = true)
	public List<PortfolioResponse> getPortfoliosByUser(Integer userId) {
		return portfolioRepository.findByUserIdAndActiveTrue(userId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public PortfolioResponse getPortfolio(Integer portfolioId) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
				.orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));
		return toResponse(portfolio);
	}

	@Transactional
	public PortfolioPositionResponse addPosition(Integer portfolioId, AddPositionRequest request) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
				.orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

		PortfolioPosition position = new PortfolioPosition();
		position.setPortfolio(portfolio);
		position.setStockId(request.getStockId());
		position.setQuantity(request.getQuantity());
		position.setAverageBuyPrice(request.getAverageBuyPrice());

		return toPositionResponse(positionRepository.save(position));
	}

	@Transactional(readOnly = true)
	public List<PortfolioPositionResponse> getPositions(Integer portfolioId) {
		if (!portfolioRepository.existsById(portfolioId)) {
			throw new IllegalArgumentException("Portfolio not found: " + portfolioId);
		}

		return positionRepository.findByPortfolioId(portfolioId).stream()
				.map(this::toPositionResponse)
				.toList();
	}

	@Transactional
	public PortfolioPositionResponse applyTrade(Integer portfolioId, PortfolioTradeRequest request) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
				.orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

		PortfolioPosition position = positionRepository.findByPortfolioIdAndStockId(portfolioId, request.getStockId())
				.orElseGet(() -> {
					PortfolioPosition p = new PortfolioPosition();
					p.setPortfolio(portfolio);
					p.setStockId(request.getStockId());
					p.setQuantity(0);
					p.setAverageBuyPrice(BigDecimal.ZERO);
					return p;
				});

		if (request.getSide() == TradeSide.BUY) {
			applyBuy(position, request);
		} else {
			applySell(position, request);
		}

		if (position.getQuantity() == 0) {
			positionRepository.delete(position);
			return new PortfolioPositionResponse(null, request.getStockId(), 0, BigDecimal.ZERO, null);
		}

		return toPositionResponse(positionRepository.save(position));
	}

	private void applyBuy(PortfolioPosition position, PortfolioTradeRequest request) {
		BigDecimal existingQty = BigDecimal.valueOf(position.getQuantity());
		BigDecimal incomingQty = BigDecimal.valueOf(request.getQuantity());
		BigDecimal newQty = existingQty.add(incomingQty);

		BigDecimal existingCost = position.getAverageBuyPrice().multiply(existingQty);
		BigDecimal incomingCost = request.getExecutionPrice().multiply(incomingQty);
		BigDecimal newAvg = existingCost.add(incomingCost).divide(newQty, 2, RoundingMode.HALF_UP);

		position.setQuantity(newQty.intValueExact());
		position.setAverageBuyPrice(newAvg);
	}

	private void applySell(PortfolioPosition position, PortfolioTradeRequest request) {
		if (position.getQuantity() < request.getQuantity()) {
			throw new IllegalArgumentException("Insufficient portfolio quantity for sell");
		}

		int newQty = position.getQuantity() - request.getQuantity();
		position.setQuantity(newQty);
		if (newQty == 0) {
			position.setAverageBuyPrice(BigDecimal.ZERO);
		}
	}

	private PortfolioResponse toResponse(Portfolio portfolio) {
		List<PortfolioPositionResponse> positions = positionRepository.findByPortfolioId(portfolio.getId()).stream()
				.map(this::toPositionResponse)
				.toList();
		return toResponse(portfolio, positions);
	}

	private PortfolioResponse toResponse(Portfolio portfolio, List<PortfolioPositionResponse> positions) {
		return new PortfolioResponse(
				portfolio.getId(),
				portfolio.getUserId(),
				portfolio.getPortfolioName(),
				portfolio.isActive(),
				portfolio.getCreatedAt(),
				portfolio.getUpdatedAt(),
				positions);
	}

	private PortfolioPositionResponse toPositionResponse(PortfolioPosition position) {
		return new PortfolioPositionResponse(
				position.getId(),
				position.getStockId(),
				position.getQuantity(),
				position.getAverageBuyPrice(),
				position.getCreatedAt());
	}
}
