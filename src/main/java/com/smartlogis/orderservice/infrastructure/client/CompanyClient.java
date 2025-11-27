package com.smartlogis.orderservice.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartlogis.common.presentation.ApiResponse;
import com.smartlogis.orderservice.infrastructure.client.dto.CompanyResponse;

@FeignClient(name = "company-service")
public interface CompanyClient {

	@GetMapping("/{id}")
	ApiResponse<CompanyResponse> getCompany(@PathVariable("id") UUID id);
}
