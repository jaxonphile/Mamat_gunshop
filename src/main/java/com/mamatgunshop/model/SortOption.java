package com.mamatgunshop.model;

/** Pilihan pengurutan katalog. */
public enum SortOption {
    NAME_ASC("Nama A–Z"),
    NAME_DESC("Nama Z–A"),
    PRICE_ASC("Harga Terendah"),
    PRICE_DESC("Harga Tertinggi"),
    CALIBER_ASC("Kaliber A–Z"),
    STOCK_DESC("Stok Terbanyak");

    private final String label;

    SortOption(String label) {
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
