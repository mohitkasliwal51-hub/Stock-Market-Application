package com.sankalp.marketservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sankalp.marketservice.entity.StockPrice;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Integer> {

	@Query(value = """
			SELECT sp.*
			FROM stock_price sp
			INNER JOIN (
				SELECT stock_id, MAX(timestamp) AS max_ts
				FROM stock_price
				GROUP BY stock_id
			) latest ON latest.stock_id = sp.stock_id AND latest.max_ts = sp.timestamp
			""", nativeQuery = true)
	List<StockPrice> findLatestPricesForAllStocks();
}
