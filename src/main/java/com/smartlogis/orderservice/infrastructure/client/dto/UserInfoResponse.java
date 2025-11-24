package com.smartlogis.orderservice.infrastructure.client.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
	private UUID id;
	private String firstName;
	private String lastName;
	private String email;

	public String getFullName() {
		return firstName + " " + lastName;
	}
}
