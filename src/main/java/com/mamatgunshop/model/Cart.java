package com.mamatgunshop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Keranjang belanja berada di memori dan dipakai sebelum checkout. */
public class Cart {

    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity minimal 1");
        }
        CartItem existing = items.get(product.getId());
        int desired = quantity + (existing == null ? 0 : existing.getQuantity());
        if (desired > product.getStock()) {
            throw new IllegalArgumentException("Stok tidak mencukupi. Tersedia: " + product.getStock());
        }
        items.put(product.getId(), new CartItem(product.copy(), desired));
    }

    public void updateQuantity(String productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Produk tidak ada di keranjang: " + productId);
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity minimal 1");
        }
        if (quantity > item.getProduct().getStock()) {
            throw new IllegalArgumentException("Stok tidak mencukupi. Tersedia: " + item.getProduct().getStock());
        }
        item.setQuantity(quantity);
    }

    public void remove(String productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public CartItem get(String productId) {
        return items.get(productId);
    }

    public Collection<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public List<CartItem> getItemList() {
        return List.copyOf(items.values());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotal() {
        return items.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
