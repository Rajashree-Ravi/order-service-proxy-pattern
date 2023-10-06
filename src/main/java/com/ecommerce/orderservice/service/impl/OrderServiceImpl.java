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

import com.ecommerce.orderservice.entity.Item;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.feign.CustomerClient;
import com.ecommerce.orderservice.feign.ProductClient;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.ItemService;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.sharedlibrary.exception.EcommerceException;
import com.ecommerce.sharedlibrary.exception.NotFoundException;
import com.ecommerce.sharedlibrary.model.CustomerDto;
import com.ecommerce.sharedlibrary.model.ItemDto;
import com.ecommerce.sharedlibrary.model.OrderDto;
import com.ecommerce.sharedlibrary.model.OrderStatus;
import com.ecommerce.sharedlibrary.model.ProductDto;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ItemService itemService;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private ProductClient productClient;

	@Autowired
	private CustomerClient customerClient;

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

		// Check if all the product are available in stock
		checkProductStockAvailability(orderDto.getItems());

		// Check for customer information
		checkCustomer(orderDto.getUserId());

		// Create items
		orderDto = createItems(orderDto);

		Order order = mapper.map(orderDto, Order.class);

		return mapper.map(orderRepository.save(order), OrderDto.class);
	}

	@Override
	public OrderDto updateOrder(long id, OrderDto orderDto) {

		// Check if all products are available in stock
		checkProductStockAvailability(orderDto.getItems());

		// Check for customer information
		checkCustomer(orderDto.getUserId());

		Optional<Order> updatedOrder = orderRepository.findById(id).map(existingOrder -> {

			List<ItemDto> savedItems = new ArrayList<>();

			// Create new items added to order
			for (ItemDto itemDto : orderDto.getItems()) {
				if (itemDto.getId() == null) {
					savedItems.add(itemService.createItem(itemDto));
				} else {
					Item existingItem = existingOrder.getItems().stream().filter(x -> x.getId() == itemDto.getId())
							.findAny().orElse(null);

					if (existingItem == null || existingItem.getId() == null)
						savedItems.add(itemService.createItem(itemDto));
					else
						savedItems.add(itemService.updateItem(itemDto.getId(), itemDto));
				}

			}

			orderDto.setItems(savedItems);

			// Delete items that have been removed from order
			for (Item item : existingOrder.getItems()) {
				ItemDto existingItem = orderDto.getItems().stream().filter(x -> x.getId() == item.getId()).findAny()
						.orElse(null);

				if (existingItem != null && existingItem.getId() != null)
					itemService.deleteItem(existingItem.getId());
			}

			Order order = mapper.map(orderDto, Order.class);
			return orderRepository.save(existingOrder.updateWith(order));
		});

		return (updatedOrder.isPresent() ? mapper.map(updatedOrder.get(), OrderDto.class) : null);
	}

	@Override
	public void deleteOrder(long id) {

		OrderDto orderDto = getOrderById(id);

		if (orderDto == null || orderDto.getId() == null)
			throw new EcommerceException("order-not-found", String.format("Order with id=%d not found", id),
					HttpStatus.NOT_FOUND);

		// Do not delete an order if status is - INTRANSIT, PAYMENTDUE, PICKUPAVAILABLE,
		// PROCESSING
		if (orderDto.getStatus().equals(OrderStatus.INTRANSIT) || orderDto.getStatus().equals(OrderStatus.PAYMENTDUE)
				|| orderDto.getStatus().equals(OrderStatus.PICKUPAVAILABLE)
				|| orderDto.getStatus().equals(OrderStatus.PROCESSING))
			throw new EcommerceException("order-status-active", "Current Status of the Order with id = " + id + " is "
					+ orderDto.getStatus() + ", Hence it cannot be deleted.", HttpStatus.NOT_FOUND);

		// Remove the items in order before deleting the order
		for (ItemDto itemDto : orderDto.getItems()) {
			itemService.deleteItem(itemDto.getId());
		}

		orderRepository.deleteById(id);
		LOGGER.info("Order deleted Successfully");
	}

	private void checkProductStockAvailability(List<ItemDto> items) {
		for (ItemDto item : items) {
			ProductDto product = productClient.getProductById(item.getProductId());
			if (product == null || product.getId() == null)
				throw new NotFoundException("Product with id = " + item.getProductId() + " not found.");

			if (product.getAvailability() == 0)
				throw new EcommerceException("product-not-available",
						"Product with id: " + item.getProductId() + " is/are not available in stock.",
						HttpStatus.NOT_FOUND);

			if (product.getAvailability() < item.getQuantity())
				throw new EcommerceException("product-not-available", "Only " + product.getAvailability()
						+ " units of product - id: " + item.getProductId() + " is/are available.",
						HttpStatus.NOT_FOUND);
		}
	}

	private void checkCustomer(long customerId) {
		CustomerDto customer = customerClient.getCustomerById(customerId);
		if (customer == null || customer.getId() == null)
			throw new NotFoundException("Customer with id = " + customerId + " not found.");
	}

	private OrderDto createItems(OrderDto orderDto) {
		List<ItemDto> savedItems = new ArrayList<>();

		// Create new items added to order
		for (ItemDto itemDto : orderDto.getItems()) {
			savedItems.add(itemService.createItem(itemDto));
		}
		orderDto.setItems(savedItems);

		return orderDto;
	}
}
