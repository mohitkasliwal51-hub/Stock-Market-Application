package com.sankalp.orderservice.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.orderservice.entity.OrderOutboxEvent;
import com.sankalp.orderservice.entity.OutboxStatus;

@Repository
public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, Long> {

	List<OrderOutboxEvent> findTop50ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
			List<OutboxStatus> statuses,
			Timestamp now);
}