package com.sankalp.companyservice.dto;

import java.math.BigDecimal;

import com.sankalp.companyservice.entity.CompanyStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CompanyDto {
        private Integer id;
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;
    @NotBlank(message = "CEO name is required")
    @Size(min = 2, max = 50, message = "CEO name must be between 2 and 50 characters")
    private String ceo;
    @NotBlank(message = "Board of Directors is required")
    @Size(min = 5, max = 500, message = "Board of Directors must be between 5 and 500 characters")
    private String boardOfDirectors;
    private Integer sectorId;
    private String briefWriteup;
    private CompanyStatus status;
    @NotNull(message = "Turnover is required")
    @Positive(message = "Turnover must be positive")
    private BigDecimal turnover;

}
