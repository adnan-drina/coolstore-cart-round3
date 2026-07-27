package com.demo.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;

class ModelIntegrationTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart("integration-test-cart");
    }

    @Test
    void shopping_cart_initializes_with_empty_item_list() {
        assertThat(cart.getShoppingCartItemList()).isEmpty();
        assertThat(cart.getShoppingCartItemList()).isNotNull();
    }

    @Test
    void shopping_cart_initializes_with_zero_pricing_fields() {
        assertThat(cart.getCartItemTotal()).isZero();
        assertThat(cart.getCartItemPromoSavings()).isZero();
        assertThat(cart.getShippingTotal()).isZero();
        assertThat(cart.getShippingPromoSavings()).isZero();
        assertThat(cart.getCartTotal()).isZero();
    }

    @Test
    void add_single_cart_item_with_product() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(25.0);

        cart.addShoppingCartItem(item);

        assertThat(cart.getShoppingCartItemList()).hasSize(1);
        assertThat(cart.getShoppingCartItemList().get(0).getProduct()).isSameAs(product);
        assertThat(cart.getShoppingCartItemList().get(0).getQuantity()).isEqualTo(1);
        assertThat(cart.getShoppingCartItemList().get(0).getPrice()).isEqualTo(25.0);
    }

    @Test
    void add_multiple_cart_items_with_different_products() {
        Product car = new Product("1111", "Car", "Super car", 1000);
        Product bike = new Product("2222", "Bike", "Fast bike", 500);

        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setProduct(car);
        item1.setQuantity(2);
        item1.setPrice(1000);

        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setProduct(bike);
        item2.setQuantity(1);
        item2.setPrice(500);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);

        assertThat(cart.getShoppingCartItemList()).hasSize(2);
        assertThat(cart.getShoppingCartItemList().get(0).getProduct().getItemId()).isEqualTo("1111");
        assertThat(cart.getShoppingCartItemList().get(1).getProduct().getItemId()).isEqualTo("2222");
    }

    @Test
    void add_null_cart_item_is_ignored() {
        int initialSize = cart.getShoppingCartItemList().size();
        cart.addShoppingCartItem(null);

        assertThat(cart.getShoppingCartItemList()).hasSize(initialSize);
    }

    @Test
    void remove_existing_cart_item_returns_true() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(25.0);

        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(item);

        assertThat(removed).isTrue();
        assertThat(cart.getShoppingCartItemList()).isEmpty();
    }

    @Test
    void remove_nonexistent_cart_item_returns_false() {
        ShoppingCartItem item = new ShoppingCartItem();

        boolean removed = cart.removeShoppingCartItem(item);

        assertThat(removed).isFalse();
        assertThat(cart.getShoppingCartItemList()).isEmpty();
    }

    @Test
    void remove_null_cart_item_returns_false() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(25.0);

        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(null);

        assertThat(removed).isFalse();
        assertThat(cart.getShoppingCartItemList()).hasSize(1);
    }

    @Test
    void reset_shopping_cart_item_list_clears_all_items() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(25.0);

        cart.addShoppingCartItem(item);
        cart.resetShoppingCartItemList();

        assertThat(cart.getShoppingCartItemList()).isEmpty();
    }

    @Test
    void reset_preserves_cart_id() {
        cart.resetShoppingCartItemList();
        assertThat(cart.getCartId()).isEqualTo("integration-test-cart");
    }

    @Test
    void product_reference_integrity_across_cart_item_operations() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(3);
        item.setPrice(25.0);

        cart.addShoppingCartItem(item);

        Product retrieved = cart.getShoppingCartItemList().get(0).getProduct();

        assertThat(retrieved).isSameAs(product);
        assertThat(retrieved.getItemId()).isEqualTo("P001");
        assertThat(retrieved.getName()).isEqualTo("Widget");
        assertThat(retrieved.getDesc()).isEqualTo("A widget");
        assertThat(retrieved.getPrice()).isEqualTo(25.0);
    }

    @Test
    void cart_item_total_data_flow_from_items() {
        Product product = new Product("1111", "Car", "Super car", 1000);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(1000);

        cart.addShoppingCartItem(item);

        double expectedTotal = cart.getShoppingCartItemList().stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();

        assertThat(expectedTotal).isEqualTo(2000.0);
    }

    @Test
    void cart_item_promo_savings_integration() {
        Product product = new Product("P001", "Widget", "A widget", 100.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(100.0);
        item.setPromoSavings(10.0);

        cart.addShoppingCartItem(item);

        assertThat(cart.getShoppingCartItemList().get(0).getPromoSavings()).isEqualTo(10.0);
    }

    @Test
    void promotion_entity_links_to_product_by_item_id() {
        Product product = new Product("P001", "Widget", "A widget", 100.0);
        Promotion promotion = new Promotion("P001", 10.0);

        assertThat(promotion.getItemId()).isEqualTo(product.getItemId());
        assertThat(promotion.getPercentOff()).isEqualTo(10.0);
    }

    @Test
    void promotion_default_constructor_initializes_empty() {
        Promotion promotion = new Promotion();

        assertThat(promotion.getItemId()).isNull();
        assertThat(promotion.getPercentOff()).isZero();
    }

    @Test
    void promotion_constructor_sets_all_fields() {
        Promotion promotion = new Promotion("P001", 15.5);

        assertThat(promotion.getItemId()).isEqualTo("P001");
        assertThat(promotion.getPercentOff()).isEqualTo(15.5);
    }

    @Test
    void promotion_setters_update_fields() {
        Promotion promotion = new Promotion();
        promotion.setItemId("P002");
        promotion.setPercentOff(20.0);

        assertThat(promotion.getItemId()).isEqualTo("P002");
        assertThat(promotion.getPercentOff()).isEqualTo(20.0);
    }

    @Test
    void full_pricing_calculation_data_flow() {
        Product car = new Product("1111", "Car", "Super car", 1000);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(car);
        item.setQuantity(2);
        item.setPrice(1000);

        cart.addShoppingCartItem(item);

        double cartItemTotal = cart.getShoppingCartItemList().stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();
        cart.setCartItemTotal(cartItemTotal);
        cart.setCartItemPromoSavings(0.0);

        double shippingTotal = 10.99;
        cart.setShippingTotal(shippingTotal);
        cart.setShippingPromoSavings(-10.99);

        double cartTotal = cart.getCartItemTotal() + cart.getShippingTotal()
            + cart.getCartItemPromoSavings() + cart.getShippingPromoSavings();
        cart.setCartTotal(cartTotal);

        assertThat(cart.getCartItemTotal()).isEqualTo(2000.0);
        assertThat(cart.getCartItemPromoSavings()).isEqualTo(0.0);
        assertThat(cart.getShippingTotal()).isEqualTo(10.99);
        assertThat(cart.getShippingPromoSavings()).isEqualTo(-10.99);
        assertThat(cart.getCartTotal()).isEqualTo(2000.0);
    }

    @Test
    void set_shopping_cart_item_list_replaces_existing() {
        Product p1 = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setProduct(p1);
        item1.setQuantity(1);
        item1.setPrice(25.0);

        cart.addShoppingCartItem(item1);

        Product p2 = new Product("P002", "Gadget", "A gadget", 50.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setProduct(p2);
        item2.setQuantity(1);
        item2.setPrice(50.0);

        cart.setShoppingCartItemList(java.util.List.of(item2));

        assertThat(cart.getShoppingCartItemList()).hasSize(1);
        assertThat(cart.getShoppingCartItemList().get(0).getProduct().getItemId()).isEqualTo("P002");
    }

    @Test
    void shopping_cart_to_string_contains_all_fields() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(25.0);

        cart.addShoppingCartItem(item);
        cart.setCartItemTotal(25.0);
        cart.setCartTotal(25.0);

        String result = cart.toString();

        assertThat(result)
            .contains("ShoppingCart")
            .contains("cartId=integration-test-cart")
            .contains("cartItemTotal=25.0")
            .contains("cartTotal=25.0");
    }

    @Test
    void shopping_cart_item_to_string_contains_all_fields() {
        Product product = new Product("P001", "Widget", "A widget", 25.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(25.0);
        item.setPromoSavings(5.0);

        String result = item.toString();

        assertThat(result)
            .contains("ShoppingCartItem")
            .contains("price=25.0")
            .contains("quantity=2")
            .contains("promoSavings=5.0");
    }

    @Test
    void promotion_to_string_contains_all_fields() {
        Promotion promotion = new Promotion("P001", 10.0);

        String result = promotion.toString();

        assertThat(result)
            .contains("Promotion")
            .contains("itemId=P001")
            .contains("percentOff=10.0");
    }

    @Test
    void cart_serializable_contract_preserved() {
        assertThat(cart).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void cart_item_serializable_contract_preserved() {
        ShoppingCartItem item = new ShoppingCartItem();
        assertThat(item).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void product_serializable_contract_preserved() {
        Product product = new Product();
        assertThat(product).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shopping_cart_serial_version_uid_preserved() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = ShoppingCart.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);

        assertThat(field.getLong(null)).isEqualTo(-1108043957592113528L);
    }

    @Test
    void shopping_cart_item_serial_version_uid_preserved() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = ShoppingCartItem.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);

        assertThat(field.getLong(null)).isEqualTo(6964558044240061049L);
    }

    @Test
    void complex_cart_with_multiple_item_types() {
        Product car = new Product("1111", "Car", "Super car", 1000);
        Product bike = new Product("2222", "Bike", "Fast bike", 500);
        Product helmet = new Product("3333", "Helmet", "Safe helmet", 50);

        ShoppingCartItem carItem = new ShoppingCartItem();
        carItem.setProduct(car);
        carItem.setQuantity(1);
        carItem.setPrice(1000);

        ShoppingCartItem bikeItem = new ShoppingCartItem();
        bikeItem.setProduct(bike);
        bikeItem.setQuantity(2);
        bikeItem.setPrice(500);

        ShoppingCartItem helmetItem = new ShoppingCartItem();
        helmetItem.setProduct(helmet);
        helmetItem.setQuantity(3);
        helmetItem.setPrice(50);

        cart.addShoppingCartItem(carItem);
        cart.addShoppingCartItem(bikeItem);
        cart.addShoppingCartItem(helmetItem);

        assertThat(cart.getShoppingCartItemList()).hasSize(3);

        double expectedTotal = 1000 * 1 + 500 * 2 + 50 * 3;
        double actualTotal = cart.getShoppingCartItemList().stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity())
            .sum();

        assertThat(actualTotal).isEqualTo(expectedTotal);
    }

    @Test
    void remove_item_from_multi_item_cart_preserves_order() {
        Product p1 = new Product("P001", "First", "First item", 10.0);
        Product p2 = new Product("P002", "Second", "Second item", 20.0);
        Product p3 = new Product("P003", "Third", "Third item", 30.0);

        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setProduct(p1);
        item1.setQuantity(1);
        item1.setPrice(10.0);

        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setProduct(p2);
        item2.setQuantity(1);
        item2.setPrice(20.0);

        ShoppingCartItem item3 = new ShoppingCartItem();
        item3.setProduct(p3);
        item3.setQuantity(1);
        item3.setPrice(30.0);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);
        cart.addShoppingCartItem(item3);

        cart.removeShoppingCartItem(item2);

        assertThat(cart.getShoppingCartItemList()).hasSize(2);
        assertThat(cart.getShoppingCartItemList().get(0).getProduct().getItemId()).isEqualTo("P001");
        assertThat(cart.getShoppingCartItemList().get(1).getProduct().getItemId()).isEqualTo("P003");
    }
}
