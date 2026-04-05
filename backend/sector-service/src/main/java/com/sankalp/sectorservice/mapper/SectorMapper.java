package com.sankalp.sectorservice.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;

import com.sankalp.sectorservice.dto.SectorDto;
import com.sankalp.sectorservice.entity.Sector;

@Mapper(componentModel = "spring")
public interface SectorMapper {

	SectorDto toDto(Sector sector);

	Sector toEntity(SectorDto dto);

	default List<SectorDto> toDtoList(List<Sector> sectors) {
		if (sectors == null) {
			return Collections.emptyList();
		}
		return sectors.stream().map(this::toDto).toList();
	}
}
