package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.Order;
import com.mamatgunshop.model.OrderStatus;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Riwayat pesanan + perubahan status. */
public class OrderHistoryDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AppContext context;
    private final Runnable onOrderChanged;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"No. Order", "Tanggal", "Pelanggan", "Total", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextArea detailArea = new JTextArea();
    private List<Order> orders = new ArrayList<>();

    public OrderHistoryDialog(Window owner, AppContext context, Runnable onOrderChanged) {
        super(owner, "Riwayat Pesanan", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onOrderChanged = onOrderChanged;
        setSize(880, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        table.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Daftar Order"));

        detailArea.setEditable(false);
        detailArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Rincian / Struk"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, detailScroll);
        split.setDividerLocation(520);
        add(split, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton processingButton = UiUtils.primary("Set Diproses");
        JButton completeButton = UiUtils.primary("Set Selesai");
        JButton cancelButton = UiUtils.primary("Batalkan Order");
        JButton closeButton = UiUtils.primary("Tutup");
        processingButton.addActionListener(e -> changeStatus(OrderStatus.PROCESSING));
        completeButton.addActionListener(e -> changeStatus(OrderStatus.COMPLETED));
        cancelButton.addActionListener(e -> changeStatus(OrderStatus.CANCELLED));
        closeButton.addActionListener(e -> dispose());
        buttons.add(processingButton);
        buttons.add(completeButton);
        buttons.add(cancelButton);
        buttons.add(closeButton);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        orders = context.orders().findAll();
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getId(),
                    order.getCreatedAt().format(DATE_FORMAT),
                    order.getCustomer() == null ? "-" : order.getCustomer().getName(),
                    Money.format(order.getTotal()),
                    order.getStatus() == null ? "-" : order.getStatus().getLabel()
            });
        }
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            detailArea.setText("Belum ada pesanan.");
        }
        updateDetail();
    }

    private Order selectedOrder() {
        int row = table.getSelectedRow();
        return row >= 0 && row < orders.size() ? orders.get(row) : null;
    }

    private void updateDetail() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        detailArea.setText(context.checkout().buildReceipt(order));
        detailArea.setCaretPosition(0);
    }

    private void changeStatus(OrderStatus status) {
        Order order = selectedOrder();
        if (order == null) {
            UiUtils.showError(this, "Pilih order terlebih dahulu.");
            return;
        }
        try {
            context.orders().updateStatus(order.getId(), status);
            refresh();
            if (onOrderChanged != null) {
                onOrderChanged.run();
            }
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }
}
