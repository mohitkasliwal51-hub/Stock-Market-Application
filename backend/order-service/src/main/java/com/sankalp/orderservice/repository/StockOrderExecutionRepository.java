package com.sankalp.orderservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.orderservice.entity.StockOrderExecution;

@Repository
public interface StockOrderExecutionRepository extends JpaRepository<StockOrderExecution, Integer> {
	List<StockOrderExecution> findByOrderIdOrderByEventAtDesc(Integer orderId);
}