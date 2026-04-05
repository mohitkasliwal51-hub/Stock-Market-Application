package com.sankalp.companyservice.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sankalp.companyservice.dto.CompanyDto;
import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.entity.Sector;

@Component
public class CompanyMapper {
    public CompanyDto toDto(Company company) {
        if (company == null) {
            return null;
        }
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setCompanyName(company.getName());
        dto.setCeo(company.getCeo());
        dto.setBoardOfDirectors(company.getBod());
        dto.setBriefWriteup(company.getBrief());
        dto.setTurnover(java.math.BigDecimal.valueOf(company.getTurnover()));
        dto.setStatus(company.getStatus());
        // Handle sector relationship
        if (company.getSector() != null) {
            dto.setSectorId(company.getSector().getId());
        }
        return dto;
    }

    public Company toEntity(CompanyDto dto) {
        if (dto == null) {
            return null;
        }
        Company company = new Company();
        company.setId(dto.getId());
        company.setName(dto.getCompanyName());
        company.setCeo(dto.getCeo());
        company.setBod(dto.getBoardOfDirectors());
        company.setBrief(dto.getBriefWriteup());
        company.setStatus(dto.getStatus());
        // Convert BigDecimal to long
        if (dto.getTurnover() != null) {
            company.setTurnover(dto.getTurnover().longValue());
        }
        if (dto.getSectorId() != null && dto.getSectorId() > 0) {
            Sector sector = new Sector();
            sector.setId(dto.getSectorId());
            company.setSector(sector);
        }
        return company;
    }

    public List<CompanyDto> toDtoList(List<Company> companies) {
        if (companies == null) {
            return null;
        }
        return companies.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
