package com.smartlogis.orderservice.infrastructure.client.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
	private UUID id;
	private String name;
	private String address;
	private UUID managerId;
	private UUID hubId;
}
