package com.mamatgunshop.ui;

import com.mamatgunshop.model.Product;
import com.mamatgunshop.util.Money;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Model tabel untuk menampilkan hasil katalog. */
public class ProductTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "Nama", "Merek", "Tipe", "Kaliber", "Kapasitas", "Harga", "Stok"
    };

    private List<Product> products = new ArrayList<>();

    public void setProducts(List<Product> products) {
        this.products = new ArrayList<>(products);
        fireTableDataChanged();
    }

    public Product productAt(int row) {
        if (row < 0 || row >= products.size()) {
            return null;
        }
        return products.get(row);
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return (columnIndex == 4 || columnIndex == 6) ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product p = products.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> p.getName();
            case 1 -> p.getBrand();
            case 2 -> p.getType() == null ? "-" : p.getType().getLabel();
            case 3 -> p.getCaliber();
            case 4 -> p.getCapacity();
            case 5 -> Money.format(p.getPrice());
            case 6 -> p.getStock();
            default -> "";
        };
    }
}
