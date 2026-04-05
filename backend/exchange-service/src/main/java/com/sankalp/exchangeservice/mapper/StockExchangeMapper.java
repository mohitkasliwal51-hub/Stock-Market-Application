package com.sankalp.exchangeservice.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;

import com.sankalp.exchangeservice.dto.StockExchangeDto;
import com.sankalp.exchangeservice.entity.StockExchange;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface StockExchangeMapper {

	StockExchangeDto toDto(StockExchange stockExchange);

	StockExchange toEntity(StockExchangeDto dto);

	default List<StockExchangeDto> toDtoList(List<StockExchange> stockExchanges) {
		if (stockExchanges == null) {
			return Collections.emptyList();
		}
		return stockExchanges.stream().map(this::toDto).toList();
	}
}
