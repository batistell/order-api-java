package com.batistell.orderapi.client;

import com.batistell.orderapi.dto.CatalogProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-api", url = "${catalog.api.url}")
public interface CatalogFeignClient {

    @GetMapping("/api/catalog/products/{id}")
    CatalogProductDto getProductById(@PathVariable("id") String id);
}
