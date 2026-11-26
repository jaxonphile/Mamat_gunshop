package com.mamatgunshop.service;

import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;
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

class CartServiceTest {

    @TempDir
    Path tempDir;

    private ProductRepository repository;
    private CartService cartService;
    private Product product;

    @BeforeEach
    void setUp() {
        repository = new ProductRepository(tempDir.resolve("products.json"));
        product = TestFixtures.product("p1", "Glock 17", "Glock", GunType.PISTOL, "9mm", 17, "100000", 5);
        repository.save(product);
        cartService = new CartService(repository);
    }

    @Test
    void addProductCalculatesTotal() {
        cartService.add("p1", 2);

        assertEquals(2, cartService.getCart().getItemCount());
        assertEquals(new BigDecimal("200000"), cartService.getCart().getTotal());
    }

    @Test
    void addingSameProductAccumulatesQuantity() {
        cartService.add("p1", 2);
        cartService.add("p1", 1);

        assertEquals(3, cartService.getCart().getItemCount());
    }

    @Test
    void addRejectsQuantityAboveStock() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cartService.add("p1", 6));

        assertTrue(error.getMessage().contains("Stok"));
    }

    @Test
    void updateQuantityChangesTotal() {
        cartService.add("p1", 1);

        cartService.updateQuantity("p1", 3);

        assertEquals(new BigDecimal("300000"), cartService.getCart().getTotal());
    }

    @Test
    void updateQuantityRejectsZero() {
        cartService.add("p1", 1);

        assertThrows(IllegalArgumentException.class, () -> cartService.updateQuantity("p1", 0));
    }

    @Test
    void removeItemEmptiesCart() {
        cartService.add("p1", 1);

        cartService.remove("p1");

        assertTrue(cartService.getCart().isEmpty());
        assertEquals(BigDecimal.ZERO, cartService.getCart().getTotal());
    }

    @Test
    void validateStockRejectsEmptyCart() {
        assertThrows(IllegalArgumentException.class, cartService::validateStock);
    }

    @Test
    void validateStockRejectsWhenRepositoryStockDrops() {
        cartService.add("p1", 3);
        repository.decrementStock("p1", 4);
        repository.persist();

        assertThrows(IllegalArgumentException.class, cartService::validateStock);
        assertFalse(cartService.getCart().isEmpty());
    }
}
