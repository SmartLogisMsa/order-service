package com.smartlogis.orderservice.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlogis.orderservice.domain.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
	List<OrderItem> findByOrderIdAndDeletedAtIsNull(UUID orderId);

	long countByOrderIdAndDeletedAtIsNull(UUID orderId);
}
