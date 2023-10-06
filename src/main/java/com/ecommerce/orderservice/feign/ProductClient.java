package com.ecommerce.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.sharedlibrary.model.ProductDto;

@FeignClient(name = "product-service", url = "http://localhost:8080/api/products")
public interface ProductClient {

	@GetMapping("/{id}")
	ProductDto getProductById(@PathVariable("id") Long id);
	
	@PutMapping("/{id}")
	ProductDto updateProductById(@PathVariable("id") Long id, @RequestBody ProductDto product);
}
