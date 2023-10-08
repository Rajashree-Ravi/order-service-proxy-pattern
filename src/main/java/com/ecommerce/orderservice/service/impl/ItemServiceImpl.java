package com.ecommerce.orderservice.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.orderservice.entity.Item;
import com.ecommerce.orderservice.repository.ItemRepository;
import com.ecommerce.orderservice.service.ItemService;
import com.ecommerce.orderservice.model.InventoryDto;
import com.ecommerce.orderservice.model.ItemDto;

@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	InventoryServiceProxy inventoryServiceProxy;

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
		if (itemDto.getInventoryId() != null) {
			InventoryDto inventoryDto = inventoryServiceProxy.getInventoryById(itemDto.getInventoryId());
			int quantity = inventoryDto.getVendorInventory() + itemDto.getQuantity();
			inventoryDto.setVendorInventory(quantity);
			inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);
		} else {
			InventoryDto inventoryDto = inventoryServiceProxy.getInventoryByProductId(itemDto.getProductId()).get(0);
			int quantity = inventoryDto.getVendorInventory() + itemDto.getQuantity();
			inventoryDto.setVendorInventory(quantity);
			inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);

			itemDto.setInventoryId(inventoryDto.getId());
			updateItem(itemDto.getId(), itemDto);
		}
	}

	private void increaseProductStock(ItemDto itemDto, int quantity) {
		if (itemDto.getInventoryId() != null) {
			InventoryDto inventoryDto = inventoryServiceProxy.getInventoryById(itemDto.getInventoryId());
			int quant = inventoryDto.getVendorInventory() + quantity;
			inventoryDto.setVendorInventory(quant);
			inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);
		} else {
			InventoryDto inventoryDto = inventoryServiceProxy.getInventoryByProductId(itemDto.getProductId()).get(0);
			int quant = inventoryDto.getVendorInventory() + quantity;
			inventoryDto.setVendorInventory(quant);
			inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);

			itemDto.setInventoryId(inventoryDto.getId());
			updateItem(itemDto.getId(), itemDto);
		}
	}

	@Override
	public void reduceProductStock(ItemDto itemDto) {
		boolean canReduce = false;

		if (itemDto.getInventoryId() != null) {
			InventoryDto inventoryDto = inventoryServiceProxy.getInventoryById(itemDto.getInventoryId());
			if (inventoryDto.getVendorInventory() != null
					&& inventoryDto.getVendorInventory() >= itemDto.getQuantity()) {
				int quantity = inventoryDto.getVendorInventory() - itemDto.getQuantity();
				inventoryDto.setVendorInventory(quantity);
				inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);
				canReduce = true;
			}
		}

		if (!canReduce) {
			List<InventoryDto> inventoryList = inventoryServiceProxy.getInventoryByProductId(itemDto.getProductId());

			for (InventoryDto inventoryDto : inventoryList) {
				if (inventoryDto.getVendorInventory() != null
						&& inventoryDto.getVendorInventory() >= itemDto.getQuantity()) {
					int quantity = inventoryDto.getVendorInventory() - itemDto.getQuantity();
					inventoryDto.setVendorInventory(quantity);
					inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);

					itemDto.setInventoryId(inventoryDto.getId());
					updateItem(itemDto.getId(), itemDto);

					canReduce = true;
					break;
				}
			}
		}
	}

	private void reduceProductStock(ItemDto itemDto, int quantity) {
		boolean canReduce = false;

		if (itemDto.getInventoryId() != null) {
			InventoryDto inventoryDto = inventoryServiceProxy.getInventoryById(itemDto.getInventoryId());
			if (inventoryDto.getVendorInventory() != null && inventoryDto.getVendorInventory() >= quantity) {
				int quant = inventoryDto.getVendorInventory() - quantity;
				inventoryDto.setVendorInventory(quant);
				inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);
				canReduce = true;
			}
		}

		if (!canReduce) {
			List<InventoryDto> inventoryList = inventoryServiceProxy.getInventoryByProductId(itemDto.getProductId());

			for (InventoryDto inventoryDto : inventoryList) {
				if (inventoryDto.getVendorInventory() != null && inventoryDto.getVendorInventory() >= quantity) {
					int quant = inventoryDto.getVendorInventory() - quantity;
					inventoryDto.setVendorInventory(quant);
					inventoryServiceProxy.updateInventory(inventoryDto.getId(), inventoryDto);

					itemDto.setInventoryId(inventoryDto.getId());
					updateItem(itemDto.getId(), itemDto);

					canReduce = true;
					break;
				}
			}
		}
	}

}
