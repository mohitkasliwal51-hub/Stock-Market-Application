package com.sankalp.exchangeservice.mapper;

import com.sankalp.exchangeservice.dto.StockExchangeDto;
import com.sankalp.exchangeservice.entity.StockExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockExchangeMapper {
    
    @Autowired
    private AddressMapper addressMapper;
    
    public StockExchangeDto toDto(StockExchange stockExchange) {
        if (stockExchange == null) {
            return null;
        }
        StockExchangeDto dto = new StockExchangeDto();
        dto.setId(stockExchange.getId());
        dto.setName(stockExchange.getName());
        dto.setBrief(stockExchange.getBrief());
        dto.setRemarks(stockExchange.getRemarks());
        if (stockExchange.getAddress() != null) {
            dto.setAddress(addressMapper.toDto(stockExchange.getAddress()));
        }
        return dto;
    }

    public StockExchange toEntity(StockExchangeDto dto) {
        if (dto == null) {
            return null;
        }
        StockExchange stockExchange = new StockExchange();
        stockExchange.setId(dto.getId());
        stockExchange.setName(dto.getName());
        stockExchange.setBrief(dto.getBrief());
        stockExchange.setRemarks(dto.getRemarks());
        if (dto.getAddress() != null) {
            stockExchange.setAddress(addressMapper.toEntity(dto.getAddress()));
        }
        return stockExchange;
    }

    public List<StockExchangeDto> toDtoList(List<StockExchange> stockExchanges) {
        if (stockExchanges == null) {
            return null;
        }
        return stockExchanges.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
