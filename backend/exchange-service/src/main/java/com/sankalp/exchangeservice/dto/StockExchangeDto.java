package com.sankalp.exchangeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockExchangeDto {
    private Integer id;
    
    @NotBlank(message = "Stock exchange name is required")
    @Size(min = 2, max = 100, message = "Stock exchange name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Stock exchange brief is required")
    @Size(min = 5, max = 500, message = "Stock exchange brief must be between 5 and 500 characters")
    private String brief;
    
    @NotBlank(message = "Remarks is required")
    @Size(min = 5, max = 500, message = "Remarks must be between 5 and 500 characters")
    private String remarks;
    
    private AddressDto address;
}
