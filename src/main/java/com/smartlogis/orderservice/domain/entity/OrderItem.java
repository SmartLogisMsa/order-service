package com.smartlogis.orderservice.domain.entity;

import java.util.UUID;

import com.smartlogis.common.domain.AbstractEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderItem extends AbstractEntity {
	@Id
	@Column(name = "id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Builder
	public static OrderItem create(Order order, UUID productId, Integer quantity) {
		OrderItem item = new OrderItem();
		item.id = UUID.randomUUID();
		item.order = order;
		item.productId = productId;
		item.quantity = quantity;
		return item;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
