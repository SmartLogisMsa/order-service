package com.smartlogis.orderservice.domain.order.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlogis.orderservice.domain.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
	List<OrderItem> findByOrderIdAndDeletedAtIsNull(UUID orderId);

	long countByOrderIdAndDeletedAtIsNull(UUID orderId);
}
