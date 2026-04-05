package com.sankalp.exchangeservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sankalp.exchangeservice.dto.ApiResult;
import com.sankalp.exchangeservice.dto.CompanyDto;
import com.sankalp.exchangeservice.entity.StockExchange;
import com.sankalp.exchangeservice.repository.StockExchangeRepository;

@Service
public class StockExchangeService {
	
	@Autowired
	private StockExchangeRepository stockExchangeRepository;

	@Autowired
	private RestTemplate restTemplate;
	
	public List<StockExchange> getAllStockExchanges(){
		return stockExchangeRepository.findAll();
	}
	
	public StockExchange addStockExchange(StockExchange stockExchange) {
		return stockExchangeRepository.save(stockExchange);
	}
	
	public StockExchange getStockExchangeById(int id) {
		Optional<StockExchange> stockExchangeOptional = stockExchangeRepository.findById(id);
		if(stockExchangeOptional.isPresent())
			return stockExchangeOptional.get();
		return null;
	}
	
	public void deleteStockExchange(int id) {
		stockExchangeRepository.deleteById(id);
	}

	public ResponseEntity<ApiResult<List<CompanyDto>>> getCompaniesByExchangeId(int id) {
		StockExchange exchange = getStockExchangeById(id);
		if (exchange == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResult.error("Stock exchange not found with id: " + id));
		}

		String apiUrl = "http://COMPANY-SERVICE/api/companies/by-exchange/" + id;
		try {
			ResponseEntity<ApiResult<List<CompanyDto>>> response = restTemplate.exchange(
					apiUrl,
					HttpMethod.GET,
					HttpEntity.EMPTY,
					new ParameterizedTypeReference<ApiResult<List<CompanyDto>>>() {}
			);
			ApiResult<List<CompanyDto>> body = response.getBody();
			if (body == null) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
						.body(ApiResult.error("Company service returned an empty response"));
			}
			return ResponseEntity.status(response.getStatusCode()).body(body);
		} catch (RestClientException ex) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(ApiResult.error("Failed to retrieve companies from Company Service"));
		}
	}
	
}
