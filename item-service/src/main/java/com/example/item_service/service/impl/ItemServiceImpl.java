package com.example.item_service.service.impl;

import com.example.common.annotation.DistributedLock;
import com.example.common.dto.ModelName;

import com.example.common.dto.RedisKey;
import com.example.common.exception.BadRequestException;
import com.example.common.service.RedisService;
import com.example.common.service.UUIDProvider;
import com.example.item_service.dto.ItemDTO;
import com.example.item_service.dto.ItemSpecification;
import com.example.item_service.entity.Item;
import com.example.item_service.repository.ItemRepository;
import com.example.item_service.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);
    private static final String PREFIX = RedisKey.ITEM_CACHE.getPrefix();
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UUIDProvider uuidProvider;
    @Autowired
    private RedisService redisService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DistributedLock(key ="#{T(com.example.common.RedisKey).ITEM_LOCK.getPrefix() + #itemDTO.id}")
    public String createItem(ItemDTO itemDTO) {
        log.info("Attempting to create item: {}", itemDTO.getName());

        if (itemRepository.existsByName(itemDTO.getName())) {
            log.warn("create: Item with name {} already exists.", itemDTO.getName());
            throw new BadRequestException("Item with name " + itemDTO.getName() + " already exists.");
        }
        String itemId = uuidProvider.generateUUID(ModelName.ITEM);
        Item item = updateItemFromDTO(new Item(itemId), itemDTO);
        itemRepository.save(item);
        redisService.save(PREFIX + itemId, item);
        return itemId;
    }

    @Override
    public Item getItemById(String id) {
        Item item = redisService.get(PREFIX + id, Item.class);
        if(item!= null){
            return item;
        }
        return itemRepository.findById(id).orElseThrow( () -> {
            log.warn("que: Item not found with ID: {}", id);
            return new BadRequestException("Item not found with ID: {}" + id);
        });
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DistributedLock(key ="#{T(com.example.common.RedisKey).ITEM_LOCK.getPrefix() + #itemDTO.id}")
    public void updateItem(ItemDTO itemDTO) {
        log.info("Attempting to update item with ID: {}", itemDTO.getId());

        Item item = itemRepository.findById(itemDTO.getId()).orElseThrow(
                () -> {
                    log.warn("update: Item not found with ID: {}", itemDTO.getId());
                    return new BadRequestException("Item not found with ID: " + itemDTO.getId());
                });

        updateItemFromDTO(item, itemDTO);
        itemRepository.save(item);
        redisService.update(PREFIX + itemDTO.getId(), item);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DistributedLock(key ="#{T(com.example.common.RedisKey).ITEM_LOCK.getPrefix() + #itemDTO.id}")
    public void deleteItem(String id) {
        log.info("Attempting to delete item with ID: {}", id);
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            redisService.delete(PREFIX + id);
        } else {
            log.warn("delete: Item not found with ID: {}", id);
            throw new BadRequestException("Item not found with ID: " + id);
        }
    }

    @Override
    public Page<Item> getAllItems(Integer minPrice, Integer maxPrice, String itemName, Pageable pageable) {
        Specification<Item> spec = ItemSpecification.searchByCriteria(minPrice, maxPrice, itemName);
        return itemRepository.findAll(spec, pageable);
    }
    private Item updateItemFromDTO(Item item, ItemDTO itemDTO) {
        Optional.ofNullable(itemDTO.getName()).ifPresent(item::setName);
        Optional.of(itemDTO.getPrice()).ifPresent(item::setPrice);
        return item;
    }
}
