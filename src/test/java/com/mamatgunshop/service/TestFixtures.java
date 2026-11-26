package com.mamatgunshop.service;

import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;

import java.math.BigDecimal;

/** Data produk kecil untuk kebutuhan unit test. */
final class TestFixtures {

    static Product product(String id, String name, String brand, GunType type,
                           String caliber, int capacity, String price, int stock) {
        return new Product(id, name, brand, type, caliber, capacity,
                new BigDecimal(price), stock, "Deskripsi " + name, "");
    }

    private TestFixtures() {
    }
}
