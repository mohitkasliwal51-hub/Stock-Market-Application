package com.sankalp.orderservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.orderservice.entity.OrderStatus;
import com.sankalp.orderservice.entity.StockOrder;

@Repository
public interface StockOrderRepository extends JpaRepository<StockOrder, Integer> {

	List<StockOrder> findByUserIdOrderByCreatedAtDesc(Integer userId);

	List<StockOrder> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
}
