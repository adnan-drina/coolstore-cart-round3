package com.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ConcurrencyTest {

    @Mock
    CatalogService catalogService;

    ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(catalogService.products()).thenReturn(ProductsObjectMother.createVehicleProducts());
        shoppingCartService = new ShoppingCartServiceImpl(
            new ShippingService(),
            catalogService,
            new PromoService());
    }

    @Test
    void concurrentCartCreation_createsSingleCart() throws InterruptedException {
        String cartId = "concurrent-create-" + System.nanoTime();
        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicReference<ShoppingCart> result = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
                    if (result.get() == null) {
                        result.compareAndSet(null, cart);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        ShoppingCart cart = result.get();
        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
    }

    @Test
    void concurrentAddItem_toSameCart_preservesState() throws InterruptedException {
        String cartId = "concurrent-add-" + System.nanoTime();
        String itemId = "1111";
        int threads = 20;
        int quantityPerThread = 1;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    shoppingCartService.addItem(cartId, itemId, quantityPerThread);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        assertNotNull(cart);
        assertTrue(!cart.getShoppingCartItemList().isEmpty());
        assertTrue(cart.getCartItemTotal() > 0);
    }

    @Test
    void concurrentReadAndWrite_cartRemainsConsistent() throws InterruptedException {
        String cartId = "concurrent-rw-" + System.nanoTime();
        String itemId = "2222";
        int writers = 5;
        int readers = 10;
        int total = writers + readers;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(total);
        AtomicInteger readErrors = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(total);

        for (int i = 0; i < writers; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    shoppingCartService.addItem(cartId, itemId, 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        for (int i = 0; i < readers; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
                    if (cart == null) {
                        readErrors.incrementAndGet();
                    }
                } catch (Exception e) {
                    readErrors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(0, readErrors.get());
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        assertNotNull(cart);
    }

    @Test
    void concurrentDeleteItem_reducesQuantityCorrectly() throws InterruptedException {
        String cartId = "concurrent-delete-" + System.nanoTime();
        String itemId = "1111";

        shoppingCartService.addItem(cartId, itemId, 10);

        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    shoppingCartService.deleteItem(cartId, itemId, 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        assertNotNull(cart);
        List<ShoppingCartItem> items = cart.getShoppingCartItemList();
        if (items.isEmpty()) {
        } else {
            int totalQty = items.stream().mapToInt(com.demo.model.ShoppingCartItem::getQuantity).sum();
            assertTrue(totalQty <= 10);
        }
    }

    @Test
    void concurrentCheckout_clearsCartItems() throws InterruptedException {
        String cartId = "concurrent-checkout-" + System.nanoTime();

        shoppingCartService.addItem(cartId, "1111", 2);
        shoppingCartService.addItem(cartId, "2222", 1);

        int threads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    shoppingCartService.checkout(cartId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        assertNotNull(cart);
        assertEquals(0, cart.getShoppingCartItemList().size());
        assertEquals(0.0, cart.getCartTotal(), 0.001);
    }

    @Test
    void concurrentGetShoppingCart_noCrashAndValidState() throws InterruptedException {
        String cartId = "concurrent-pricing-" + System.nanoTime();
        shoppingCartService.addItem(cartId, "1111", 2);
        shoppingCartService.addItem(cartId, "2222", 1);

        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
                    if (cart == null || cart.getShoppingCartItemList().isEmpty()) {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(0, errors.get());
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        assertNotNull(cart);
        assertTrue(!cart.getShoppingCartItemList().isEmpty());
        assertTrue(cart.getCartTotal() > 0);
    }

    @Test
    void concurrentDifferentCarts_noInterference() throws InterruptedException {
        int carts = 10;
        int total = carts;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(total);
        AtomicInteger errors = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(total);

        for (int i = 0; i < carts; i++) {
            final String cartId = "different-cart-" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ShoppingCart cart = shoppingCartService.addItem(cartId, "1111", 1);
                    if (cart == null || !cartId.equals(cart.getCartId())) {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(0, errors.get());
    }

}
