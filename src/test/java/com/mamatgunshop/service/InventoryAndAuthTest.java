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

class InventoryAndAuthTest {

    @TempDir
    Path tempDir;

    private InventoryService inventory;

    @BeforeEach
    void setUp() {
        inventory = new InventoryService(new ProductRepository(tempDir.resolve("products.json")));
    }

    @Test
    void saveValidProduct() {
        Product saved = inventory.newProduct("Glock 17", "Glock", GunType.PISTOL,
                "9mm", 17, new BigDecimal("6800000"), 10, "Pistol tes");
        inventory.save(saved);

        assertEquals("Glock 17", inventory.findAll().get(0).getName());
        assertTrue(saved.getId() != null && !saved.getId().isBlank());
    }

    @Test
    void rejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> inventory.newProduct(
                "", "Glock", GunType.PISTOL, "9mm", 17, BigDecimal.TEN, 1, "x"));
    }

    @Test
    void rejectBlankCaliber() {
        assertThrows(IllegalArgumentException.class, () -> inventory.newProduct(
                "Senjata", "Glock", GunType.PISTOL, "", 17, BigDecimal.TEN, 1, "x"));
    }

    @Test
    void rejectZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> inventory.newProduct(
                "Senjata", "Glock", GunType.PISTOL, "9mm", 17, BigDecimal.ZERO, 1, "x"));
    }

    @Test
    void rejectNegativeStock() {
        assertThrows(IllegalArgumentException.class, () -> inventory.newProduct(
                "Senjata", "Glock", GunType.PISTOL, "9mm", 17, BigDecimal.TEN, -1, "x"));
    }

    @Test
    void rejectNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> inventory.newProduct(
                "Senjata", "Glock", GunType.PISTOL, "9mm", -2, BigDecimal.TEN, 1, "x"));
    }

    @Test
    void deleteProductRemovesItFromRepository() {
        Product saved = inventory.newProduct("Glock 17", "Glock", GunType.PISTOL,
                "9mm", 17, BigDecimal.TEN, 1, "x");
        inventory.save(saved);

        assertTrue(inventory.delete(saved.getId()));
        assertTrue(inventory.findAll().isEmpty());
    }

    @Test
    void adminLoginSuccess() {
        assertTrue(new AuthService().login("admin", "admin123"));
    }

    @Test
    void adminLoginFailsWithWrongPassword() {
        assertFalse(new AuthService().login("admin", "salah"));
    }

    @Test
    void adminLoginIsCaseInsensitiveForUsername() {
        assertTrue(new AuthService().login("ADMIN", "admin123"));
    }
}
