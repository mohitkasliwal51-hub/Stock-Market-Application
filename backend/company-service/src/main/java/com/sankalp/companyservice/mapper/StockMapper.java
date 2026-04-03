package com.sankalp.companyservice.mapper;

import com.sankalp.companyservice.dto.StockDto;
import com.sankalp.companyservice.entity.Stock;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockMapper {
    
    public StockDto toDto(Stock stock) {
        if (stock == null) return null;
        
        StockDto dto = new StockDto();
        dto.setId(stock.getId());
        dto.setStockCode(stock.getStockCode());
        
        if (stock.getCompany() != null) {
            dto.setCompanyId(stock.getCompany().getId());
        }
        if (stock.getStockExchange() != null) {
            dto.setStockExchangeId(stock.getStockExchange().getId());
        }
        
        return dto;
    }
    
    public Stock toEntity(StockDto dto) {
        if (dto == null) return null;
        
        Stock stock = new Stock();
        stock.setId(dto.getId());
        stock.setStockCode(dto.getStockCode());
        
        return stock;
    }
    
    public List<StockDto> toDtoList(List<Stock> stocks) {
        if (stocks == null) return null;
        return stocks.stream().map(this::toDto).collect(Collectors.toList());
    }
    
    public List<Stock> toEntityList(List<StockDto> stockDtos) {
        if (stockDtos == null) return null;
        return stockDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}