package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.CartItem;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/** Dialog keranjang: ubah qty, hapus item, lanjut ke checkout. */
public class CartDialog extends JDialog {

    private final AppContext context;
    private final Runnable onCartChanged;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Produk", "Kaliber", "Harga", "Qty", "Subtotal"}, 0) {
        @Override
        public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 2, 4 -> java.math.BigDecimal.class;
                case 3 -> Integer.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel totalLabel = UiTheme.subheading("Total: Rp 0");

    private final JButton plusButton = UiTheme.secondary("+ Tambah");
    private final JButton minusButton = UiTheme.secondary("− Kurangi");
    private final JButton removeButton = UiTheme.danger("Hapus item");
    private final JButton checkoutButton = UiTheme.primary("Lanjut checkout");
    private final JButton closeButton = UiTheme.ghost("Tutup");

    private List<CartItem> currentItems = new ArrayList<>();

    public CartDialog(Window owner, AppContext context, Runnable onCartChanged) {
        super(owner, "Keranjang Belanja", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onCartChanged = onCartChanged;

        setSize(760, 480);
        setMinimumSize(new Dimension(640, 400));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.CANVAS);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        header.add(UiTheme.heading("Keranjang belanja"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Periksa jumlah item sebelum melanjutkan ke checkout."),
                BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        UiTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
            }
        });
        add(UiTheme.scroll(table), BorderLayout.CENTER);

        JPanel summary = new JPanel(new BorderLayout(12, 0));
        summary.setBackground(UiTheme.SURFACE);
        JPanel editing = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editing.setBackground(UiTheme.SURFACE);
        editing.add(plusButton);
        editing.add(minusButton);
        editing.add(removeButton);
        totalLabel.setFont(UiTheme.font(15, Font.BOLD));
        summary.add(editing, BorderLayout.WEST);
        summary.add(totalLabel, BorderLayout.EAST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UiTheme.SURFACE);
        actions.add(closeButton);
        actions.add(checkoutButton);

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setBackground(UiTheme.SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 14, 18)));
        footer.add(summary, BorderLayout.NORTH);
        footer.add(actions, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        plusButton.addActionListener(e -> changeQuantity(1));
        minusButton.addActionListener(e -> changeQuantity(-1));
        removeButton.addActionListener(e -> removeSelected());
        checkoutButton.addActionListener(e -> openCheckout());
        closeButton.addActionListener(e -> dispose());

        refresh();
    }

    private void refresh() {
        currentItems = context.cart().getCart().getItemList();
        tableModel.setRowCount(0);
        for (CartItem item : currentItems) {
            tableModel.addRow(new Object[]{
                    item.getProduct().getName(),
                    item.getProduct().getCaliber(),
                    item.getProduct().getPrice(),
                    item.getQuantity(),
                    item.getSubtotal()
            });
        }
        totalLabel.setText("Total: " + Money.format(context.cart().getCart().getTotal()));
        updateButtons();
    }

    private void updateButtons() {
        boolean selected = selectedItem() != null;
        plusButton.setEnabled(selected);
        minusButton.setEnabled(selected);
        removeButton.setEnabled(selected);
        checkoutButton.setEnabled(!currentItems.isEmpty());
    }

    private CartItem selectedItem() {
        int row = table.getSelectedRow();
        return row >= 0 && row < currentItems.size() ? currentItems.get(row) : null;
    }

    private void changeQuantity(int delta) {
        CartItem item = selectedItem();
        if (item == null) {
            return;
        }
        try {
            context.cart().updateQuantity(item.getProduct().getId(), item.getQuantity() + delta);
            refresh();
            onCartChanged.run();
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }

    private void removeSelected() {
        CartItem item = selectedItem();
        if (item == null) {
            return;
        }
        context.cart().remove(item.getProduct().getId());
        refresh();
        onCartChanged.run();
    }

    private void openCheckout() {
        new CheckoutDialog(this, context, () -> {
            refresh();
            onCartChanged.run();
        }).setVisible(true);
    }
}
