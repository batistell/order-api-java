package com.batistell.orderapi.service;

import com.batistell.orderapi.client.CatalogFeignClient;
import com.batistell.orderapi.client.InventoryFeignClient;
import com.batistell.orderapi.dto.CatalogProductDto;
import com.batistell.orderapi.dto.InventoryDto;
import com.batistell.orderapi.dto.OrderItemRequest;
import com.batistell.orderapi.exception.InsufficientStockException;
import com.batistell.orderapi.exception.PriceChangedException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private final CatalogFeignClient catalogClient;
    private final InventoryFeignClient inventoryClient;

    public void validateOrderItem(OrderItemRequest item) {
        log.info("Validating item: {}", item.getProductId());

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<CatalogProductDto> priceTask = scope.fork(() -> getCatalogProductWrapped(item.getProductId()).join());
            StructuredTaskScope.Subtask<InventoryDto> stockTask = scope.fork(() -> getInventoryWrapped(item.getProductId()).join());

            scope.join().throwIfFailed(); // Joins both virtual threads, throws if any failed

            validatePrice(priceTask.get(), item);
            validateStock(stockTask.get(), item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Validation interrupted", e);
        } catch (Exception e) {
            if (e instanceof PriceChangedException || e instanceof InsufficientStockException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = "catalogDB")
    @TimeLimiter(name = "catalogDB")
    @Bulkhead(name = "catalogDB")
    public CompletableFuture<CatalogProductDto> getCatalogProductWrapped(String productId) {
        return CompletableFuture.supplyAsync(() -> catalogClient.getProductById(productId));
    }

    @CircuitBreaker(name = "inventoryDB")
    @TimeLimiter(name = "inventoryDB")
    @Bulkhead(name = "inventoryDB")
    public CompletableFuture<InventoryDto> getInventoryWrapped(String productId) {
        return CompletableFuture.supplyAsync(() -> inventoryClient.getInventory(productId));
    }

    private void validatePrice(CatalogProductDto product, OrderItemRequest item) {
        if (product == null) {
            throw new RuntimeException("Product not found: " + item.getProductId());
        }
        if (product.getPrice().compareTo(item.getExpectedPrice()) != 0) {
            throw new PriceChangedException(String.format("Price changed for product %s. Expected: %s, Actual: %s",
                    item.getProductId(), item.getExpectedPrice(), product.getPrice()));
        }
    }

    private void validateStock(InventoryDto inventory, OrderItemRequest item) {
        if (inventory == null) {
            throw new InsufficientStockException("Inventory not found for product: " + item.getProductId());
        }
        if (inventory.getQuantity() < item.getQuantity()) {
            throw new InsufficientStockException(String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                    item.getProductId(), item.getQuantity(), inventory.getQuantity()));
        }
    }
}
