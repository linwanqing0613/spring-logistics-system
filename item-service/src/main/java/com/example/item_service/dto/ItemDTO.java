package com.example.item_service.dto;


import com.example.common.dto.OnCreate;
import com.example.common.dto.OnRegister;
import com.example.common.dto.OnUpdate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public class ItemDTO {
    @NotNull(message = "ID cannot be null", groups = OnUpdate.class)
    @Null(message = "ID must be null for create requests", groups = {OnCreate.class, OnRegister.class})
    private String id;
    @NotNull(message = "Username cannot be blank", groups = OnRegister.class)
    private String name;
    @NotNull(message = "Username cannot be blank", groups = OnRegister.class)
    private int price;

    public ItemDTO() {
    }

    public ItemDTO(String id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
