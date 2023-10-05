package com.ecommerce.orderservice.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.sharedlibrary.exception.EcommerceException;
import com.ecommerce.sharedlibrary.model.OrderDto;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ModelMapper mapper;

	@Override
	public List<OrderDto> getAllOrders() {
		List<OrderDto> orders = new ArrayList<>();
		orderRepository.findAll().forEach(order -> {
			orders.add(mapper.map(order, OrderDto.class));
		});
		return orders;
	}

	@Override
	public OrderDto getOrderById(long id) {
		Optional<Order> order = orderRepository.findById(id);
		return (order.isPresent() ? mapper.map(order.get(), OrderDto.class) : null);
	}

	@Override
	public OrderDto createOrder(OrderDto orderDto) {
		Order order = mapper.map(orderDto, Order.class);
		return mapper.map(orderRepository.save(order), OrderDto.class);
	}

	@Override
	public OrderDto updateOrder(long id, OrderDto orderDto) {
		Optional<Order> updatedOrder = orderRepository.findById(id).map(existingOrder -> {
			Order order = mapper.map(orderDto, Order.class);
			return orderRepository.save(existingOrder.updateWith(order));
		});

		return (updatedOrder.isPresent() ? mapper.map(updatedOrder.get(), OrderDto.class) : null);
	}

	@Override
	public void deleteOrder(long id) {
		// Do not delete an order if status is - INTRANSIT, PAYMENTDUE, PICKUPAVAILABLE,
		// PROCESSING

		// Remove the items in order before deleting the order

		if (getOrderById(id) != null) {
			orderRepository.deleteById(id);
			LOGGER.info("Order deleted Successfully");
		} else {
			throw new EcommerceException("order-not-found", String.format("Order with id=%d not found", id),
					HttpStatus.NOT_FOUND);
		}
	}
}
