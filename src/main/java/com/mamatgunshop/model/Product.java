package com.mamatgunshop.model;

import java.math.BigDecimal;
import java.util.UUID;

/** Entitas produk senjata yang dijual. */
public class Product {

    private String id;
    private String name;
    private String brand;
    private GunType type;
    private String caliber;
    private int capacity;
    private BigDecimal price;
    private int stock;
    private String description;
    private String imagePath;

    /** Konstruktor kosong untuk deserialisasi JSON. */
    public Product() {
    }

    public Product(String id, String name, String brand, GunType type, String caliber,
                   int capacity, BigDecimal price, int stock, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.type = type;
        this.caliber = caliber;
        this.capacity = capacity;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public Product(String name, String brand, GunType type, String caliber, int capacity,
                   BigDecimal price, int stock, String description, String imagePath) {
        this(UUID.randomUUID().toString(), name, brand, type, caliber, capacity,
                price, stock, description, imagePath);
    }

    /** Salinan mandiri, dipakai untuk snapshot keranjang. */
    public Product copy() {
        return new Product(id, name, brand, type, caliber, capacity, price, stock, description, imagePath);
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public GunType getType() {
        return type;
    }

    public void setType(GunType type) {
        this.type = type;
    }

    public String getCaliber() {
        return caliber;
    }

    public void setCaliber(String caliber) {
        this.caliber = caliber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    @Override
    public String toString() {
        return name + " (" + caliber + ")";
    }
}
