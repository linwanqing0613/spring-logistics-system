package com.example.item_service.repository;

import com.example.item_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String>, JpaSpecificationExecutor<Item> {
    Boolean existsByName(String name);
    Optional<Item> findByName(String name);
}
