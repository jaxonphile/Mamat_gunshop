package com.mamatgunshop.service;

import com.mamatgunshop.model.Order;
import com.mamatgunshop.model.OrderStatus;
import com.mamatgunshop.repository.OrderRepository;

import java.util.List;

/** Operasi baca & ubah status untuk riwayat pesanan. */
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public void updateStatus(String orderId, OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status tidak boleh null");
        }
        repository.updateStatus(orderId, status);
    }
}
