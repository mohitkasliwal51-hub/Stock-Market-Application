package com.sankalp.companyservice.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sankalp.companyservice.dto.StockPriceDto;
import com.sankalp.companyservice.entity.Stock;
import com.sankalp.companyservice.entity.StockPrice;

@Mapper(componentModel = "spring")
public interface StockPriceMapper {

	@Mapping(target = "stockId", source = "stock")
	@Mapping(target = "timestamp", source = "timestamp")
	StockPriceDto toDto(StockPrice stockPrice);

	@Mapping(target = "stock", source = "stockId")
	@Mapping(target = "timestamp", source = "timestamp")
	StockPrice toEntity(StockPriceDto dto);

	default List<StockPriceDto> toDtoList(List<StockPrice> stockPrices) {
		if (stockPrices == null) {
			return Collections.emptyList();
		}
		return stockPrices.stream().map(this::toDto).toList();
	}

	default List<StockPrice> toEntityList(List<StockPriceDto> stockPriceDtos) {
		if (stockPriceDtos == null) {
			return Collections.emptyList();
		}
		return stockPriceDtos.stream().map(this::toEntity).toList();
	}

	default Integer map(Stock stock) {
		return stock == null ? null : stock.getId();
	}

	default Stock map(Integer stockId) {
		if (stockId == null || stockId <= 0) {
			return null;
		}
		Stock stock = new Stock();
		stock.setId(stockId);
		return stock;
	}

	default LocalDateTime map(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	default Timestamp map(LocalDateTime dateTime) {
		return dateTime == null ? null : Timestamp.valueOf(dateTime);
	}
}