package com.sankalp.companyservice.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sankalp.companyservice.dto.ApiResult;
import com.sankalp.companyservice.dto.StockPriceDto;
import com.sankalp.companyservice.entity.StockPrice;
import com.sankalp.companyservice.mapper.StockPriceMapper;
import com.sankalp.companyservice.service.StockPriceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("api/stock-prices")
@Tag(name = "Stock Price Management", description = "Operations for managing stock prices")
public class StockPriceController {
	
	@Autowired
	private StockPriceService stockPriceService;
	
	@Autowired
	private StockPriceMapper stockPriceMapper;
	
	@GetMapping
	@Operation(summary = "Get all stock prices", description = "Retrieve all stock price records")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Stock prices retrieved successfully"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<ApiResult<List<StockPriceDto>>> getAllStockPrices(){
		List<StockPriceDto> stockPrices = stockPriceMapper.toDtoList(stockPriceService.getAllStockPrices());
		return ResponseEntity.ok(ApiResult.success("Stock prices retrieved successfully", stockPrices));
	}
	
	@PostMapping
	@Operation(summary = "Add stock prices", description = "Add a list of stock price records")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Stock prices added successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input data")
	})
	public ResponseEntity<ApiResult<List<StockPriceDto>>> addStockPrices(@Valid @RequestBody List<StockPriceDto> stockPriceDtos){
		List<StockPrice> stockPrices = stockPriceMapper.toEntityList(stockPriceDtos);
		List<StockPrice> createdStockPrices = stockPriceService.addStockPriceEntities(stockPrices);
		List<StockPriceDto> responseDtos = stockPriceMapper.toDtoList(createdStockPrices);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResult.success("Stock prices added successfully", responseDtos));
	}
	
	@GetMapping("/by-company/{companyId}/{exchangeId}/{before}/{after}")
	@Operation(summary = "Get stock prices by company", description = "Retrieve stock prices for a company and exchange within a date range")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Stock prices retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "No matching stock prices found")
	})
	public ResponseEntity<ApiResult<List<StockPriceDto>>> getStockPricesByCompany(
			@Parameter(description = "Company ID", required = true)
			@PathVariable(value = "companyId") int companyId, 
			@Parameter(description = "Exchange ID", required = true)
			@PathVariable(value = "exchangeId") int exchangeId, 
			@Parameter(description = "Start date or before value", required = true)
			@PathVariable(value = "before") String before, 
			@Parameter(description = "End date or after value", required = true)
			@PathVariable(value = "after") String after){
		List<StockPrice> stockPrices = stockPriceService.getStockPriceByCompany(companyId, exchangeId, before, after);
		List<StockPriceDto> stockPriceDtos = stockPriceMapper.toDtoList(stockPrices);
		return ResponseEntity.ok(ApiResult.success("Stock prices retrieved successfully", stockPriceDtos));
	}
}