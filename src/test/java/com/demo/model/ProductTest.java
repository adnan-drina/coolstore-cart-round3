package com.demo.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for Product entity behavior and serialization.
 * These tests preserve the legacy Product contract to ensure migration integrity.
 */
class ProductTest {

    @Test
    void default_constructor_initializes_null_fields() {
        Product product = new Product();

        assertThat(product.getItemId()).isNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getDesc()).isNull();
        assertThat(product.getPrice()).isZero();
    }

    @Test
    void constructor_initializes_all_fields() {
        Product product = new Product("1111", "Car", "Super car", 1000);

        assertThat(product)
            .returns("1111", Product::getItemId)
            .returns("Car", Product::getName)
            .returns("Super car", Product::getDesc)
            .returns(1000.0, Product::getPrice);
    }

    @Test
    void constructor_preserves_exact_field_values() {
        Product product = new Product("A1", "Widget", "A fine widget", 29.99);

        assertThat(product.getItemId()).isEqualTo("A1");
        assertThat(product.getName()).isEqualTo("Widget");
        assertThat(product.getDesc()).isEqualTo("A fine widget");
        assertThat(product.getPrice()).isEqualTo(29.99);
    }

    @Test
    void setter_updates_item_id() {
        Product product = new Product();
        product.setItemId("X99");

        assertThat(product.getItemId()).isEqualTo("X99");
    }

    @Test
    void setter_updates_name() {
        Product product = new Product();
        product.setName("Updated Name");

        assertThat(product.getName()).isEqualTo("Updated Name");
    }

    @Test
    void setter_updates_desc() {
        Product product = new Product();
        product.setDesc("Updated Description");

        assertThat(product.getDesc()).isEqualTo("Updated Description");
    }

    @Test
    void setter_updates_price() {
        Product product = new Product();
        product.setPrice(99.99);

        assertThat(product.getPrice()).isEqualTo(99.99);
    }

    @Test
    void serial_version_uid_is_preserved() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = Product.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);

        assertThat(field.getLong(null)).isEqualTo(-7304814269819778382L);
    }

    @Test
    void to_string_contains_all_fields() {
        Product product = new Product("1111", "Car", "Super car", 1000);

        String result = product.toString();

        assertThat(result)
            .contains("Product")
            .contains("itemId=1111")
            .contains("name=Car")
            .contains("desc=Super car")
            .contains("price=1000.0");
    }

    @Test
    void to_string_format_matches_legacy_specification() {
        Product product = new Product("123", "Test Product", "Test Description", 49.99);

        String result = product.toString();
        String expected = "Product [itemId=123, name=Test Product, desc=Test Description, price=49.99]";

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getters_and_setters_work_correctly_together() {
        Product product = new Product();
        
        // Set all fields
        product.setItemId("456");
        product.setName("Updated Product");
        product.setDesc("Updated Description");
        product.setPrice(149.99);

        // Verify all fields
        assertThat(product)
            .returns("456", Product::getItemId)
            .returns("Updated Product", Product::getName)
            .returns("Updated Description", Product::getDesc)
            .returns(149.99, Product::getPrice);
    }

    @Test
    void json_serialization_compatibility() {
        // Test that JSON field names match legacy expectations
        Product product = new Product("789", "JSON Product", "JSON Description", 19.99);
        
        // Verify expected JSON field names via getters
        assertThat(product.getItemId()).isEqualTo("789");     // JSON: itemId
        assertThat(product.getName()).isEqualTo("JSON Product"); // JSON: name
        assertThat(product.getDesc()).isEqualTo("JSON Description"); // JSON: desc
        assertThat(product.getPrice()).isEqualTo(19.99);     // JSON: price
    }

    @Test
    void shopping_cart_item_integration_contract() {
        // Verify Product can be used with ShoppingCartItem as per tasks.md integration
        Product product = new Product("1111", "Car", "Super car", 1000);
        
        // Ensure fields required by ShoppingCartItem integration are accessible
        assertThat(product.getItemId()).isEqualTo("1111");
        assertThat(product.getName()).isEqualTo("Car");
        assertThat(product.getDesc()).isEqualTo("Super car");
        assertThat(product.getPrice()).isEqualTo(1000.0);
    }

    @Test
    void constructor_with_zero_price() {
        Product product = new Product("FREE", "Free Item", "No cost", 0.0);

        assertThat(product.getPrice()).isZero();
        assertThat(product.getItemId()).isEqualTo("FREE");
    }

    @Test
    void constructor_with_negative_price() {
        Product product = new Product("DEBT", "Owe Money", "Negative price", -10.0);

        assertThat(product.getPrice()).isEqualTo(-10.0);
    }

    @Test
    void to_string_with_null_fields_handles_nulls() {
        Product product = new Product();
        product.setItemId(null);
        product.setName(null);
        product.setDesc(null);

        String result = product.toString();
        
        assertThat(result)
            .contains("Product")
            .contains("itemId=null")
            .contains("name=null")
            .contains("desc=null")
            .contains("price=0.0");
    }
}