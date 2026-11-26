package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.Customer;
import com.mamatgunshop.model.Order;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.GridLayout;

/** Form checkout: identitas pembeli + validasi usia/KTP. */
public class CheckoutDialog extends JDialog {

    private final AppContext context;
    private final Runnable onSuccess;
    private final JTextField nameField = new JTextField(20);
    private final JTextField ktpField = new JTextField(16);
    private final JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(21, 17, 100, 1));

    public CheckoutDialog(Window owner, AppContext context, Runnable onSuccess) {
        super(owner, "Checkout — Data Pembeli", Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.onSuccess = onSuccess;
        setSize(460, 280);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Identitas Pembeli"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        form.add(new JLabel("Nama lengkap:"));
        form.add(nameField);
        form.add(new JLabel("No. KTP (16 digit):"));
        form.add(ktpField);
        form.add(new JLabel("Usia (min. 21 tahun):"));
        form.add(ageSpinner);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton cancelButton = UiUtils.primary("Batal");
        JButton payButton = UiUtils.primary("Buat Order");
        cancelButton.addActionListener(e -> dispose());
        payButton.addActionListener(e -> pay());
        buttons.add(cancelButton);
        buttons.add(payButton);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(payButton);
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
}
