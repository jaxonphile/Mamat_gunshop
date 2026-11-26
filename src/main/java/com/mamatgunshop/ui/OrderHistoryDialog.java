package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.Order;
import com.mamatgunshop.model.OrderStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Riwayat pesanan + perubahan status. */
public class OrderHistoryDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AppContext context;
    private final Runnable onOrderChanged;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"No. order", "Tanggal", "Pelanggan", "Total", "Status"}, 0) {
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 ? BigDecimal.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextArea detailArea = UiTheme.area(14, 40);
    private final JLabel hintLabel = UiTheme.muted("Klik baris untuk melihat rincian order.");
    private List<Order> orders = new ArrayList<>();

    public OrderHistoryDialog(Window owner, AppContext context, Runnable onOrderChanged) {
        super(owner, "Riwayat Pesanan", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onOrderChanged = onOrderChanged;

        setSize(900, 560);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.CANVAS);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));
        header.add(UiTheme.heading("Riwayat pesanan"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Perbarui status order setelah diproses."), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        UiTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
            }
        });

        JScrollPane tableScroll = UiTheme.scroll(table);
        tableScroll.setBorder(UiTheme.sectionBorder("Daftar order"));

        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane detailScroll = UiTheme.scroll(detailArea);
        detailScroll.setBorder(UiTheme.sectionBorder("Rincian / struk"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, detailScroll);
        split.setDividerLocation(540);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);
        JButton processingButton = UiTheme.secondary("Set diproses");
        JButton completeButton = UiTheme.primary("Set selesai");
        JButton cancelButton = UiTheme.danger("Batalkan order");
        JButton closeButton = UiTheme.ghost("Tutup");
        processingButton.addActionListener(e -> changeStatus(OrderStatus.PROCESSING));
        completeButton.addActionListener(e -> changeStatus(OrderStatus.COMPLETED));
        cancelButton.addActionListener(e -> changeStatus(OrderStatus.CANCELLED));
        closeButton.addActionListener(e -> dispose());
        buttons.add(processingButton);
        buttons.add(completeButton);
        buttons.add(cancelButton);
        buttons.add(closeButton);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(UiTheme.SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        footer.add(buttons, BorderLayout.WEST);
        footer.add(hintLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

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
                    order.getTotal(),
                    order.getStatus() == null ? "-" : order.getStatus().getLabel()
            });
        }
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            detailArea.setText("Belum ada pesanan.");
            hintLabel.setText("Belum ada order tersimpan.");
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
        hintLabel.setText("Order dipilih: " + order.getId());
    }

    private void changeStatus(OrderStatus status) {
        Order order = selectedOrder();
        if (order == null) {
            hintLabel.setText("Pilih order terlebih dahulu.");
            return;
        }
        try {
            context.orders().updateStatus(order.getId(), status);
            refresh();
            if (onOrderChanged != null) {
                onOrderChanged.run();
            }
            hintLabel.setText("Status " + order.getId() + " diperbarui menjadi "
                    + status.getLabel() + ".");
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }
}
