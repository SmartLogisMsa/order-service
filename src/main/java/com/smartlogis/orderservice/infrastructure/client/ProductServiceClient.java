package com.smartlogis.orderservice.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.smartlogis.common.presentation.ApiResponse;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckRequest;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckResponse;
import com.smartlogis.orderservice.infrastructure.client.dto.ProductResponse;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

	@PostMapping("/v1/products/inventory/check")
	InventoryCheckResponse checkInventories(@RequestBody InventoryCheckRequest request);

	@GetMapping("/v1/products/{id}")
	ApiResponse<ProductResponse> getProduct(@PathVariable("id") UUID id);
}
