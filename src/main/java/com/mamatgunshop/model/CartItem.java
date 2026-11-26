package com.mamatgunshop.model;

import java.math.BigDecimal;

/** Satu baris dalam keranjang: produk (snapshot) + jumlah. */
public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity minimal 1");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity minimal 1");
        }
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
