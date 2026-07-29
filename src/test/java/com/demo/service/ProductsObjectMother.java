package com.demo.service;

import java.util.Arrays;
import java.util.List;

import com.demo.model.Product;

class ProductsObjectMother {

    public static List<Product> createVehicleProducts() {
        return Arrays.asList(
            new Product("1111", "Car", "Super car", 1000),
            new Product("2222", "Bike", "Super bike", 200));
    }
}
