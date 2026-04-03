package com.sankalp.companyservice.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sankalp.companyservice.dto.ApiResult;
import com.sankalp.companyservice.dto.IpoDto;
import com.sankalp.companyservice.entity.Ipo;
import com.sankalp.companyservice.mapper.IpoMapper;
import com.sankalp.companyservice.service.IpoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("api/ipos")
@Tag(name = "IPO Management", description = "Operations for managing Initial Public Offerings")
public class IpoController {

	@Autowired
	private IpoService ipoService;
	
	@Autowired
	private IpoMapper ipoMapper;
	
	@GetMapping
	@Operation(summary = "Get all IPOs", description = "Retrieve a list of all IPOs in the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved IPOs"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<IpoDto>>> getAllIpos(){
		List<IpoDto> ipos = ipoMapper.toDtoList(ipoService.getAllIpo());
		return ResponseEntity.ok(ApiResult.success("IPOs retrieved successfully", ipos));
	}
	
	@PostMapping
	@Operation(summary = "Create new IPO", description = "Add a new IPO to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "IPO created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<IpoDto>> createIpo(
			@Parameter(description = "IPO object to be created", required = true)
			@Valid @RequestBody IpoDto ipoDto){
		Ipo ipo = ipoMapper.toEntity(ipoDto);
		Ipo createdIpo = ipoService.addIpo(ipo);
		IpoDto responseDto = ipoMapper.toDto(createdIpo);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResult.success("IPO created successfully", responseDto));
	}
	
	@PutMapping("/{ipoId}")
	@Operation(summary = "Update IPO", description = "Update an existing IPO's information")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "IPO updated successfully"),
		@ApiResponse(responseCode = "404", description = "IPO not found")
	})
	public ResponseEntity<ApiResult<IpoDto>> updateIpo(
			@Parameter(description = "ID of the IPO to update", required = true)
			@PathVariable(value = "ipoId") int id, 
			@Parameter(description = "Updated IPO information", required = true)
			@Valid @RequestBody IpoDto ipoDto) {
		Ipo ipo = ipoMapper.toEntity(ipoDto);
		Ipo updatedIpo = ipoService.updateIpo(id, ipo);
		if (updatedIpo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("IPO not found with id: " + id));
		}
		IpoDto responseDto = ipoMapper.toDto(updatedIpo);
		return ResponseEntity.ok(ApiResult.success("IPO updated successfully", responseDto));
	}
	
	@GetMapping("/by-company/{companyId}")
	@Operation(summary = "Get IPO by company", description = "Retrieve IPO information for a specific company")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "IPO retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "No IPO found for company")
	})
	public ResponseEntity<ApiResult<IpoDto>> getIpoByCompany(
			@Parameter(description = "ID of the company", required = true)
			@PathVariable(value = "companyId") int id){
		Ipo ipo = ipoService.getIpoByCompany(id);
		if (ipo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("No IPO found with company id: " + id));
		}
		IpoDto ipoDto = ipoMapper.toDto(ipo);
		return ResponseEntity.ok(ApiResult.success("IPO retrieved successfully", ipoDto));
	}
}