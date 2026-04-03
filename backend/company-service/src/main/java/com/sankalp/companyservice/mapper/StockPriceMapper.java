package com.sankalp.companyservice.mapper;

import com.sankalp.companyservice.dto.StockPriceDto;
import com.sankalp.companyservice.entity.StockPrice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockPriceMapper {
    
    public StockPriceDto toDto(StockPrice stockPrice) {
        if (stockPrice == null) return null;
        
        StockPriceDto dto = new StockPriceDto();
        dto.setId(stockPrice.getId());
        dto.setPrice(stockPrice.getPrice());
        dto.setTimestamp(stockPrice.getTimestamp().toLocalDateTime());
        
        if (stockPrice.getStock() != null) {
            dto.setStockId(stockPrice.getStock().getId());
        }
        
        return dto;
    }
    
    public StockPrice toEntity(StockPriceDto dto) {
        if (dto == null) return null;
        
        StockPrice stockPrice = new StockPrice();
        stockPrice.setId(dto.getId());
        stockPrice.setPrice(dto.getPrice());
        
        if (dto.getTimestamp() != null) {
            stockPrice.setTimestamp(java.sql.Timestamp.valueOf(dto.getTimestamp()));
        }
        
        return stockPrice;
    }
    
    public List<StockPriceDto> toDtoList(List<StockPrice> stockPrices) {
        if (stockPrices == null) return null;
        return stockPrices.stream().map(this::toDto).collect(Collectors.toList());
    }
    
    public List<StockPrice> toEntityList(List<StockPriceDto> stockPriceDtos) {
        if (stockPriceDtos == null) return null;
        return stockPriceDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}