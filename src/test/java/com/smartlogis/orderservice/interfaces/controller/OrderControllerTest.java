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
import com.smartlogis.common.presentation.dto.PageRequest;
import com.smartlogis.common.presentation.dto.PageResponse;
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
			))
			.ordererId(UUID.randomUUID())
			.ordererName("테스트 주문자")
			.ordererEmail("test@example.com")
			.build();

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
		mockMvc.perform(patch("/v1/orders/cancel/{orderId}", orderId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(orderId.toString()))
			.andExpect(jsonPath("$.data.receiptCompanyId").value(receiptCompanyId.toString()))
			.andExpect(jsonPath("$.data.requestDetails").value("긴급 배송 요청"));

		then(orderService).should(times(1))
			.cancelOrder(any(UUID.class));
	}

	@Test
	@DisplayName("주문 단건 조회 성공 시 200 OK 반환")
	void getOrder_Success() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		UUID receiptCompanyId = UUID.randomUUID();

		OrderResponse response = OrderResponse.builder()
			.id(orderId)
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("긴급 배송 요청")
			.orderItems(List.of())
			.build();

		given(orderService.getOrder(any(UUID.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/v1/orders/{orderId}", orderId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(orderId.toString()))
			.andExpect(jsonPath("$.data.receiptCompanyId").value(receiptCompanyId.toString()))
			.andExpect(jsonPath("$.data.requestDetails").value("긴급 배송 요청"));

		then(orderService).should(times(1))
			.getOrder(any(UUID.class));
	}

	@Test
	@DisplayName("업체별 주문 목록 조회 성공 시 200 OK 반환")
	void getOrdersByCompany_Success() throws Exception {
		// given
		UUID receiptCompanyId = UUID.randomUUID();

		OrderResponse order1 = OrderResponse.builder()
			.id(UUID.randomUUID())
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("주문1")
			.orderItems(List.of())
			.build();

		OrderResponse order2 = OrderResponse.builder()
			.id(UUID.randomUUID())
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("주문2")
			.orderItems(List.of())
			.build();

		PageResponse<OrderResponse> pageResponse =
			new PageResponse<>(
				List.of(order1, order2),
				0,
				10,
				2L
			);

		given(orderService.getOrdersByCompany(
			any(UUID.class),
			any(PageRequest.class)))
			.willReturn(pageResponse);

		// when & then
		mockMvc.perform(get("/v1/orders/company/{receiptCompanyId}", receiptCompanyId)
				.param("page", "0")
				.param("size", "10")
				.param("sortBy", "createdAt")
				.param("direction", "DESC"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(2))
			.andExpect(jsonPath("$.data.page").value(0))
			.andExpect(jsonPath("$.data.size").value(10))
			.andExpect(jsonPath("$.data.total").value(2));

		then(orderService).should(times(1))
			.getOrdersByCompany(
				any(UUID.class),
				any(PageRequest.class));
	}

	@Test
	@DisplayName("주문 삭제 성공 시 200 OK 반환")
	void deleteOrder_Success() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();

		willDoNothing().given(orderService)
			.deleteOrder(any(UUID.class));

		// when & then
		mockMvc.perform(delete("/v1/orders/{orderId}", orderId))
			.andDo(print())
			.andExpect(status().isOk());

		then(orderService).should(times(1))
			.deleteOrder(any(UUID.class));
	}
}
