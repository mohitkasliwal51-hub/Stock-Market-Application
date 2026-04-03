package com.sankalp.sectorservice.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sankalp.sectorservice.dto.ApiResult;
import com.sankalp.sectorservice.dto.CompanyDto;
import com.sankalp.sectorservice.dto.SectorDto;
import com.sankalp.sectorservice.entity.Company;
import com.sankalp.sectorservice.entity.Sector;
import com.sankalp.sectorservice.mapper.CompanyMapper;
import com.sankalp.sectorservice.mapper.SectorMapper;
import com.sankalp.sectorservice.service.SectorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("api/sectors")
@Tag(name = "Sector Management", description = "Operations for managing sectors and companies")
public class SectorController {

	@Autowired
	private SectorService sectorService;
	
	@Autowired
	private SectorMapper sectorMapper;
	
	@Autowired
	private CompanyMapper companyMapper;
	
	@GetMapping
	@Operation(summary = "Get all sectors", description = "Retrieve a list of all sectors in the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved sectors"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<SectorDto>>> getAllSectors(){
		List<SectorDto> sectors = sectorMapper.toDtoList(sectorService.getAllSectors());
		return ResponseEntity.ok(ApiResult.success("Sectors retrieved successfully", sectors));
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Get sector by ID", description = "Retrieve a specific sector by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sector found"),
		@ApiResponse(responseCode = "404", description = "Sector not found")
	})
	public ResponseEntity<ApiResult<SectorDto>> getSectorById(
			@Parameter(description = "ID of the sector to retrieve", required = true)
			@PathVariable(value = "id") int id){
		Sector sector = sectorService.getSectorById(id);
		if (sector == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Sector not found with id: " + id));
		}
		SectorDto sectorDto = sectorMapper.toDto(sector);
		return ResponseEntity.ok(ApiResult.success("Sector retrieved successfully", sectorDto));
	}
	
	@PostMapping
	@Operation(summary = "Create a new sector", description = "Add a new sector to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Sector created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<SectorDto>> createSector(
			@Parameter(description = "Sector object to be created", required = true)
			@Valid @RequestBody SectorDto sectorDto){
		Sector sector = sectorMapper.toEntity(sectorDto);
		Sector createdSector = sectorService.createSector(sector);
		SectorDto responseDto = sectorMapper.toDto(createdSector);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResult.success("Sector created successfully", responseDto));
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Update sector", description = "Update an existing sector's information")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sector updated successfully"),
		@ApiResponse(responseCode = "404", description = "Sector not found"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<SectorDto>> updateSector(
			@Parameter(description = "ID of the sector to update", required = true)
			@PathVariable(value = "id") int id,
			@Parameter(description = "Updated sector information", required = true)
			@Valid @RequestBody SectorDto sectorDto){
		Sector sector = sectorService.getSectorById(id);
		if (sector == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Sector not found with id: " + id));
		}
		sector.setName(sectorDto.getName());
		sector.setBrief(sectorDto.getBrief());
		Sector updatedSector = sectorService.createSector(sector);
		SectorDto responseDto = sectorMapper.toDto(updatedSector);
		return ResponseEntity.ok(ApiResult.success("Sector updated successfully", responseDto));
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "Delete sector", description = "Remove a sector from the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sector deleted successfully"),
		@ApiResponse(responseCode = "404", description = "Sector not found"),
		@ApiResponse(responseCode = "409", description = "Sector has dependent companies")
	})
	public ResponseEntity<ApiResult<?>> deleteSector(
			@Parameter(description = "ID of the sector to delete", required = true)
			@PathVariable(value = "id") int id){
		Sector sector = sectorService.getSectorById(id);
		if (sector == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Sector not found with id: " + id));
		}
		sectorService.deleteSector(id);
		return ResponseEntity.ok(ApiResult.success("Sector deleted successfully", null));
	}
	
	@GetMapping("/{id}/companies")
	@Operation(summary = "Get companies by sector", description = "Retrieve all companies belonging to a specific sector")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "Sector not found")
	})
	public ResponseEntity<ApiResult<List<CompanyDto>>> getAllCompaniesBySector(
			@Parameter(description = "ID of the sector", required = true)
			@PathVariable(value = "id") int id){
		Sector sector = sectorService.getSectorById(id);
		if (sector == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Sector not found with id: " + id));
		}
		List<Company> companies = sectorService.getCompaniesBySector(id);
		List<CompanyDto> companyDtos = companyMapper.toDtoList(companies);
		return ResponseEntity.ok(ApiResult.success("Companies retrieved successfully", companyDtos));
	}
}
