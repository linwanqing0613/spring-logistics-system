package com.example.item_service.service;

import com.example.item_service.dto.ItemDTO;
import com.example.item_service.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {
    public String createItem(ItemDTO itemDTO);
    public Item getItemById(String id);
    public void updateItem(ItemDTO itemDTO);
    public void deleteItem(String id);
    public Page<Item> getAllItems(Integer minPrice, Integer maxPrice, String ItemName, Pageable pageable);
}
