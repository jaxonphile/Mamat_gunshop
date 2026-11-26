package com.mamatgunshop.service;

import com.mamatgunshop.model.Customer;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Order;
import com.mamatgunshop.model.OrderStatus;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.repository.OrderRepository;
import com.mamatgunshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutServiceTest {

    @TempDir
    Path tempDir;

    private ProductRepository products;
    private OrderRepository orders;
    private CartService cartService;
    private CheckoutService checkoutService;
    private Product product;

    @BeforeEach
    void setUp() {
        products = new ProductRepository(tempDir.resolve("products.json"));
        orders = new OrderRepository(tempDir.resolve("orders.json"));
        product = TestFixtures.product("p1", "Glock 17", "Glock", GunType.PISTOL, "9mm", 17, "100000", 5);
        products.save(product);
        cartService = new CartService(products);
        checkoutService = new CheckoutService(products, orders, cartService);
    }

    @Test
    void checkoutCreatesOrderAndDecrementsStock() {
        cartService.add("p1", 2);

        Order order = checkoutService.checkout(new Customer("Dina", "1234567890123456", 25));

        assertTrue(order.getId().startsWith("MG-"));
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(new BigDecimal("200000"), order.getTotal());
        assertEquals(3, products.findById("p1").orElseThrow().getStock());
        assertTrue(cartService.getCart().isEmpty());
        assertEquals(1, orders.findAll().size());
    }

    @Test
    void checkoutPersistsProductStock() {
        cartService.add("p1", 1);

        checkoutService.checkout(new Customer("Dina", "1234567890123456", 25));

        ProductRepository reloaded = new ProductRepository(tempDir.resolve("products.json"));
        assertEquals(4, reloaded.findById("p1").orElseThrow().getStock());
    }

    @Test
    void checkoutRejectsAgeBelow21() {
        cartService.add("p1", 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> checkoutService.checkout(new Customer("Dina", "1234567890123456", 20)));

        assertTrue(error.getMessage().contains("21"));
        assertFalse(cartService.getCart().isEmpty());
    }

    @Test
    void checkoutRejectsInvalidKtp() {
        cartService.add("p1", 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> checkoutService.checkout(new Customer("Dina", "123", 25)));

        assertTrue(error.getMessage().contains("16 digit"));
    }

    @Test
    void checkoutRejectsBlankName() {
        cartService.add("p1", 1);

        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.checkout(new Customer("  ", "1234567890123456", 25)));
    }

    @Test
    void checkoutRejectsWhenStockChangedBeforePayment() {
        cartService.add("p1", 3);
        products.decrementStock("p1", 4);
        products.persist();

        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.checkout(new Customer("Dina", "1234567890123456", 25)));
        assertEquals(1, products.findById("p1").orElseThrow().getStock());
        assertTrue(orders.findAll().isEmpty());
    }

    @Test
    void checkoutRejectsEmptyCart() {
        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.checkout(new Customer("Dina", "1234567890123456", 25)));
    }

    @Test
    void receiptContainsMaskedKtpAndTotal() {
        cartService.add("p1", 2);
        Order order = checkoutService.checkout(new Customer("Dina", "1234567890123456", 25));

        String receipt = checkoutService.buildReceipt(order);

        assertTrue(receipt.contains("MAMAT GUNSHOP"));
        assertTrue(receipt.contains("123456******3456"));
        assertTrue(receipt.contains("Rp 200.000"));
        assertTrue(receipt.contains(order.getId()));
    }
}
