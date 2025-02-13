package com.example.item_service.controller;

import com.example.common.dto.ResponseDTO;
import com.example.item_service.dto.ItemDTO;
import com.example.item_service.entity.Item;
import com.example.item_service.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;


    @PostMapping(consumes = "application/json",
            produces = "application/json",
            headers = "Authorization")
    public ResponseEntity<ResponseDTO<Void>> createItem(@Valid @RequestBody ItemDTO itemDTO) {
        itemService.createItem(itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseDTO.create("Item created successfully", null)
        );
    }

    @GetMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseDTO<Page<Item>>> getAllItems(
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String itemName,
            Pageable pageable
    ) {
        Page<Item> itemList = itemService.getAllItems(minPrice, maxPrice, itemName, pageable);
        return ResponseEntity.ok(
                ResponseDTO.success("Fetched all items successfully", itemList)
        );
    }

    @GetMapping(value = "/{id}",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<ResponseDTO<Item>> getItemById(@PathVariable String id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(
                ResponseDTO.success("Item fetched successfully", item)
        );
    }

    @PutMapping(value = "/{id}",
            consumes = "application/json",
            produces = "application/json",
            headers = "Authorization")
    public ResponseEntity<ResponseDTO<Item>> updateItem(@PathVariable String id, @Valid @RequestBody ItemDTO itemDTO) {
        itemDTO.setId(id);
        itemService.updateItem(itemDTO);
        Item updatedItem = itemService.getItemById(id);
        return ResponseEntity.ok(
                ResponseDTO.success("Item updated successfully", updatedItem)
        );
    }

    @DeleteMapping(
            value = "/{id}",
            consumes = "application/json",
            produces = "application/json",
            headers = "Authorization")
    public ResponseEntity<ResponseDTO<Void>> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(
                ResponseDTO.success("Item deleted successfully", null)
        );
    }
}