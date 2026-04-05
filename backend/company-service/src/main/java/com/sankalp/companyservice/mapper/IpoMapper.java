package com.sankalp.companyservice.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sankalp.companyservice.dto.IpoDto;
import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.entity.Ipo;
import com.sankalp.companyservice.entity.StockExchange;

@Mapper(componentModel = "spring")
public interface IpoMapper {

    @Mapping(target = "companyId", source = "company")
    @Mapping(target = "stockExchangeId", source = "stockExchange")
    @Mapping(target = "dateTime", source = "dateTime")
    IpoDto toDto(Ipo ipo);

    @Mapping(target = "company", source = "companyId")
    @Mapping(target = "stockExchange", source = "stockExchangeId")
    @Mapping(target = "dateTime", source = "dateTime")
    Ipo toEntity(IpoDto dto);

    default List<IpoDto> toDtoList(List<Ipo> ipos) {
        if (ipos == null) {
            return Collections.emptyList();
        }
        return ipos.stream().map(this::toDto).toList();
    }

    default List<Ipo> toEntityList(List<IpoDto> ipoDtos) {
        if (ipoDtos == null) {
            return Collections.emptyList();
        }
        return ipoDtos.stream().map(this::toEntity).toList();
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

    default LocalDateTime map(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    default Timestamp map(LocalDateTime dateTime) {
        return dateTime == null ? null : Timestamp.valueOf(dateTime);
    }
}