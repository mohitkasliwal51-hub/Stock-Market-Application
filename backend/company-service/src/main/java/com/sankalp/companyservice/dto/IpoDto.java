package com.sankalp.companyservice.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class IpoDto {
    private Integer id;
    
    @NotNull(message = "Price per share is required")
    @DecimalMin(value = "0.01", message = "Price per share must be positive")
    private Double pricePerShare;
    
    @NotNull(message = "Total shares is required")
    @Min(value = 1, message = "Total shares must be at least 1")
    private Integer totalShares;
    
    @NotNull(message = "Date time is required")
    private LocalDateTime dateTime;
    
    private String remarks;
    private Integer companyId;
    private Integer stockExchangeId;
}