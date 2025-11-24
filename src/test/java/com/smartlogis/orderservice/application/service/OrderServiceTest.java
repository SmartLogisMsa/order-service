package com.smartlogis.orderservice.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.smartlogis.common.presentation.ApiResponse;
import com.smartlogis.common.presentation.dto.PageRequest;
import com.smartlogis.common.presentation.dto.PageResponse;
import com.smartlogis.orderservice.TestMessageResolver;
import com.smartlogis.orderservice.domain.entity.Order;
import com.smartlogis.orderservice.domain.entity.OrderItem;
import com.smartlogis.orderservice.domain.entity.OrderStatus;
import com.smartlogis.orderservice.domain.event.OrderCanceledEvent;
import com.smartlogis.orderservice.domain.event.OrderCreatedEvent;
import com.smartlogis.orderservice.domain.exception.InsufficientInventoryException;
import com.smartlogis.orderservice.domain.exception.OrderCannotBeCanceledException;
import com.smartlogis.orderservice.domain.exception.OrderNotFoundException;
import com.smartlogis.orderservice.domain.repository.OrderRepository;
import com.smartlogis.orderservice.infrastructure.client.CompanyClient;
import com.smartlogis.orderservice.infrastructure.client.ProductServiceClient;
import com.smartlogis.orderservice.infrastructure.client.UserServiceClient;
import com.smartlogis.orderservice.infrastructure.client.dto.CompanyResponse;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckRequest;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckResponse;
import com.smartlogis.orderservice.infrastructure.event.publisher.OrderEventPublisher;
import com.smartlogis.orderservice.interfaces.dto.request.CreateOrderRequest;
import com.smartlogis.orderservice.interfaces.dto.request.OrderItemRequest;
import com.smartlogis.orderservice.interfaces.dto.response.OrderResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

	@InjectMocks
	private OrderService orderService;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductServiceClient productServiceClient;

	@Mock
	private OrderEventPublisher orderEventPublisher;

	@Mock
	private CompanyClient companyClient;

	@Mock
	private UserServiceClient userServiceClient;

	private UUID receiptCompanyId;
	private UUID productId1;
	private UUID productId2;
	private UUID userId;
	private CreateOrderRequest request;

	@BeforeEach
	void setUp() {
		TestMessageResolver.initializeMessageResource();
		receiptCompanyId = UUID.randomUUID();
		productId1 = UUID.randomUUID();
		productId2 = UUID.randomUUID();
		userId = UUID.randomUUID();

		request = CreateOrderRequest.builder()
			.receiptCompanyId(receiptCompanyId)
			.requestDetails("긴급 배송 요청")
			.orderItems(List.of(
				createOrderItemRequest(productId1, 10),
				createOrderItemRequest(productId2, 5)
			))
			.build();

		CompanyResponse companyResponse = new CompanyResponse(
			receiptCompanyId,
			"테스트 업체",
			"서울시 강남구",
			UUID.randomUUID(),
			UUID.randomUUID()
		);
		ApiResponse<CompanyResponse> apiResponse = ApiResponse.successWithDataOnly(companyResponse);
		lenient().when(companyClient.getCompany(any(UUID.class))).thenReturn(apiResponse);

		// 사용자 정보 조회 모킹
		com.smartlogis.orderservice.infrastructure.client.dto.UserInfoResponse userInfo =
			com.smartlogis.orderservice.infrastructure.client.dto.UserInfoResponse.builder()
				.id(userId)
				.firstName("테스트")
				.lastName("주문자")
				.email("test@example.com")
				.build();
		lenient().when(userServiceClient.getCurrentUser())
			.thenReturn(ApiResponse.successWithDataOnly(userInfo));
	}

	@Test
	@DisplayName("재고가 충분할 때 주문 생성 성공")
	void createOrder_Success_WhenInventoryAvailable() {
		// given
		mockInventoryCheckResponse(true, true);

		given(orderRepository.save(any(Order.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		OrderResponse response = orderService.createOrder(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getReceiptCompanyId()).isEqualTo(receiptCompanyId);
		assertThat(response.getOrderItems()).hasSize(2);

		verifyOrderCreationSuccess();
	}

	@Test
	@DisplayName("일부 재고가 부족할 때 InsufficientInventoryException 발생")
	void createOrder_ThrowsException_WhenSomeInventoryInsufficient() {
		// given
		mockInventoryCheckResponse(true, false);

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(request))
			.isInstanceOf(InsufficientInventoryException.class);

		verifyOrderCreationFailed();
	}

	@Test
	@DisplayName("모든 재고가 부족할 때 InsufficientInventoryException 발생")
	void createOrder_ThrowsException_WhenAllInventoriesInsufficient() {
		// given
		mockInventoryCheckResponse(false, false);

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(request))
			.isInstanceOf(InsufficientInventoryException.class);

		verifyOrderCreationFailed();
	}

	@Test
	@DisplayName("PENDING 상태의 주문을 취소하면 OrderCanceledEvent 발행")
	void cancelOrder_Success_WhenOrderExists() {
		// given
		UUID orderId = UUID.randomUUID();
		Order mockOrder = Order.create(
			receiptCompanyId,
			"테스트",
			List.of(OrderItem.create(null, productId1, "상품1", 10)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);

		given(orderRepository.findById(orderId))
			.willReturn(Optional.of(mockOrder));

		given(orderRepository.save(any(Order.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		OrderResponse response = orderService.cancelOrder(orderId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(mockOrder.getId());

		then(orderRepository).should(times(1))
			.findById(orderId);
		then(orderRepository).should(times(1))
			.save(any(Order.class));
		then(orderEventPublisher).should(times(1))
			.publishOrderCanceled(any(OrderCanceledEvent.class));
	}

	@Test
	@DisplayName("존재하지 않는 주문 ID로 취소 시 OrderNotFoundException 발생")
	void cancelOrder_ThrowsException_WhenOrderNotFound() {
		// given
		UUID orderId = UUID.randomUUID();

		given(orderRepository.findById(orderId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(orderId))
			.isInstanceOf(OrderNotFoundException.class);

		then(orderRepository).should(never())
			.save(any(Order.class));
		then(orderEventPublisher).should(never())
			.publishOrderCanceled(any(OrderCanceledEvent.class));
	}

	@Test
	@DisplayName("SHIPPED 상태의 주문을 취소하면 OrderCannotBeCanceledException 발생")
	void cancelOrder_ThrowsException_WhenOrderStatusIsShipped() {
		// given
		UUID orderId = UUID.randomUUID();
		Order shippedOrder = Order.create(
			receiptCompanyId,
			"테스트",
			List.of(OrderItem.create(null, productId1, "상품1", 10)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);
		setOrderStatus(shippedOrder, OrderStatus.SHIPPED);

		given(orderRepository.findById(orderId))
			.willReturn(Optional.of(shippedOrder));

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(orderId))
			.isInstanceOf(OrderCannotBeCanceledException.class);

		then(orderRepository).should(never())
			.save(any(Order.class));
		then(orderEventPublisher).should(never())
			.publishOrderCanceled(any(OrderCanceledEvent.class));
	}

	@Test
	@DisplayName("DELIVERED 상태의 주문을 취소하면 OrderCannotBeCanceledException 발생")
	void cancelOrder_ThrowsException_WhenOrderStatusIsDelivered() {
		// given
		UUID orderId = UUID.randomUUID();
		Order shippedOrder = Order.create(
			receiptCompanyId,
			"테스트",
			List.of(OrderItem.create(null, productId1, "상품1", 10)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);
		setOrderStatus(shippedOrder, OrderStatus.DELIVERED);

		given(orderRepository.findById(orderId))
			.willReturn(Optional.of(shippedOrder));

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(orderId))
			.isInstanceOf(OrderCannotBeCanceledException.class);

		then(orderRepository).should(never())
			.save(any(Order.class));
		then(orderEventPublisher).should(never())
			.publishOrderCanceled(any(OrderCanceledEvent.class));
	}

	@Test
	@DisplayName("존재하는 주문을 논리적 삭제 성공")
	void deleteOrder_Success_WhenOrderExists() {
		// given
		UUID orderId = UUID.randomUUID();
		Order mockOrder = Order.create(
			receiptCompanyId,
			"테스트",
			List.of(OrderItem.create(null, productId1, "상품1", 10)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);

		given(orderRepository.findByIdAndDeletedAtIsNull(orderId))
			.willReturn(Optional.of(mockOrder));

		// when
		orderService.deleteOrder(orderId);

		// then
		then(orderRepository).should(times(1))
			.findByIdAndDeletedAtIsNull(orderId);
		then(orderRepository).should(times(1))
			.save(any(Order.class));
	}

	@Test
	@DisplayName("존재하지 않는 주문 삭제 시 OrderNotFoundException 발생")
	void deleteOrder_ThrowsException_WhenOrderNotFound() {
		// given
		UUID orderId = UUID.randomUUID();

		given(orderRepository.findByIdAndDeletedAtIsNull(orderId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.deleteOrder(orderId))
			.isInstanceOf(OrderNotFoundException.class);

		then(orderRepository).should(never())
			.save(any(Order.class));
	}

	@Test
	@DisplayName("이미 삭제된 주문 삭제 시 OrderNotFoundException 발생")
	void deleteOrder_ThrowsException_WhenAlreadyDeleted() {
		// given
		UUID orderId = UUID.randomUUID();

		given(orderRepository.findByIdAndDeletedAtIsNull(orderId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.deleteOrder(orderId))
			.isInstanceOf(OrderNotFoundException.class);
	}

	@Test
	@DisplayName("존재하는 주문 단건 조회 성공")
	void getOrder_Success_WhenOrderExists() {
		// given
		UUID orderId = UUID.randomUUID();
		Order mockOrder = Order.create(
			receiptCompanyId,
			"테스트",
			List.of(OrderItem.create(null, productId1, "상품1", 10)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);

		given(orderRepository.findByIdAndDeletedAtIsNull(orderId))
			.willReturn(Optional.of(mockOrder));

		// when
		OrderResponse response = orderService.getOrder(orderId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getReceiptCompanyId()).isEqualTo(receiptCompanyId);

		then(orderRepository).should(times(1))
			.findByIdAndDeletedAtIsNull(orderId);
	}

	@Test
	@DisplayName("존재하지 않는 주문 조회 시 OrderNotFoundException 발생")
	void getOrder_ThrowsException_WhenOrderNotFound() {
		// given
		UUID orderId = UUID.randomUUID();

		given(orderRepository.findByIdAndDeletedAtIsNull(orderId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.getOrder(orderId))
			.isInstanceOf(OrderNotFoundException.class);
	}

	@Test
	@DisplayName("업체별 주문 목록 조회 성공")
	void getOrdersByCompany_Success() {
		// given
		UUID companyId = UUID.randomUUID();

		Order order1 = Order.create(
			companyId,
			"주문1",
			List.of(OrderItem.create(null, productId1, "상품1", 10)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);
		Order order2 = Order.create(
			companyId,
			"주문2",
			List.of(OrderItem.create(null, productId2, "상품2", 5)),
			userId,
			"테스트 주문자",
			"test@example.com"
		);

		Page<Order> mockPage = new PageImpl<>(
			List.of(order1, order2),
			org.springframework.data.domain.PageRequest.of(0, 10),
			2
		);

		given(orderRepository.findByReceiptCompanyIdAndDeletedAtIsNull(
			eq(companyId),
			any(Pageable.class)))
			.willReturn(mockPage);

		PageRequest pageRequest =
			new PageRequest(0, 10, "createdAt", "DESC");

		// when
		PageResponse<OrderResponse> response = orderService.getOrdersByCompany(companyId, pageRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(2);
		assertThat(response.page()).isEqualTo(0);
		assertThat(response.size()).isEqualTo(10);
		assertThat(response.total()).isEqualTo(2);

		then(orderRepository).should(times(1))
			.findByReceiptCompanyIdAndDeletedAtIsNull(eq(companyId), any(Pageable.class));
	}

	@Test
	@DisplayName("업체에 주문이 없을 때 빈 리스트 반환")
	void getOrdersByCompany_ReturnsEmptyList_WhenNoOrders() {
		// given
		UUID companyId = UUID.randomUUID();
		Page<Order> emptyPage = new PageImpl<>(List.of());

		given(orderRepository.findByReceiptCompanyIdAndDeletedAtIsNull(
			eq(companyId),
			any(Pageable.class)))
			.willReturn(emptyPage);

		PageRequest pageRequest =
			new PageRequest(0, 10, "createdAt", "DESC");

		// when
		PageResponse<OrderResponse> response = orderService.getOrdersByCompany(companyId, pageRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.content()).isEmpty();
		assertThat(response.total()).isEqualTo(0);
	}

	private OrderItemRequest createOrderItemRequest(UUID productId, int quantity) {
		return OrderItemRequest.builder()
			.productId(productId)
			.quantity(quantity)
			.build();
	}

	private void mockInventoryCheckResponse(boolean product1Available, boolean product2Available) {
		InventoryCheckResponse response = InventoryCheckResponse.builder()
			.results(List.of(
				createInventoryCheckResult(productId1, product1Available),
				createInventoryCheckResult(productId2, product2Available)
			))
			.build();

		given(productServiceClient.checkInventories(any(InventoryCheckRequest.class)))
			.willReturn(response);
	}

	private InventoryCheckResponse.InventoryCheckResult createInventoryCheckResult(UUID productId, boolean available) {
		return InventoryCheckResponse.InventoryCheckResult.builder()
			.productId(productId)
			.available(available)
			.build();
	}

	private void verifyOrderCreationSuccess() {
		then(productServiceClient).should(times(1))
			.checkInventories(any(InventoryCheckRequest.class));
		then(orderRepository).should(times(1))
			.save(any(Order.class));
		then(orderEventPublisher).should(times(1))
			.publishOrderCreated(any(OrderCreatedEvent.class));
	}

	private void verifyOrderCreationFailed() {
		then(productServiceClient).should(times(1))
			.checkInventories(any(InventoryCheckRequest.class));
		then(orderRepository).should(never())
			.save(any(Order.class));
		then(orderEventPublisher).should(never())
			.publishOrderCreated(any(OrderCreatedEvent.class));
	}

	private void setOrderStatus(Order order, OrderStatus status) {
		try {
			Field statusField = Order.class.getDeclaredField("status");
			statusField.setAccessible(true);
			statusField.set(order, status);
		} catch (NoSuchFieldException | IllegalAccessException exception) {
			throw new RuntimeException("Failed to set order status", exception);
		}
	}
}