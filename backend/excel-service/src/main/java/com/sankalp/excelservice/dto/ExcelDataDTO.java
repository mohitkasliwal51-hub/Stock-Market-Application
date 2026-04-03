package com.sankalp.excelservice.dto;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDataDTO {
	
	@NotNull(message = "Company ID cannot be null")
	@Positive(message = "Company ID must be positive")
	private int companyId;
	
	@NotNull(message = "Exchange ID cannot be null")
	@Positive(message = "Exchange ID must be positive")
	private int exchangeId;
	
	@NotNull(message = "Price cannot be null")
	@Positive(message = "Price must be positive")
	private double price;
	
	private Timestamp timestamp;
	
}
