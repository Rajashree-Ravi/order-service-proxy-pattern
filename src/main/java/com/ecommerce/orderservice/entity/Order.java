package com.ecommerce.orderservice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.ecommerce.sharedlibrary.model.OrderStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private LocalDate orderedDate;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@NotNull
	private BigDecimal total;

	@NotEmpty
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Item> items;

	@NotNull
	private Long userId;

	public Order updateWith(Order order) {
		return new Order(this.id, order.orderedDate, order.status, order.total, order.items, order.userId);
	}
}
