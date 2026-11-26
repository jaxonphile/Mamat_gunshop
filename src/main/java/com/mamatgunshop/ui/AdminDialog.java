package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Panel admin untuk CRUD produk dan update stok. */
public class AdminDialog extends JDialog {

    private final AppContext context;
    private final Runnable onProductsChanged;
    private final List<Product> products = new ArrayList<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nama", "Merek", "Tipe", "Kaliber", "Harga", "Stok"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField brandField = new JTextField();
    private final JComboBox<GunType> typeCombo = new JComboBox<>(GunType.values());
    private final JTextField caliberField = new JTextField();
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JTextField priceField = new JTextField();
    private final JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
    private final JTextArea descriptionArea = new JTextArea(3, 20);

    private String selectedId;

    public AdminDialog(Window owner, AppContext context, Runnable onProductsChanged) {
        super(owner, "Admin — Kelola Produk", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onProductsChanged = onProductsChanged;
        setSize(1020, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        table.setRowHeight(26);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Daftar Produk"));

        JPanel form = buildForm();
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createTitledBorder("Form Produk"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, formScroll);
        split.setDividerLocation(560);
        add(split, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton newButton = UiUtils.primary("Produk Baru");
        JButton saveButton = UiUtils.primary("Simpan");
        JButton deleteButton = UiUtils.primary("Hapus");
        JButton closeButton = UiUtils.primary("Tutup");
        newButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> save());
        deleteButton.addActionListener(e -> delete());
        closeButton.addActionListener(e -> dispose());
        buttons.add(newButton);
        buttons.add(saveButton);
        buttons.add(deleteButton);
        buttons.add(closeButton);
        add(buttons, BorderLayout.SOUTH);

        idField.setEditable(false);
        refresh();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        form.add(labeled("ID:", idField));
        form.add(labeled("Nama:", nameField));
        form.add(labeled("Merek:", brandField));
        form.add(labeled("Tipe:", typeCombo));
        form.add(labeled("Kaliber:", caliberField));
        form.add(labeled("Kapasitas (peluru):", capacitySpinner));
        form.add(labeled("Harga (Rp):", priceField));
        form.add(labeled("Stok:", stockSpinner));
        form.add(labeled("Deskripsi:", new JScrollPane(descriptionArea)));
        return form;
    }

    private JPanel labeled(String label, java.awt.Component field) {
        JPanel panel = new JPanel(new BorderLayout(4, 2));
        panel.setMaximumSize(new Dimension(400, 60));
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void refresh() {
        products.clear();
        products.addAll(context.inventory().findAll());
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getBrand(),
                    p.getType() == null ? "-" : p.getType().getLabel(),
                    p.getCaliber(), Money.format(p.getPrice()), p.getStock()
            });
        }
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            clearForm();
        }
    }

    private Product selectedProduct() {
        int row = table.getSelectedRow();
        return row >= 0 && row < products.size() ? products.get(row) : null;
    }

    private void fillFormFromSelection() {
        Product product = selectedProduct();
        if (product == null) {
            return;
        }
        selectedId = product.getId();
        idField.setText(product.getId());
        nameField.setText(product.getName());
        brandField.setText(product.getBrand());
        typeCombo.setSelectedItem(product.getType());
        caliberField.setText(product.getCaliber());
        capacitySpinner.setValue(product.getCapacity());
        priceField.setText(product.getPrice() == null ? "" : product.getPrice().toPlainString());
        stockSpinner.setValue(product.getStock());
        descriptionArea.setText(product.getDescription() == null ? "" : product.getDescription());
    }

    private void clearForm() {
        selectedId = null;
        idField.setText("(produk baru)");
        nameField.setText("");
        brandField.setText("");
        typeCombo.setSelectedIndex(0);
        caliberField.setText("");
        capacitySpinner.setValue(0);
        priceField.setText("");
        stockSpinner.setValue(0);
        descriptionArea.setText("");
        table.clearSelection();
    }

    private void save() {
        try {
            BigDecimal price = Money.parse(priceField.getText());
            Product product = new Product(
                    selectedId,
                    nameField.getText().trim(),
                    brandField.getText().trim(),
                    (GunType) typeCombo.getSelectedItem(),
                    caliberField.getText().trim(),
                    (Integer) capacitySpinner.getValue(),
                    price,
                    (Integer) stockSpinner.getValue(),
                    descriptionArea.getText().trim(),
                    "");
            context.inventory().save(product);
            refresh();
            if (onProductsChanged != null) {
                onProductsChanged.run();
            }
            UiUtils.showInfo(this, "Produk berhasil disimpan.");
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }

    private void delete() {
        Product product = selectedProduct();
        if (product == null) {
            UiUtils.showError(this, "Pilih produk yang akan dihapus.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus produk \"" + product.getName() + "\"?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        context.inventory().delete(product.getId());
        clearForm();
        refresh();
        if (onProductsChanged != null) {
            onProductsChanged.run();
        }
    }
}
