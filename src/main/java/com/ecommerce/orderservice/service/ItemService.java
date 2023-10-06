package com.ecommerce.orderservice.service;

import java.util.List;

import com.ecommerce.sharedlibrary.model.ItemDto;

public interface ItemService {

	List<ItemDto> getAllItems();

	ItemDto getItemById(long id);

	ItemDto createItem(ItemDto item);

	ItemDto updateItem(long id, ItemDto item);

	void deleteItem(long id);
	
	void reduceProductStock(ItemDto item);
}
