package com.demo.service;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.demo.model.Product;

/**
 * REST client for the external catalog service.
 *
 * <p>FORBIDDEN: This interface must never fall back to mock product data.
 * Catalog failures must propagate as exceptions — no method calls that return
 * fabricated products, no "fallback to mock" logic, no synthetic product data.
 * See migration.yaml forbidden list for enforcement.</p>
 */
@RegisterRestClient(configKey = "catalogService")
public interface CatalogService {

    @GET
    @Path("/api/products")
    List<Product> products();
}
