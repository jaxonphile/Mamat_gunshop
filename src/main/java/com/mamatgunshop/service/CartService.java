package com.mamatgunshop.service;

import com.mamatgunshop.model.Cart;
import com.mamatgunshop.model.CartItem;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.repository.ProductRepository;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Mengelola isi keranjang dan memvalidasi ketersediaan stok. */
public class CartService {

    private final ProductRepository repository;
    private final Cart cart = new Cart();

    public CartService(ProductRepository repository) {
        this.repository = repository;
    }

    public Cart getCart() {
        return cart;
    }

    public void add(String productId, int quantity) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan: " + productId));
        cart.add(product, quantity);
    }

    public void updateQuantity(String productId, int quantity) {
        cart.updateQuantity(productId, quantity);
    }

    public void remove(String productId) {
        cart.remove(productId);
    }

    public void clear() {
        cart.clear();
    }

    /** Dipakai sebelum checkout: mengecek ulang stok terhadap data terbaru repo. */
    public void validateStock() {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Keranjang masih kosong");
        }
        Map<String, Product> latest = repository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (CartItem item : cart.getItems()) {
            Product current = latest.get(item.getProduct().getId());
            if (current == null) {
                throw new IllegalArgumentException("Produk di keranjang sudah tidak tersedia: "
                        + item.getProduct().getName());
            }
            if (current.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Stok " + current.getName()
                        + " tersisa " + current.getStock() + ", diminta " + item.getQuantity());
            }
        }
    }
}
