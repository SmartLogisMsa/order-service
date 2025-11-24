package com.smartlogis.orderservice.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartlogis.common.presentation.ApiResponse;
import com.smartlogis.orderservice.infrastructure.client.dto.UserInfoResponse;

@FeignClient(name = "localhost:8082")
public interface UserServiceClient {

	@GetMapping("/v1/users/{id}")
	ApiResponse<UserInfoResponse> getUser(@PathVariable("id") UUID id);
}
