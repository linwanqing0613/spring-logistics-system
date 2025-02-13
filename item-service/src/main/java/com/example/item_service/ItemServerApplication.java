package com.example.item_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.item_service", "com.example.common"})
public class ItemServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServerApplication.class, args);
	}

}
