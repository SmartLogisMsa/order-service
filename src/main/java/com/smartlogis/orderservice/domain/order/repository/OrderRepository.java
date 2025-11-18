package com.smartlogis.orderservice.domain.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlogis.orderservice.domain.order.entity.Order;
import com.smartlogis.orderservice.domain.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {
	Optional<Order> findByIdAndDeletedAtIsNull(UUID id);

	Page<Order> findByReceiptCompanyIdAndDeletedAtIsNull(UUID receiptCompanyId, Pageable pageable);

	Optional<Order> findByDeliveryIdAndDeletedAtIsNull(UUID deliveryId);

	Page<Order> findByStatusAndDeletedAtIsNull(OrderStatus status, Pageable pageable);
}
