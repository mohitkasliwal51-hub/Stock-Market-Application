package com.sankalp.marketservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.marketservice.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {
}
