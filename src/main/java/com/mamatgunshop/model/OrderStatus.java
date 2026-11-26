package com.mamatgunshop.model;

/** Status lifecycle sebuah pesanan. */
public enum OrderStatus {
    PROCESSING("Diproses"),
    COMPLETED("Selesai"),
    CANCELLED("Dibatalkan");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
