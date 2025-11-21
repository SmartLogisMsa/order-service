package com.smartlogis.orderservice.interfaces.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogis.orderservice.TestMessageResolver;
import com.smartlogis.orderservice.application.service.OrderService;
import com.smartlogis.orderservice.interfaces.dto.request.CreateOrderRequest;
import com.smartlogis.orderservice.interfaces.dto.request.OrderItemRequest;
import com.smartlogis.orderservice.interfaces.dto.response.OrderResponse;

@WebMvcTest(controllers = OrderController.class,
	properties = {
		"spring.cloud.config.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false"
	})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OrderController API 테스트")
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	private CreateOrderRequest validRequest;
	private OrderResponse mockResponse;

	@BeforeEach
	void setUp() {
		TestMessageResolver.initializeMessageResource();

		UUID receiptCompanyId = UUID.randomUUID();
		UUID productId1 = UUID.randomUUID();
		UUID productId2 = UUID.randomUUID();

		validRequest = CreateOrderRequest.builder()
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("긴급 배송 요청")
			.orderItems(List.of(
				OrderItemRequest.builder()
					.productId(productId1)
					.quantity(10)
					.build(),
				OrderItemRequest.builder()
					.productId(productId2)
					.quantity(5)
					.build()
			)).build();

		mockResponse = OrderResponse.builder()
			.id(UUID.randomUUID())
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("긴급 배송 요청")
			.orderItems(List.of())
			.build();
	}

	@Test
	@DisplayName("정상적인 주문 생성 요청 시 201 Created 반환")
	void createOrder_Success() throws Exception {
		// given
		given(orderService.createOrder(any(CreateOrderRequest.class)))
			.willReturn(mockResponse);

		// when & then
		mockMvc.perform(post("/v1/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.id").exists())
			.andExpect(jsonPath("$.data.receiptCompanyId").value(mockResponse.getReceiptCompanyId().toString()))
			.andExpect(jsonPath("$.data.requestDetails").value("긴급 배송 요청"));

		then(orderService).should(times(1))
			.createOrder(any(CreateOrderRequest.class));
	}

	@Test
	@DisplayName("orderItems가 비어있을 때 400 Bad Request")
	void createOrder_BadRequest_EmptyOrderItems() throws Exception {
		// given
		CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
			.receiptCompanyId(UUID.randomUUID())
			.orderItems(List.of())
			.build();

		// when & then
		mockMvc.perform(post("/v1/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))
			.andDo(print())
			.andExpect(status().isBadRequest());

		then(orderService).should(never())
			.createOrder(any(CreateOrderRequest.class));
	}

	@Test
	@DisplayName("상품 수량이 0 이하일 때 400 Bad Request")
	void createOrder_BadRequest_InvalidQuantity() throws Exception {
		// given
		CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
			.receiptCompanyId(UUID.randomUUID())
			.orderItems(List.of(
				OrderItemRequest.builder()
					.productId(UUID.randomUUID())
					.quantity(0)
					.build()
			)).build();

		// when & then
		mockMvc.perform(post("/v1/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))
			.andDo(print())
			.andExpect(status().isBadRequest());

		then(orderService).should(never())
			.createOrder(any(CreateOrderRequest.class));
	}

	@Test
	@DisplayName("정상적인 주문 취소 요청 시 200 OK 반환")
	void cancelOrder_Success() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		UUID receiptCompanyId = UUID.randomUUID();

		OrderResponse cancelResponse = OrderResponse.builder()
			.id(orderId)
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("긴급 배송 요청")
			.orderItems(List.of())
			.build();

		given(orderService.cancelOrder(any(UUID.class)))
			.willReturn(cancelResponse);

		// when & then
		mockMvc.perform(delete("/v1/orders/{orderId}", orderId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(orderId.toString()))
			.andExpect(jsonPath("$.data.receiptCompanyId").value(receiptCompanyId.toString()))
			.andExpect(jsonPath("$.data.requestDetails").value("긴급 배송 요청"));

		then(orderService).should(times(1))
			.cancelOrder(any(UUID.class));
	}
}
