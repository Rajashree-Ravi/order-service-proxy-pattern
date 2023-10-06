package com.ecommerce.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.orderservice.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
