package com.batistell.orderapi.client;

import com.batistell.orderapi.dto.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-api", url = "${inventory.api.url}")
public interface InventoryFeignClient {

    @GetMapping("/api/inventory/{productId}")
    InventoryDto getInventory(@PathVariable("productId") String productId);
}
