package com.ecommerce.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ecommerce.sharedlibrary.model.CustomerDto;

@FeignClient(name = "customer-service", url = "http://localhost:8080/api/customers")
public interface CustomerClient {

	@GetMapping("/{id}")
	CustomerDto getCustomerById(@PathVariable("id") Long id);
}
