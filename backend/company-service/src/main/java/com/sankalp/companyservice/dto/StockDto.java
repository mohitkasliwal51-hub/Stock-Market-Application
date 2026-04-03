package com.sankalp.companyservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockDto {
    private Integer id;
    
    @NotBlank(message = "Stock code is required")
    private String stockCode;
    
    private Integer companyId;
    private Integer stockExchangeId;
}