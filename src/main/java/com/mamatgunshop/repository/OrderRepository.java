package com.mamatgunshop.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mamatgunshop.model.Order;
import com.mamatgunshop.model.OrderStatus;
import com.mamatgunshop.util.DataPaths;
import com.mamatgunshop.util.JsonSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/** Menyimpan dan memuat riwayat pesanan dari JSON. */
public class OrderRepository {

    private static final Logger LOG = Logger.getLogger(OrderRepository.class.getName());
    private static final TypeReference<List<Order>> LIST_TYPE = new TypeReference<>() {
    };

    private final Path file;
    private final List<Order> orders = new ArrayList<>();

    public OrderRepository() {
        this(DataPaths.ordersFile());
    }

    public OrderRepository(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        try {
            DataPaths.ensureParent(file);
            if (!Files.exists(file)) {
                JsonSupport.mapper().writeValue(file.toFile(), List.of());
            }
            List<Order> loaded = JsonSupport.mapper().readValue(file.toFile(), LIST_TYPE);
            orders.clear();
            if (loaded != null) {
                orders.addAll(loaded);
            }
        } catch (IOException e) {
            LOG.warning("Gagal memuat " + file + " — memakai daftar kosong. Detail: " + e.getMessage());
            orders.clear();
        }
    }

    public synchronized void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order tidak boleh null");
        }
        orders.add(order);
        persist();
    }

    /** Pesanan terbaru tampil lebih dulu. */
    public synchronized List<Order> findAll() {
        return orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    public synchronized Optional<Order> findById(String id) {
        return orders.stream().filter(o -> o.getId().equalsIgnoreCase(id)).findFirst();
    }

    public synchronized void updateStatus(String id, OrderStatus status) {
        Order order = orders.stream()
                .filter(o -> o.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan: " + id));
        order.setStatus(status);
        persist();
    }

    public synchronized void persist() {
        try {
            JsonSupport.mapper().writeValue(file.toFile(), orders);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan order ke " + file, e);
        }
    }
}
