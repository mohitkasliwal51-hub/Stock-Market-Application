package com.sankalp.portfolioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.portfolioservice.entity.Portfolio;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {

	List<Portfolio> findByUserIdAndActiveTrue(Integer userId);
}
