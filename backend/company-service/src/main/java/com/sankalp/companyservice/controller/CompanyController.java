package com.sankalp.companyservice.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sankalp.companyservice.dto.ApiResult;
import com.sankalp.companyservice.dto.CompanyDto;
import com.sankalp.companyservice.entity.Company;
import com.sankalp.companyservice.mapper.CompanyMapper;
import com.sankalp.companyservice.service.CompanyService;
import com.sankalp.companyservice.service.DeleteCompanyResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/companies")
@Tag(name = "Company Management", description = "Operations for managing companies")
public class CompanyController {
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private CompanyMapper companyMapper;
	
	@GetMapping
	@Operation(summary = "Get all companies", description = "Retrieve a list of all companies in the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved companies"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<CompanyDto>>> getAllCompanies(){
		List<CompanyDto> companies = companyMapper.toDtoList(companyService.getAllCompanies());
		return ResponseEntity.ok(ApiResult.success("Companies retrieved successfully", companies));
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Get company by ID", description = "Retrieve a specific company by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Company found"),
		@ApiResponse(responseCode = "404", description = "Company not found")
	})
	public ResponseEntity<ApiResult<CompanyDto>> getCompanyById(
			@Parameter(description = "ID of the company to retrieve", required = true)
			@PathVariable(value = "id") int id){
		Company company = companyService.getCompanyById(id);
		if (company == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Company not found with id: " + id));
		}
		CompanyDto companyDto = companyMapper.toDto(company);
		return ResponseEntity.ok(ApiResult.success("Company retrieved successfully", companyDto));
	}
	
	@PostMapping
	@Operation(summary = "Create a new company", description = "Add a new company to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Company created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<CompanyDto>> createCompany(
			@Parameter(description = "Company object to be created", required = true)
			@Valid @RequestBody CompanyDto companyDto){
		Company company = companyMapper.toEntity(companyDto);
		Company createdCompany = companyService.createCompany(company);
		CompanyDto responseDto = companyMapper.toDto(createdCompany);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResult.success("Company created successfully", responseDto));
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Update company", description = "Update an existing company's information")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Company updated successfully"),
		@ApiResponse(responseCode = "404", description = "Company not found")
	})
	public ResponseEntity<ApiResult<CompanyDto>> updateCompany(
			@Parameter(description = "ID of the company to update", required = true)
			@PathVariable(value="id") int id, 
			@Parameter(description = "Updated company information", required = true)
			@Valid @RequestBody CompanyDto companyDto){
		Company company = companyMapper.toEntity(companyDto);
		Company updatedCompany = companyService.updateCompany(id, company);
		if (updatedCompany == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Company not found with id: " + id));
		}
		CompanyDto responseDto = companyMapper.toDto(updatedCompany);
		return ResponseEntity.ok(ApiResult.success("Company updated successfully", responseDto));
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "Deactivate company", description = "Deactivate a company by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Company deactivated successfully"),
		@ApiResponse(responseCode = "404", description = "Company not found")
	})
	public ResponseEntity<ApiResult<CompanyDto>> deactivateCompany(
			@Parameter(description = "ID of the company to deactivate", required = true)
			@PathVariable(value="id") int id){
		DeleteCompanyResult result = companyService.deactivateCompany(id);
		if (result.getStatus() == DeleteCompanyResult.Status.NOT_FOUND) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error(result.getMessage()));
		}
		if (result.getStatus() == DeleteCompanyResult.Status.BLOCKED_BY_DEPENDENCIES) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(ApiResult.error(result.getMessage()));
		}
		CompanyDto responseDto = companyMapper.toDto(result.getCompany());
		return ResponseEntity.ok(ApiResult.success(result.getMessage(), responseDto));
	}
	
	@GetMapping("/search/{pattern}")
	@Operation(summary = "Search companies", description = "Search companies by name pattern")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Search completed successfully")
	})
	public ResponseEntity<ApiResult<List<CompanyDto>>> getCompanyByPattern(
			@Parameter(description = "Search pattern for company names", required = true)
			@PathVariable("pattern") String pattern){
		List<CompanyDto> companies = companyMapper.toDtoList(companyService.getCompanyByPattern(pattern));
		return ResponseEntity.ok(ApiResult.success("Companies retrieved successfully", companies));
	}
	
	@GetMapping("/by-exchange/{exchangeId}")
	@Operation(summary = "Get companies by exchange", description = "Retrieve companies listed on a specific stock exchange")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Companies retrieved successfully")
	})
	public ResponseEntity<ApiResult<List<CompanyDto>>> getCompanyByExchange(
			@Parameter(description = "ID of the stock exchange", required = true)
			@PathVariable("exchangeId") int exchangeId){
		List<CompanyDto> companies = companyMapper.toDtoList(companyService.getCompanyByStockExchangeId(exchangeId));
		return ResponseEntity.ok(ApiResult.success("Companies retrieved successfully", companies));
	}
}