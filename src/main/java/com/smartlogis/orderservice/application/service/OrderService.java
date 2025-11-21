package com.smartlogis.orderservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlogis.common.presentation.dto.PageRequest;
import com.smartlogis.common.presentation.dto.PageResponse;
import com.smartlogis.orderservice.domain.entity.Order;
import com.smartlogis.orderservice.domain.entity.OrderItem;
import com.smartlogis.orderservice.domain.event.OrderCanceledEvent;
import com.smartlogis.orderservice.domain.event.OrderCreatedEvent;
import com.smartlogis.orderservice.domain.exception.InsufficientInventoryException;
import com.smartlogis.orderservice.domain.exception.OrderMessageCode;
import com.smartlogis.orderservice.domain.exception.OrderNotFoundException;
import com.smartlogis.orderservice.domain.repository.OrderRepository;
import com.smartlogis.orderservice.infrastructure.client.ProductServiceClient;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckRequest;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckResponse;
import com.smartlogis.orderservice.infrastructure.event.publisher.OrderEventPublisher;
import com.smartlogis.orderservice.interfaces.dto.request.CreateOrderRequest;
import com.smartlogis.orderservice.interfaces.dto.response.OrderResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductServiceClient productServiceClient;
	private final OrderEventPublisher orderEventPublisher;

	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		List<OrderItem> orderItems = request.getOrderItems().stream()
			.map(itemRequest -> OrderItem.create(null, itemRequest.getProductId(), itemRequest.getQuantity()))
			.toList();

		InventoryCheckRequest inventoryCheckRequest = InventoryCheckRequest.builder()
			.inventoryChecks(orderItems.stream()
				.map(item -> InventoryCheckRequest.InventoryCheckItem.builder()
					.productId(item.getProductId())
					.requestedQuantity(item.getQuantity())
					.build())
				.toList())
			.build();

		InventoryCheckResponse inventoryCheckResponse = productServiceClient.checkInventories(inventoryCheckRequest);

		boolean available = inventoryCheckResponse.getResults().stream()
			.allMatch(InventoryCheckResponse.InventoryCheckResult::getAvailable);

		if (!available) {
			throw new InsufficientInventoryException(OrderMessageCode.ORDER_INSUFFICIENT_INVENTORY);
		}

		Order order = Order.create(request.getReceiptCompanyId(), request.getRequestDetails(), orderItems);

		Order savedOrder = orderRepository.save(order);

		OrderCreatedEvent event = OrderCreatedEvent.from(savedOrder);
		orderEventPublisher.publishOrderCreated(event);

		return OrderResponse.from(savedOrder);
	}

	@Transactional
	public OrderResponse cancelOrder(UUID orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new OrderNotFoundException(OrderMessageCode.ORDER_NOT_FOUND));

		order.cancel();

		Order savedOrder = orderRepository.save(order);

		OrderCanceledEvent event = OrderCanceledEvent.from(savedOrder);
		orderEventPublisher.publishOrderCanceled(event);

		return OrderResponse.from(savedOrder);
	}

	@Transactional
	public OrderResponse deleteOrder(UUID orderId) {
		Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new OrderNotFoundException(OrderMessageCode.ORDER_NOT_FOUND));

		order.delete();

		Order savedOrder = orderRepository.save(order);

		return OrderResponse.from(savedOrder);
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(UUID orderId) {
		Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new OrderNotFoundException(OrderMessageCode.ORDER_NOT_FOUND));

		return OrderResponse.from(order);
	}

	@Transactional(readOnly = true)
	public PageResponse<OrderResponse> getOrderByCompany(UUID receiptCompanyId, PageRequest pageRequest) {
		Sort.Direction direction = Sort.Direction.fromString(
			pageRequest.getDirection() != null ? pageRequest.getDirection() : "DESC"
		);
		String sortBy = pageRequest.getSortBy() != null ? pageRequest.getSortBy() : "createdAt";

		Pageable pageable = org.springframework.data.domain.PageRequest.of(
			pageRequest.getPage(),
			pageRequest.getSize(),
			Sort.by(direction, sortBy)
		);

		Page<Order> orders = orderRepository.findByReceiptCompanyIdAndDeletedAtIsNull(receiptCompanyId, pageable);

		return PageResponse.from(orders.map(OrderResponse::from));
	}
}
