package com.example.item_service.dto;

import com.example.item_service.entity.Item;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ItemSpecification {
    public static Specification<Item> searchByCriteria(Integer minPrice, Integer maxPrice, String itemName) {
        return (root,  query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (minPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (itemName != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("name"), itemName));
            }
            return predicate;
        };
    }
}
