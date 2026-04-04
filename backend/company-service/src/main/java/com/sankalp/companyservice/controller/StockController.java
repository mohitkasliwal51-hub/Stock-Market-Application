package com.sankalp.companyservice.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sankalp.companyservice.dto.ApiResult;
import com.sankalp.companyservice.dto.StockDto;
import com.sankalp.companyservice.entity.Stock;
import com.sankalp.companyservice.mapper.StockMapper;
import com.sankalp.companyservice.service.StockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/stocks")
@Tag(name = "Stock Management", description = "Operations for managing stocks")
public class StockController {
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private StockMapper stockMapper;
	
	@GetMapping
	@Operation(summary = "Get all stocks", description = "Retrieve a list of all stocks in the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Stocks retrieved successfully"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<StockDto>>> getAllStocks(){
		List<StockDto> stocks = stockMapper.toDtoList(stockService.getAllStocks());
		return ResponseEntity.ok(ApiResult.success("Stocks retrieved successfully", stocks));
	}
	
	@PostMapping
	@Operation(summary = "Create new stock", description = "Add a new stock to the system")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Stock created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<StockDto>> addStock(
			@Parameter(description = "Stock object to be created", required = true)
			@Valid @RequestBody StockDto stockDto){
		Stock stock = stockMapper.toEntity(stockDto);
		Stock createdStock = stockService.addStock(stock);
		StockDto responseDto = stockMapper.toDto(createdStock);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResult.success("Stock created successfully", responseDto));
	}
}