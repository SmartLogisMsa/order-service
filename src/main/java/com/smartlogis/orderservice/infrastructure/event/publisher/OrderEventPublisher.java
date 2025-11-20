package com.smartlogis.orderservice.infrastructure.event.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.smartlogis.orderservice.domain.event.OrderCreatedEvent;
import com.smartlogis.orderservice.infrastructure.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
	private final RabbitTemplate rabbitTemplate;

	public void publishOrderCreated(OrderCreatedEvent event) {
		rabbitTemplate.convertAndSend(
			RabbitMQConfig.ORDER_CREATED_EXCHANGE,
			RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
			event
		);
	}
}
