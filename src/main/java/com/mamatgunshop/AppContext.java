package com.mamatgunshop;

import com.mamatgunshop.repository.OrderRepository;
import com.mamatgunshop.repository.ProductRepository;
import com.mamatgunshop.service.AuthService;
import com.mamatgunshop.service.CartService;
import com.mamatgunshop.service.CatalogService;
import com.mamatgunshop.service.CheckoutService;
import com.mamatgunshop.service.InventoryService;
import com.mamatgunshop.service.OrderService;

/** Tempat berkumpulnya seluruh service agar UI tidak membangun dependency sendiri. */
public class AppContext {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CatalogService catalogService;
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final AuthService authService;

    public AppContext() {
        this.productRepository = new ProductRepository();
        this.orderRepository = new OrderRepository();
        this.catalogService = new CatalogService(productRepository);
        this.cartService = new CartService(productRepository);
        this.checkoutService = new CheckoutService(productRepository, orderRepository, cartService);
        this.orderService = new OrderService(orderRepository);
        this.inventoryService = new InventoryService(productRepository);
        this.authService = new AuthService();
    }

    public CatalogService catalog() {
        return catalogService;
    }

    public CartService cart() {
        return cartService;
    }

    public CheckoutService checkout() {
        return checkoutService;
    }

    public OrderService orders() {
        return orderService;
    }

    public InventoryService inventory() {
        return inventoryService;
    }

    public AuthService auth() {
        return authService;
    }
}
