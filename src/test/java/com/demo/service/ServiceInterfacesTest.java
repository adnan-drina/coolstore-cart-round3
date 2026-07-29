package com.demo.service;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.junit.jupiter.api.Test;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceInterfacesTest {

  @Test
  void shoppingCartServiceHasAllSevenMethodSignatures() {
    Class<?> iface = ShoppingCartService.class;

    Method getCart = findMethod(iface, "getShoppingCart", String.class);
    Method getProduct = findMethod(iface, "getProduct", String.class);
    Method deleteItem = findMethod(iface, "deleteItem", String.class, String.class, int.class);
    Method checkout = findMethod(iface, "checkout", String.class);
    Method addItem = findMethod(iface, "addItem", String.class, String.class, int.class);
    Method set = findMethod(iface, "set", String.class, String.class);
    Method price = findMethod(iface, "priceShoppingCart", ShoppingCart.class);

    assertAll(
      () -> {
        assertNotNull(getCart, "getShoppingCart(String) missing");
        assertEquals(ShoppingCart.class, getCart.getReturnType());
      },
      () -> {
        assertNotNull(getProduct, "getProduct(String) missing");
        assertEquals(Product.class, getProduct.getReturnType());
      },
      () -> {
        assertNotNull(deleteItem, "deleteItem(String, String, int) missing");
        assertEquals(ShoppingCart.class, deleteItem.getReturnType());
      },
      () -> {
        assertNotNull(checkout, "checkout(String) missing");
        assertEquals(ShoppingCart.class, checkout.getReturnType());
      },
      () -> {
        assertNotNull(addItem, "addItem(String, String, int) missing");
        assertEquals(ShoppingCart.class, addItem.getReturnType());
      },
      () -> {
        assertNotNull(set, "set(String, String) missing");
        assertEquals(ShoppingCart.class, set.getReturnType());
      },
      () -> {
        assertNotNull(price, "priceShoppingCart(ShoppingCart) missing");
        assertEquals(void.class, price.getReturnType());
      }
    );
  }

  @Test
  void catalogServiceHasRegisterRestClientAnnotation() {
    RegisterRestClient ann = CatalogService.class.getAnnotation(RegisterRestClient.class);
    assertNotNull(ann, "CatalogService missing @RegisterRestClient");
    assertEquals("catalog-service", ann.configKey());
  }

  @Test
  void catalogServiceHasBasePathAnnotation() {
    Path pathAnn = CatalogService.class.getAnnotation(Path.class);
    assertNotNull(pathAnn, "CatalogService missing @Path");
    assertEquals("/", pathAnn.value());
  }

  @Test
  void catalogServiceProductsMethodHasCorrectSignature() {
    Method products = findMethod(CatalogService.class, "products");
    assertNotNull(products, "products() method missing");
    assertEquals(List.class, products.getReturnType());
    assertTrue(products.isAnnotationPresent(GET.class), "products() missing @GET");
    assertTrue(products.isAnnotationPresent(Path.class), "products() missing @Path");
    assertEquals("/api/products", products.getAnnotation(Path.class).value());
  }

  @Test
  void catalogEndpointConfigurationPreserved() {
    Properties props = new Properties();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
      assertNotNull(is, "application.properties not found on classpath");
      props.load(is);
    } catch (Exception e) {
      throw new AssertionError("Failed to load application.properties", e);
    }

    String url = props.getProperty("quarkus.rest-client.catalog-service.url");
    assertNotNull(url, "quarkus.rest-client.catalog-service.url not configured");
    assertTrue(url.contains("CATALOG_ENDPOINT"), "CATALOG_ENDPOINT env var not referenced");
    assertTrue(url.contains("http://localhost:8081"), "Default fallback http://localhost:8081 missing");
  }

  @Test
  void catalogServiceProductsMethodReturnsParameterizedListOfProduct() {
    Method products = findMethod(CatalogService.class, "products");
    assertNotNull(products, "products() method missing");

    java.lang.reflect.Type returnType = products.getGenericReturnType();
    assertTrue(returnType instanceof java.lang.reflect.ParameterizedType,
      "products() should return ParameterizedType");

    java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) returnType;
    assertEquals(List.class, pt.getRawType());

    java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
    assertEquals(1, typeArgs.length);
    assertEquals(Product.class, typeArgs[0]);
  }

  private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
    try {
      return clazz.getMethod(name, paramTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
}
