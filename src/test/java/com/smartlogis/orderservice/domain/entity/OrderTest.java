package com.smartlogis.orderservice.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartlogis.orderservice.TestMessageResolver;
import com.smartlogis.orderservice.domain.exception.OrderCannotBeCanceledException;

@DisplayName("Order 도메인 테스트")
class OrderTest {

	private UUID receiptCompanyId;
	private List<OrderItem> orderItems;

	@BeforeEach
	void setUp() {
		TestMessageResolver.initializeMessageResource();
		receiptCompanyId = UUID.randomUUID();
		orderItems = List.of(
			OrderItem.create(null, UUID.randomUUID(), 10),
			OrderItem.create(null, UUID.randomUUID(), 5)
		);
	}

	@Test
	@DisplayName("PENDING 상태의 주문은 취소할 수 있다")
	void cancel_Success_WhenStatusIsPending() {
		// given
		Order order = Order.create(receiptCompanyId, "긴급 배송", orderItems);

		// when
		order.cancel();

		// then
		assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
	}

	@Test
	@DisplayName("SHIPPED 상태의 주문은 취소할 수 없다")
	void cancel_ThrowsException_WhenStatusIsShipped() {
		// given
		Order order = Order.create(receiptCompanyId, "긴급 배송", orderItems);
		setOrderStatus(order, OrderStatus.SHIPPED);

		// when & then
		assertThatThrownBy(() -> order.cancel())
			.isInstanceOf(OrderCannotBeCanceledException.class);
	}

	@Test
	@DisplayName("DELIVERED 상태의 주문은 취소할 수 없다")
	void cancel_ThrowsException_WhenStatusIsDelivered() {
		// given
		Order order = Order.create(receiptCompanyId, "긴급 배송", orderItems);
		setOrderStatus(order, OrderStatus.DELIVERED);

		// when & then
		assertThatThrownBy(() -> order.cancel())
			.isInstanceOf(OrderCannotBeCanceledException.class);
	}

	@Test
	@DisplayName("CANCELED 상태의 주문은 취소할 수 없다")
	void cancel_ThrowsException_WhenStatusIsCanceled() {
		// given
		Order order = Order.create(receiptCompanyId, "긴급 배송", orderItems);
		setOrderStatus(order, OrderStatus.CANCELED);

		// when & then
		assertThatThrownBy(() -> order.cancel())
			.isInstanceOf(OrderCannotBeCanceledException.class);
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