package com.sankalp.sectorservice.dto;

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
public class SectorDto {
    private Integer id;
    
    @NotBlank(message = "Sector name is required")
    @Size(min = 2, max = 100, message = "Sector name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Sector brief is required")
    @Size(min = 5, max = 500, message = "Sector brief must be between 5 and 500 characters")
    private String brief;
}
