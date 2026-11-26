package com.mamatgunshop.ui;

import com.mamatgunshop.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/** Login sederhana untuk membuka panel admin. */
public class AuthDialog extends JDialog {

    private final AuthService authService;
    private final JTextField usernameField = UiTheme.field();
    private final JPasswordField passwordField = UiTheme.passwordField();
    private boolean authenticated = false;

    public AuthDialog(Window owner, AuthService authService) {
        super(owner, "Login Admin", Dialog.ModalityType.APPLICATION_MODAL);
        this.authService = authService;

        setSize(420, 270);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.CANVAS);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));
        header.add(UiTheme.heading("Akses admin"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Masuk untuk mengelola produk dan stok."), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.SURFACE);
        form.setBorder(UiTheme.sectionBorder("Kredensial"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        addRow(form, gbc, 0, "Username", usernameField);
        addRow(form, gbc, 1, "Password", passwordField);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(UiTheme.CANVAS);
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 8, 18));
        content.add(form, BorderLayout.NORTH);
        content.add(UiTheme.muted("Demo lokal — admin / admin123"), BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JButton cancelButton = UiTheme.secondary("Batal");
        JButton loginButton = UiTheme.primary("Masuk");
        cancelButton.addActionListener(e -> dispose());
        loginButton.addActionListener(e -> attemptLogin());
        buttons.add(cancelButton);
        buttons.add(loginButton);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
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

    private void attemptLogin() {
        String password = new String(passwordField.getPassword());
        if (authService.login(usernameField.getText(), password)) {
            authenticated = true;
            dispose();
        } else {
            UiUtils.showError(this, "Username atau password salah.");
            passwordField.setText("");
        }
    }

    public static boolean show(Window owner, AuthService authService) {
        AuthDialog dialog = new AuthDialog(owner, authService);
        dialog.setVisible(true);
        return dialog.authenticated;
    }
}
