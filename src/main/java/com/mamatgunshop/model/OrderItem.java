package com.mamatgunshop.model;

import java.math.BigDecimal;

/** Baris pesanan; harga di-snapshot pada saat checkout (BR-05). */
public class OrderItem {

    private String productId;
    private String productName;
    private String caliber;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;

    public OrderItem() {
    }

    public OrderItem(String productId, String productName, String caliber,
                     BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.caliber = caliber;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public static OrderItem from(Product product, int quantity) {
        BigDecimal unitPrice = product.getPrice();
        return new OrderItem(
                product.getId(),
                product.getName(),
                product.getCaliber(),
                unitPrice,
                quantity,
                unitPrice.multiply(BigDecimal.valueOf(quantity))
        );
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCaliber() {
        return caliber;
    }

    public void setCaliber(String caliber) {
        this.caliber = caliber;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
