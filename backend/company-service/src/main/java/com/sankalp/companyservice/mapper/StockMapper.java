package com.sankalp.companyservice.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sankalp.companyservice.dto.StockDto;
import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.entity.Stock;
import com.sankalp.companyservice.entity.StockExchange;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "companyId", source = "company")
    @Mapping(target = "stockExchangeId", source = "stockExchange")
    StockDto toDto(Stock stock);

    @Mapping(target = "company", source = "companyId")
    @Mapping(target = "stockExchange", source = "stockExchangeId")
    Stock toEntity(StockDto dto);

    default List<StockDto> toDtoList(List<Stock> stocks) {
        if (stocks == null) {
            return Collections.emptyList();
        }
        return stocks.stream().map(this::toDto).toList();
    }

    default List<Stock> toEntityList(List<StockDto> stockDtos) {
        if (stockDtos == null) {
            return Collections.emptyList();
        }
        return stockDtos.stream().map(this::toEntity).toList();
    }

    default Integer map(Company company) {
        return company == null ? null : company.getId();
    }

    default Company map(Integer companyId) {
        if (companyId == null || companyId <= 0) {
            return null;
        }
        Company company = new Company();
        company.setId(companyId);
        return company;
    }

    default Integer map(StockExchange stockExchange) {
        return stockExchange == null ? null : stockExchange.getId();
    }

    default StockExchange mapStockExchange(Integer stockExchangeId) {
        if (stockExchangeId == null || stockExchangeId <= 0) {
            return null;
        }
        StockExchange stockExchange = new StockExchange();
        stockExchange.setId(stockExchangeId);
        return stockExchange;
    }
}