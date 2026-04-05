package com.sankalp.companyservice.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sankalp.companyservice.dto.CompanyDto;
import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.entity.Sector;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

	@Mapping(target = "companyName", source = "name")
	@Mapping(target = "boardOfDirectors", source = "bod")
	@Mapping(target = "briefWriteup", source = "brief")
	@Mapping(target = "turnover", source = "turnover")
	@Mapping(target = "sectorId", source = "sector")
	CompanyDto toDto(Company company);

	@Mapping(target = "name", source = "companyName")
	@Mapping(target = "bod", source = "boardOfDirectors")
	@Mapping(target = "brief", source = "briefWriteup")
	@Mapping(target = "turnover", source = "turnover")
	@Mapping(target = "sector", source = "sectorId")
	Company toEntity(CompanyDto dto);

	default List<CompanyDto> toDtoList(List<Company> companies) {
		if (companies == null) {
			return Collections.emptyList();
		}
		return companies.stream().map(this::toDto).toList();
	}

	default BigDecimal map(long turnover) {
		return BigDecimal.valueOf(turnover);
	}

	default long map(BigDecimal turnover) {
		return turnover == null ? 0L : turnover.longValue();
	}

	default Integer map(Sector sector) {
		return sector == null ? null : sector.getId();
	}

	default Sector map(Integer sectorId) {
		if (sectorId == null || sectorId <= 0) {
			return null;
		}
		Sector sector = new Sector();
		sector.setId(sectorId);
		return sector;
	}
}
