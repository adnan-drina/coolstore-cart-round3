package com.demo.service;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.demo.model.Product;

@Path("/api/products")
@RegisterRestClient
public interface CatalogService {
    @GET
    List<Product> products();
}