package com.batistell.orderapi.dto;

import com.batistell.orderapi.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private String customerId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private OffsetDateTime createdAt;
}
