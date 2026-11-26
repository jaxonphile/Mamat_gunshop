package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.Customer;
import com.mamatgunshop.model.Order;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/** Form checkout: identitas pembeli + validasi usia/KTP. */
public class CheckoutDialog extends JDialog {

    private final AppContext context;
    private final Runnable onSuccess;
    private final JTextField nameField = UiTheme.field();
    private final JTextField ktpField = UiTheme.field();
    private final JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(21, 21, 100, 1));

    public CheckoutDialog(Window owner, AppContext context, Runnable onSuccess) {
        super(owner, "Checkout — Data Pembeli", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onSuccess = onSuccess;

        setSize(480, 320);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.CANVAS);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));
        header.add(UiTheme.heading("Data pembeli"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Pastikan data sesuai KTP sebelum membuat order."),
                BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        ((PlainDocument) ktpField.getDocument()).setDocumentFilter(new DigitFilter(16));
        UiTheme.styleSpinner(ageSpinner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.SURFACE);
        form.setBorder(UiTheme.sectionBorder("Identitas"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        addRow(form, gbc, 0, "Nama lengkap", nameField);
        addRow(form, gbc, 1, "No. KTP (16 digit)", ktpField);
        addRow(form, gbc, 2, "Usia (min. 21 tahun)", ageSpinner);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(UiTheme.CANVAS);
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 8, 18));
        content.add(form, BorderLayout.NORTH);
        content.add(UiTheme.muted("KTP hanya berisi angka. Pembayaran dan verifikasi bersifat simulasi."),
                BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JButton cancelButton = UiTheme.secondary("Batal");
        JButton payButton = UiTheme.primary("Buat order");
        cancelButton.addActionListener(e -> dispose());
        payButton.addActionListener(e -> pay());
        buttons.add(cancelButton);
        buttons.add(payButton);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(payButton);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(UiTheme.label(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void pay() {
        try {
            Customer customer = new Customer(nameField.getText(), ktpField.getText().trim(),
                    (Integer) ageSpinner.getValue());
            Order order = context.checkout().checkout(customer);
            new ReceiptDialog(this, context.checkout().buildReceipt(order)).setVisible(true);
            if (onSuccess != null) {
                onSuccess.run();
            }
            dispose();
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }

    /** Mencegah error lebih awal: hanya digit dan maksimal 16 karakter. */
    private static final class DigitFilter extends DocumentFilter {
        private final int maxLength;

        DigitFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            String digits = text == null ? "" : text.replaceAll("\\D", "");
            int room = maxLength - (fb.getDocument().getLength() - length);
            if (room > 0) {
                super.replace(fb, offset, length, digits.substring(0, Math.min(digits.length(), room)), attrs);
            }
        }
    }
}
