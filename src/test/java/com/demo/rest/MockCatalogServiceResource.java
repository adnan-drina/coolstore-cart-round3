package com.demo.rest;

import com.demo.model.Product;
import com.demo.service.CatalogService;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Quarkus test resource that provides a mocked CatalogService
 * for integration tests to avoid external HTTP calls.
 */
public class MockCatalogServiceResource implements QuarkusTestResourceLifecycleManager {

    @Alternative
    @Priority(1)
    @RestClient
    @ApplicationScoped
    public static class MockCatalogService implements CatalogService {
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

    @Override
    public Map<String, String> start() {
        // Disable real REST client by changing the endpoint URL
        return Map.of(
            "quarkus.rest-client.catalogService.url", "http://localhost:9999",  // Non-existent URL
            "catalog.endpoint", "http://localhost:9999"
        );
    }

    @Override
    public void stop() {
        // Nothing to clean up
    }
}