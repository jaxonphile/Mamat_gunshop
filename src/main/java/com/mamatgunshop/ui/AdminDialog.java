package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Window;
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
        public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 5 -> BigDecimal.class;
                case 6 -> Integer.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField idField = UiTheme.field();
    private final JTextField nameField = UiTheme.field();
    private final JTextField brandField = UiTheme.field();
    private final JComboBox<GunType> typeCombo = new JComboBox<>(GunType.values());
    private final JTextField caliberField = UiTheme.field();
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JTextField priceField = UiTheme.field();
    private final JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
    private final JTextArea descriptionArea = UiTheme.area(3, 20);
    private final JLabel statusLabel = UiTheme.muted("Pilih produk untuk mengubah, atau klik Produk baru.");

    private String selectedId;

    public AdminDialog(Window owner, AppContext context, Runnable onProductsChanged) {
        super(owner, "Admin — Kelola Produk", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onProductsChanged = onProductsChanged;

        setSize(1080, 660);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.CANVAS);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));
        header.add(UiTheme.heading("Kelola produk"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Perubahan tersimpan otomatis ke products.json."),
                BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        UiTheme.styleTable(table);
        UiTheme.styleComboBox(typeCombo);
        UiTheme.styleSpinner(capacitySpinner);
        UiTheme.styleSpinner(stockSpinner);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });

        JScrollPane tableScroll = UiTheme.scroll(table);
        tableScroll.setBorder(UiTheme.sectionBorder("Daftar produk"));

        JScrollPane formScroll = UiTheme.scroll(buildForm());
        formScroll.setBorder(UiTheme.sectionBorder("Form produk"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, formScroll);
        split.setDividerLocation(600);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);
        JButton newButton = UiTheme.secondary("Produk baru");
        JButton saveButton = UiTheme.primary("Simpan");
        JButton deleteButton = UiTheme.danger("Hapus");
        JButton closeButton = UiTheme.ghost("Tutup");
        newButton.addActionListener(e -> {
            clearForm();
            setStatus("Form siap untuk produk baru.");
        });
        saveButton.addActionListener(e -> save());
        deleteButton.addActionListener(e -> delete());
        closeButton.addActionListener(e -> dispose());
        buttons.add(newButton);
        buttons.add(saveButton);
        buttons.add(deleteButton);
        buttons.add(closeButton);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(UiTheme.SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        footer.add(buttons, BorderLayout.WEST);
        footer.add(statusLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        idField.setEditable(false);
        idField.setForeground(UiTheme.MUTED);
        refresh();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UiTheme.SURFACE);
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(labeled("ID", idField));
        form.add(labeled("Nama", nameField));
        form.add(labeled("Merek", brandField));
        form.add(labeled("Tipe", typeCombo));
        form.add(labeled("Kaliber", caliberField));
        form.add(labeled("Kapasitas (peluru)", capacitySpinner));
        form.add(labeled("Harga (Rp)", priceField));
        form.add(labeled("Stok", stockSpinner));
        form.add(labeled("Deskripsi", new JScrollPane(descriptionArea)));
        return form;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(4, 3));
        panel.setBackground(UiTheme.SURFACE);
        panel.setMaximumSize(new Dimension(420, field instanceof JScrollPane ? 88 : 58));
        panel.add(UiTheme.label(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void setStatus(String message) {
        setStatus(message, UiTheme.MUTED);
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void refresh() {
        products.clear();
        products.addAll(context.inventory().findAll());
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getBrand(),
                    p.getType() == null ? "-" : p.getType().getLabel(),
                    p.getCaliber(), p.getPrice(), p.getStock()
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
            setStatus("Produk \"" + product.getName() + "\" berhasil disimpan.", UiTheme.SUCCESS);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), UiTheme.DANGER);
            UiUtils.showError(this, ex.getMessage());
        }
    }

    private void delete() {
        Product product = selectedProduct();
        if (product == null) {
            setStatus("Pilih produk yang akan dihapus.", UiTheme.DANGER);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus produk \"" + product.getName() + "\"?\nTindakan ini tidak dapat dibatalkan.",
                "Konfirmasi hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        context.inventory().delete(product.getId());
        clearForm();
        refresh();
        if (onProductsChanged != null) {
            onProductsChanged.run();
        }
        setStatus("Produk \"" + product.getName() + "\" dihapus.", UiTheme.DANGER);
    }
}
