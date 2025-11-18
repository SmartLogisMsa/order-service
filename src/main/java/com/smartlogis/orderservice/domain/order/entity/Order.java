package com.smartlogis.orderservice.domain.order.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.smartlogis.common.domain.AbstractEntity;
import com.smartlogis.orderservice.domain.order.exception.InvalidOrderItemException;
import com.smartlogis.orderservice.domain.order.exception.OrderMessageCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order extends AbstractEntity {
	@Id
	@Column(name = "id")
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderStatus status;

	@Column(name = "receipt_company_id", nullable = false)
	private UUID receiptCompanyId;

	@Column(name = "delivery_id")
	private UUID deliveryId;

	@Column(name = "request_details", length = 100)
	private String requestDetails;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<OrderItem> orderItems = new ArrayList<>();

	@Builder
	public static Order create(UUID receiptCompanyId, String requestDetails, List<OrderItem> orderItems) {
		if (orderItems == null || orderItems.isEmpty()) {
			throw new InvalidOrderItemException(OrderMessageCode.ORDER_ITEMS_REQUIRED);
		}
		Order order = new Order();
		order.id = UUID.randomUUID();
		order.status = OrderStatus.CREATED;
		order.receiptCompanyId = receiptCompanyId;
		order.requestDetails = requestDetails;
		order.orderItems = new ArrayList<>(orderItems);
		orderItems.forEach(item -> item.setOrder(order));
		return order;
	}

	public void cancel() {
		this.status = OrderStatus.CANCELED;
	}

	public void setDeliveryId(UUID deliveryId) {
		this.deliveryId = deliveryId;
	}

	public List<OrderItem> getActiveOrderItems() {
		return orderItems.stream()
			.filter(item -> item.getDeletedAt() == null)
			.collect(Collectors.toList());
	}
}
