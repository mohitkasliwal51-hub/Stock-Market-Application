package com.sankalp.excelservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sankalp.excelservice.dto.ApiResult;
import com.sankalp.excelservice.dto.ExcelDataDTO;

@Service
public class ExcelDataService {
	@Autowired
	private RestTemplate restTemplate;

	public ResponseEntity<ApiResult<List<ExcelDataDTO>>> uploadData(List<ExcelDataDTO> data) {
		if (data == null || data.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(ApiResult.error("Data cannot be empty", "INVALID_INPUT"));
		}

		try {
			String apiUrl = "http://COMPANY-SERVICE/api/stock-prices/uploadData";
			ResponseEntity<ApiResult<List<ExcelDataDTO>>> response = restTemplate.exchange(
					apiUrl,
					HttpMethod.POST,
					new HttpEntity<>(data),
					new ParameterizedTypeReference<ApiResult<List<ExcelDataDTO>>>() {}
			);
			ApiResult<List<ExcelDataDTO>> body = response.getBody();
			if (body == null) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
						.body(ApiResult.error("Company service returned an empty response", "SERVICE_UNAVAILABLE"));
			}
			return ResponseEntity.status(response.getStatusCode()).body(body);
		} catch (RestClientException e) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(ApiResult.error("Company service is unavailable: " + e.getMessage(), "SERVICE_UNAVAILABLE"));
		}
	}
}