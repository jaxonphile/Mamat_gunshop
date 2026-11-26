package com.mamatgunshop.service;

import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

/** Validasi dan operasi CRUD produk untuk panel admin. */
public class InventoryService {

    private final ProductRepository repository;

    public InventoryService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product save(Product product) {
        validate(product);
        return repository.save(product);
    }

    public boolean delete(String id) {
        return repository.deleteById(id);
    }

    public void validate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Nama produk wajib diisi");
        }
        if (product.getBrand() == null || product.getBrand().isBlank()) {
            throw new IllegalArgumentException("Merek wajib diisi");
        }
        if (product.getType() == null) {
            throw new IllegalArgumentException("Tipe senjata wajib dipilih");
        }
        if (product.getCaliber() == null || product.getCaliber().isBlank()) {
            throw new IllegalArgumentException("Kaliber wajib diisi");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Harga harus lebih dari 0");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif");
        }
        if (product.getCapacity() < 0) {
            throw new IllegalArgumentException("Kapasitas tidak boleh negatif");
        }
    }

    /** Dipakai UI saat membuat produk baru dengan tipe dari combo box. */
    public Product newProduct(String name, String brand, GunType type, String caliber,
                              int capacity, BigDecimal price, int stock, String description) {
        Product product = new Product(name, brand, type, caliber, capacity, price, stock, description, "");
        validate(product);
        return product;
    }
}
