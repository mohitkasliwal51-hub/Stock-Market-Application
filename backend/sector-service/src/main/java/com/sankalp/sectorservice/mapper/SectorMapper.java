package com.sankalp.sectorservice.mapper;

import com.sankalp.sectorservice.dto.SectorDto;
import com.sankalp.sectorservice.entity.Sector;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SectorMapper {
    
    public SectorDto toDto(Sector sector) {
        if (sector == null) {
            return null;
        }
        SectorDto dto = new SectorDto();
        dto.setId(sector.getId());
        dto.setName(sector.getName());
        dto.setBrief(sector.getBrief());
        return dto;
    }

    public Sector toEntity(SectorDto dto) {
        if (dto == null) {
            return null;
        }
        Sector sector = new Sector();
        sector.setId(dto.getId());
        sector.setName(dto.getName());
        sector.setBrief(dto.getBrief());
        return sector;
    }

    public List<SectorDto> toDtoList(List<Sector> sectors) {
        if (sectors == null) {
            return null;
        }
        return sectors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
