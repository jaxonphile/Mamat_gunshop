package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.CartItem;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/** Dialog keranjang: ubah qty, hapus item, lanju ke checkout. */
public class CartDialog extends JDialog {

    private final AppContext context;
    private final Runnable onCartChanged;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Nama", "Kaliber", "Harga", "Qty", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel totalLabel = new JLabel("Total: Rp 0");

    private final JButton plusButton = UiUtils.primary("+ Tambah Qty");
    private final JButton minusButton = UiUtils.primary("- Kurangi Qty");
    private final JButton removeButton = UiUtils.primary("Hapus Item");
    private final JButton checkoutButton = UiUtils.primary("Checkout");

    private List<CartItem> currentItems = new ArrayList<>();

    public CartDialog(Window owner, AppContext context, Runnable onCartChanged) {
        super(owner, "Keranjang Belanja", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onCartChanged = onCartChanged;
        setSize(720, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        table.setRowHeight(26);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(4, 4));
        south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        buttons.add(plusButton);
        buttons.add(minusButton);
        buttons.add(removeButton);
        south.add(buttons, BorderLayout.WEST);
        south.add(totalLabel, BorderLayout.EAST);

        JPanel checkoutRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        JButton closeButton = UiUtils.primary("Tutup");
        checkoutRow.add(closeButton);
        checkoutRow.add(checkoutButton);
        south.add(checkoutRow, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

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
                    Money.format(item.getProduct().getPrice()),
                    item.getQuantity(),
                    Money.format(item.getSubtotal())
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
