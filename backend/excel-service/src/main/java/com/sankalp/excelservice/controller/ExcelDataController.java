package com.sankalp.excelservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sankalp.excelservice.dto.ApiResult;
import com.sankalp.excelservice.dto.ExcelDataDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/excel")
@Tag(name = "Excel Data Management", description = "APIs for importing and processing Excel data")
public class ExcelDataController {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/health")
	@Operation(summary = "Health check", description = "Verify Excel service is running")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Service is healthy", content = @Content(schema = @Schema(implementation = ApiResult.class))),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<Void>> health() {
		return ResponseEntity.ok(ApiResult.success("Excel service is healthy", null));
	}
	
	@PostMapping("/uploadData")
	@Operation(summary = "Upload Excel data", description = "Import stock prices from Excel and process them via company service")
	@Parameter(name = "data", description = "List of Excel data entries containing companyId, exchangeId, price, and timestamp", required = true)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Data uploaded successfully", content = @Content(schema = @Schema(implementation = ApiResult.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
		@ApiResponse(responseCode = "503", description = "Company service is unavailable"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<ExcelDataDTO>>> uploadData(
			@Valid @RequestBody List<ExcelDataDTO> data) {
		if (data == null || data.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(ApiResult.error("Data cannot be empty", "INVALID_INPUT"));
		}
		
		try {
			String apiUrl = "http://COMPANY-SERVICE/company/addStockPrices";
			ResponseEntity<List<ExcelDataDTO>> response = restTemplate.exchange(
					apiUrl,
					HttpMethod.POST,
					new HttpEntity<>(data),
					new ParameterizedTypeReference<List<ExcelDataDTO>>() {}
			);
			List<ExcelDataDTO> failedInserts = response.getBody();
			
			if (failedInserts == null || failedInserts.isEmpty()) {
				return ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(ApiResult.success("All records processed successfully", new ArrayList<>()));
			} else {
				return ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(ApiResult.success("Some records failed processing", failedInserts));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(ApiResult.error("Company service is unavailable: " + e.getMessage(), "SERVICE_UNAVAILABLE"));
		}
	}
	
}
