package com.ecommerce.orderservice.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.orderservice.entity.Item;
import com.ecommerce.orderservice.feign.ProductClient;
import com.ecommerce.orderservice.repository.ItemRepository;
import com.ecommerce.orderservice.service.ItemService;
import com.ecommerce.sharedlibrary.model.ItemDto;
import com.ecommerce.sharedlibrary.model.ProductDto;

@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	ProductClient productClient;

	@Autowired
	private ModelMapper mapper;

	@Override
	public List<ItemDto> getAllItems() {
		List<ItemDto> items = new ArrayList<>();
		itemRepository.findAll().forEach(item -> {
			items.add(mapper.map(item, ItemDto.class));
		});
		return items;
	}

	@Override
	public ItemDto getItemById(long id) {
		Optional<Item> item = itemRepository.findById(id);
		return (item.isPresent() ? mapper.map(item.get(), ItemDto.class) : null);
	}

	@Override
	public ItemDto createItem(ItemDto itemDto) {
		// Reduce the product quantity
		reduceProductStock(itemDto);

		Item item = mapper.map(itemDto, Item.class);
		return mapper.map(itemRepository.save(item), ItemDto.class);
	}

	@Override
	public ItemDto updateItem(long id, ItemDto itemDto) {

		Optional<Item> updatedItem = itemRepository.findById(id).map(existingItem -> {

			// Update the product quantity according to the change
			if (itemDto.getQuantity() > existingItem.getQuantity())
				reduceProductStock(itemDto, itemDto.getQuantity() - existingItem.getQuantity());
			else if (itemDto.getQuantity() < existingItem.getQuantity())
				increaseProductStock(itemDto, existingItem.getQuantity() + itemDto.getQuantity());

			Item item = mapper.map(itemDto, Item.class);
			return itemRepository.save(existingItem.updateWith(item));
		});

		return (updatedItem.isPresent()) ? mapper.map(updatedItem.get(), ItemDto.class) : null;
	}

	@Override
	public void deleteItem(long id) {
		ItemDto existingItem = getItemById(id);

		// Increase the product quantity if product exists
		increaseProductStock(existingItem);

		itemRepository.deleteById(id);
	}

	private void increaseProductStock(ItemDto itemDto) {
		ProductDto existingProduct = productClient.getProductById(itemDto.getProductId());
		if (existingProduct != null) {
			int quantity = existingProduct.getAvailability() + itemDto.getQuantity();
			existingProduct.setAvailability(quantity);
			productClient.updateProductById(itemDto.getProductId(), existingProduct);
		}
	}

	private void increaseProductStock(ItemDto itemDto, int quantity) {
		ProductDto existingProduct = productClient.getProductById(itemDto.getProductId());
		if (existingProduct != null) {
			int quant = existingProduct.getAvailability() + quantity;
			existingProduct.setAvailability(quant);
			productClient.updateProductById(itemDto.getProductId(), existingProduct);
		}
	}

	@Override
	public void reduceProductStock(ItemDto itemDto) {
		ProductDto existingProduct = productClient.getProductById(itemDto.getProductId());
		if (existingProduct != null) {
			int quantity = existingProduct.getAvailability() - itemDto.getQuantity();
			existingProduct.setAvailability(quantity);
			productClient.updateProductById(itemDto.getProductId(), existingProduct);
		}
	}

	private void reduceProductStock(ItemDto itemDto, int quantity) {
		ProductDto existingProduct = productClient.getProductById(itemDto.getProductId());
		if (existingProduct != null) {
			int quant = existingProduct.getAvailability() - quantity;
			existingProduct.setAvailability(quant);
			productClient.updateProductById(itemDto.getProductId(), existingProduct);
		}
	}

}
