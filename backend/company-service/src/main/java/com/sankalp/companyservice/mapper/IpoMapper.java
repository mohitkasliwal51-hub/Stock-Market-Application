package com.sankalp.companyservice.mapper;

import com.sankalp.companyservice.dto.IpoDto;
import com.sankalp.companyservice.entity.Ipo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IpoMapper {
    
    public IpoDto toDto(Ipo ipo) {
        if (ipo == null) return null;
        
        IpoDto dto = new IpoDto();
        dto.setId(ipo.getId());
        dto.setPricePerShare(ipo.getPricePerShare());
        dto.setTotalShares(ipo.getTotalShares());
        dto.setDateTime(ipo.getDateTime().toLocalDateTime());
        dto.setRemarks(ipo.getRemarks());
        
        if (ipo.getCompany() != null) {
            dto.setCompanyId(ipo.getCompany().getId());
        }
        if (ipo.getStockExchange() != null) {
            dto.setStockExchangeId(ipo.getStockExchange().getId());
        }
        
        return dto;
    }
    
    public Ipo toEntity(IpoDto dto) {
        if (dto == null) return null;
        
        Ipo ipo = new Ipo();
        ipo.setId(dto.getId());
        ipo.setPricePerShare(dto.getPricePerShare());
        ipo.setTotalShares(dto.getTotalShares());
        ipo.setRemarks(dto.getRemarks());
        
        if (dto.getDateTime() != null) {
            ipo.setDateTime(java.sql.Timestamp.valueOf(dto.getDateTime()));
        }
        
        return ipo;
    }
    
    public List<IpoDto> toDtoList(List<Ipo> ipos) {
        if (ipos == null) return null;
        return ipos.stream().map(this::toDto).collect(Collectors.toList());
    }
    
    public List<Ipo> toEntityList(List<IpoDto> ipoDtos) {
        if (ipoDtos == null) return null;
        return ipoDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}