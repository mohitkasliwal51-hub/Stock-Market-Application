package com.sankalp.portfolioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.portfolioservice.entity.PortfolioPosition;

@Repository
public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, Integer> {

	List<PortfolioPosition> findByPortfolioId(Integer portfolioId);
}
