package com.ecommerce.orderservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.sharedlibrary.exception.EcommerceException;
import com.ecommerce.sharedlibrary.model.OrderDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(produces = "application/json", value = "Operations pertaining to manage orders in e-commerce application")
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	OrderService orderService;

	@GetMapping
	@ApiOperation(value = "View all orders", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved all orders"),
			@ApiResponse(code = 204, message = "Orders list is empty"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	private ResponseEntity<List<OrderDto>> getAllOrders() {

		List<OrderDto> orders = orderService.getAllOrders();
		if (orders.isEmpty())
			throw new EcommerceException("no-content", "Orders list is empty", HttpStatus.NO_CONTENT);

		return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	@ApiOperation(value = "Retrieve specific order with the specified order id", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved order with the order id"),
			@ApiResponse(code = 404, message = "Order with specified order id not found"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	private ResponseEntity<OrderDto> getOrderById(@PathVariable("id") long id) {

		OrderDto order = orderService.getOrderById(id);
		if (order != null)
			return new ResponseEntity<>(order, HttpStatus.OK);
		else
			throw new EcommerceException("order-not-found", String.format("Order with id=%d not found", id),
					HttpStatus.NOT_FOUND);
	}

	@PostMapping
	@ApiOperation(value = "Create a new order", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Successfully created a order"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto order) {
		return new ResponseEntity<>(orderService.createOrder(order), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@ApiOperation(value = "Update a order information", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated order information"),
			@ApiResponse(code = 404, message = "Order with specified order id not found"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	public ResponseEntity<OrderDto> updateOrder(@PathVariable("id") long id, @RequestBody OrderDto order) {

		OrderDto updatedOrder = orderService.updateOrder(id, order);
		if (updatedOrder != null)
			return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
		else
			throw new EcommerceException("order-not-found", String.format("Order with id=%d not found", id),
					HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{id}")
	@ApiOperation(value = "Delete a order", response = ResponseEntity.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Successfully deleted order information"),
			@ApiResponse(code = 500, message = "Application failed to process the request") })
	private ResponseEntity<String> deleteOrder(@PathVariable("id") long id) {

		orderService.deleteOrder(id);
		return new ResponseEntity<>("Order deleted successfully", HttpStatus.NO_CONTENT);
	}
}
