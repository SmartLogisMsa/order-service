package com.smartlogis.orderservice.interfaces.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
	@NotNull(message = "사용자 ID는 필수입니다")
	private UUID userId;

	@NotNull(message = "수령 업체 ID는 필수입니다")
	private UUID receiptCompanyId;

	@NotEmpty(message = "최소 1개 이상의 상품이 필요합니다")
	@Valid
	private List<OrderItemRequest> orderItems;

	private String requestDetails;
}
