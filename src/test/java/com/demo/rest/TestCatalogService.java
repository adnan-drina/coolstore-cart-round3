package com.demo.rest;

import com.demo.model.Product;
import com.demo.service.CatalogService;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.List;

@Mock
@ApplicationScoped
class TestCatalogService implements CatalogService {

    @Override
    public List<Product> products() {
        return Arrays.asList(
            new Product("1111", "Car", "Super car", 1000.0),
            new Product("2222", "Phone", "Smart phone", 500.0),
            new Product("3333", "Laptop", "Fast laptop", 15.0),
            new Product("4444", "Tablet", "Small tablet", 30.0)
        );
    }
}
