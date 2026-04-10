package com.batistell.orderapi.service;

import com.batistell.orderapi.dto.OrderItemRequest;
import com.batistell.orderapi.dto.OrderRequest;
import com.batistell.orderapi.dto.OrderResponse;
import com.batistell.orderapi.model.Order;
import com.batistell.orderapi.model.OrderItem;
import com.batistell.orderapi.model.OutboxEvent;
import com.batistell.orderapi.repository.OrderRepository;
import com.batistell.orderapi.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderValidationService validationService;
    private final IdempotencyService idempotencyService;
    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String idempotencyKey) {
        log.info("Placing order for customer: {}", request.getCustomerId());
        
        // 1. Check Idempotency. (Throws exception with cached order UUID if already processed)
        idempotencyService.checkIdempotency(idempotencyKey);

        // 2. Validate Items (Structured Concurrency - price and stock checks)
        BigDecimal totalPrice = BigDecimal.ZERO;
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            validationService.validateOrderItem(itemReq);
            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getExpectedPrice())
                    .build();
            order.addItem(item);
            totalPrice = totalPrice.add(itemReq.getExpectedPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }
        order.setTotalPrice(totalPrice);

        // 3. Persist Order
        Order savedOrder = orderRepository.save(order);

        // 4. Save Idempotency Key
        idempotencyService.saveIdempotencyKey(idempotencyKey, savedOrder.getId());

        // 5. Create Outbox Event (Atomic with Order Creation)
        String payload = String.format("{\"orderId\":\"%s\",\"status\":\"%s\"}", savedOrder.getId(), savedOrder.getStatus());
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(savedOrder.getId().toString())
                .eventType("OrderCreated")
                .payload(payload)
                .build();
        outboxEventRepository.save(outboxEvent);

        log.info("Order placed successfully. Order ID: {}", savedOrder.getId());

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .status(savedOrder.getStatus())
                .totalPrice(savedOrder.getTotalPrice())
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
