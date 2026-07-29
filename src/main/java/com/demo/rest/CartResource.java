package com.demo.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import jakarta.enterprise.context.ApplicationScoped;

import com.demo.model.ShoppingCart;
import com.demo.service.ShoppingCartService;

@Path("/api/cart")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CartResource {

    private final ShoppingCartService shoppingCartService;

    public CartResource(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GET
    @Path("/{cartId}")
    public ShoppingCart getCart(@PathParam("cartId") String cartId) {
        return shoppingCartService.getShoppingCart(cartId);
    }

    @POST
    @Path("/{cartId}/{itemId}/{quantity}")
    public ShoppingCart add(@PathParam("cartId") String cartId,
                            @PathParam("itemId") String itemId,
                            @PathParam("quantity") int quantity) {
        return shoppingCartService.addItem(cartId, itemId, quantity);
    }

    @POST
    @Path("/{cartId}/{tmpId}")
    public ShoppingCart set(@PathParam("cartId") String cartId,
                            @PathParam("tmpId") String tmpId) {
        return shoppingCartService.set(cartId, tmpId);
    }

    @DELETE
    @Path("/{cartId}/{itemId}/{quantity}")
    public ShoppingCart delete(@PathParam("cartId") String cartId,
                               @PathParam("itemId") String itemId,
                               @PathParam("quantity") int quantity) {
        return shoppingCartService.deleteItem(cartId, itemId, quantity);
    }

    @POST
    @Path("/checkout/{cartId}")
    public ShoppingCart checkout(@PathParam("cartId") String cartId) {
        return shoppingCartService.checkout(cartId);
    }
}
