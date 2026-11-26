package com.mamatgunshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Transaksi yang sudah dikonfirmasi. */
public class Order {

    private String id;
    private LocalDateTime createdAt;
    private Customer customer;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal total;
    private OrderStatus status;

    public Order() {
    }

    public Order(String id, LocalDateTime createdAt, Customer customer,
                 List<OrderItem> items, BigDecimal total, OrderStatus status) {
        this.id = id;
        this.createdAt = createdAt;
        this.customer = customer;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        this.total = total;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
