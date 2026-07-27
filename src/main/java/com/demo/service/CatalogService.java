package com.demo.service;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.demo.model.Product;

@RegisterRestClient(configKey = "catalogService")
public interface CatalogService {

    @GET
    @Path("/api/products")
    List<Product> products();
}
