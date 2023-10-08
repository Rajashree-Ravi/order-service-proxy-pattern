package com.ecommerce.orderservice.service;

import java.util.List;

import com.ecommerce.orderservice.model.OrderDto;

public interface OrderService {

	List<OrderDto> getAllOrders();

	OrderDto getOrderById(long id);

	OrderDto createOrder(OrderDto order);

	OrderDto updateOrder(long id, OrderDto order);

	void deleteOrder(long id);
}
