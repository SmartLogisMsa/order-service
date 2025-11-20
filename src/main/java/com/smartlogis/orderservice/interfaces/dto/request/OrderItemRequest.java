package com.smartlogis.orderservice.interfaces.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
	@NotNull(message = "상품 ID는 필수입니다")
	private UUID productId;

	@NotNull(message = "수량은 필수입니다")
	@Min(value = 1, message = "수량은 1개 이상이어야 합니다")
	private Integer quantity;
}
