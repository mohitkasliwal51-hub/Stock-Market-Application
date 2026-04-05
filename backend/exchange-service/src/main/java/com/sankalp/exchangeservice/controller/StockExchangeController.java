package com.sankalp.exchangeservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sankalp.exchangeservice.dto.ApiResult;
import com.sankalp.exchangeservice.dto.CompanyDto;
import com.sankalp.exchangeservice.dto.StockExchangeDto;
import com.sankalp.exchangeservice.entity.StockExchange;
import com.sankalp.exchangeservice.mapper.StockExchangeMapper;
import com.sankalp.exchangeservice.service.StockExchangeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/stockExchanges")
@Tag(name = "Stock Exchange Management", description = "Operations for managing stock exchanges")
public class StockExchangeController {

	@Autowired
	private StockExchangeService stockExchangeService;
	
	@Autowired
	private StockExchangeMapper stockExchangeMapper;
	
	@GetMapping
	@Operation(summary = "Get all stock exchanges", description = "Retrieve a list of all stock exchanges in the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved stock exchanges"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<StockExchangeDto>>> getStockExchanges(){
		List<StockExchangeDto> exchanges = stockExchangeMapper.toDtoList(stockExchangeService.getAllStockExchanges());
		return ResponseEntity.ok(ApiResult.success("Stock exchanges retrieved successfully", exchanges));
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Get stock exchange by ID", description = "Retrieve a specific stock exchange by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Stock exchange found"),
		@ApiResponse(responseCode = "404", description = "Stock exchange not found")
	})
	public ResponseEntity<ApiResult<StockExchangeDto>> getStockExchangeById(
			@Parameter(description = "ID of the stock exchange to retrieve", required = true)
			@PathVariable(value = "id") int id){
		StockExchange exchange = stockExchangeService.getStockExchangeById(id);
		if (exchange == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Stock exchange not found with id: " + id));
		}
		StockExchangeDto exchangeDto = stockExchangeMapper.toDto(exchange);
		return ResponseEntity.ok(ApiResult.success("Stock exchange retrieved successfully", exchangeDto));
	}

	@PostMapping
	@Operation(summary = "Create a new stock exchange", description = "Add a new stock exchange to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Stock exchange created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<StockExchangeDto>> addStockExchange(
			@Parameter(description = "Stock exchange object to be created", required = true)
			@Valid @RequestBody StockExchangeDto exchangeDto){
		StockExchange stockExchange = stockExchangeMapper.toEntity(exchangeDto);
		StockExchange createdExchange = stockExchangeService.addStockExchange(stockExchange);
		StockExchangeDto responseDto = stockExchangeMapper.toDto(createdExchange);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResult.success("Stock exchange created successfully", responseDto));
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Update stock exchange", description = "Update an existing stock exchange's information")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Stock exchange updated successfully"),
		@ApiResponse(responseCode = "404", description = "Stock exchange not found"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<StockExchangeDto>> updateStockExchange(
			@Parameter(description = "ID of the stock exchange to update", required = true)
			@PathVariable(value = "id") int id,
			@Parameter(description = "Updated stock exchange information", required = true)
			@Valid @RequestBody StockExchangeDto exchangeDto){
		StockExchange exchange = stockExchangeService.getStockExchangeById(id);
		if (exchange == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Stock exchange not found with id: " + id));
		}
		exchange.setName(exchangeDto.getName());
		exchange.setBrief(exchangeDto.getBrief());
		exchange.setRemarks(exchangeDto.getRemarks());
		StockExchange updatedExchange = stockExchangeService.addStockExchange(exchange);
		StockExchangeDto responseDto = stockExchangeMapper.toDto(updatedExchange);
		return ResponseEntity.ok(ApiResult.success("Stock exchange updated successfully", responseDto));
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "Delete stock exchange", description = "Remove a stock exchange from the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Stock exchange deleted successfully"),
		@ApiResponse(responseCode = "404", description = "Stock exchange not found"),
		@ApiResponse(responseCode = "409", description = "Stock exchange has dependent data")
	})
	public ResponseEntity<ApiResult<?>> deleteStockExchange(
			@Parameter(description = "ID of the stock exchange to delete", required = true)
			@PathVariable(value = "id") int id){
		StockExchange exchange = stockExchangeService.getStockExchangeById(id);
		if (exchange == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Stock exchange not found with id: " + id));
		}
		stockExchangeService.deleteStockExchange(id);
		return ResponseEntity.ok(ApiResult.success("Stock exchange deleted successfully", null));
	}
	
	@GetMapping("/{exchangeId}/companies")
	@Operation(summary = "Get companies by stock exchange", description = "Retrieve all companies listed in a specific stock exchange")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "Stock exchange not found")
	})
	public ResponseEntity<ApiResult<List<CompanyDto>>> getCompaniesByExchangeId(
			@Parameter(description = "ID of the stock exchange", required = true)
			@PathVariable(value = "exchangeId") int id) {
		return stockExchangeService.getCompaniesByExchangeId(id);
	}
	
}
