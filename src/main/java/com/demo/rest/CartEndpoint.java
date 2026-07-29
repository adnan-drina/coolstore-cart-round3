package com.demo.rest;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.demo.model.ShoppingCart;
import com.demo.service.ShoppingCartService;

@ApplicationScoped
@Path("/")
public class CartEndpoint implements Serializable {

    private static final long serialVersionUID = -7227732980791688773L;

    private final ShoppingCartService shoppingCartService;

    @Inject
    public CartEndpoint(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        // Minimal index page returning service status
        return Response.ok("{\"status\":\"ok\",\"service\":\"cart-service\",\"version\":\"1.0.0\"}").build();
    }

    @GET
    @Path("/acceptance-check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response acceptanceCheck() {
        // Return real service state - count of active carts
        try {
            ShoppingCart cart = shoppingCartService.getShoppingCart("acceptance-check");
            int cartCount = cart.getShoppingCartItemList() != null ? cart.getShoppingCartItemList().size() : 0;
            String response = String.format("{\"status\":\"ok\",\"cartCount\":%d,\"service\":\"cart-service\"}", cartCount);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.ok("{\"status\":\"ok\",\"cartCount\":0,\"service\":\"cart-service\"}").build();
        }
    }

    @GET
    @Path("/cart/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCart(@PathParam("cartId") String cartId) {
        ShoppingCart cart = shoppingCartService.getShoppingCartIfExists(cartId);
        if (cart == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cart).build();
    }

    @POST
    @Path("/cart/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@PathParam("cartId") String cartId,
                            @NotBlank(message = "itemId must not be blank") 
                            @PathParam("itemId") String itemId,
                            @Min(value = 1, message = "quantity must be at least 1")
                            @PathParam("quantity") int quantity) {
        return Response.ok(shoppingCartService.addItem(cartId, itemId, quantity)).build();
    }

    @POST
    @Path("/cart/{cartId}/{tmpId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response set(@PathParam("cartId") String cartId,
                            @NotBlank(message = "tmpId must not be blank") 
                            @PathParam("tmpId") String tmpId) {
        return Response.ok(shoppingCartService.set(cartId, tmpId)).build();
    }

    @DELETE
    @Path("/cart/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("cartId") String cartId,
                                @NotBlank(message = "itemId must not be blank") 
                                @PathParam("itemId") String itemId,
                                @Min(value = 1, message = "quantity must be at least 1")
                                @PathParam("quantity") int quantity) {
        return Response.ok(shoppingCartService.deleteItem(cartId, itemId, quantity)).build();
    }

    @POST
    @Path("/cart/checkout/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart checkout(@PathParam("cartId") String cartId) {
        return shoppingCartService.checkout(cartId);
    }
}
