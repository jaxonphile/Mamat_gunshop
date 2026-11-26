package com.mamatgunshop.ui;

import com.mamatgunshop.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.GridLayout;

/** Login sederhana untuk membuka panel admin. */
public class AuthDialog extends JDialog {

    private final AuthService authService;
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private boolean authenticated = false;

    public AuthDialog(Window owner, AuthService authService) {
        super(owner, "Login Admin", Dialog.ModalityType.APPLICATION_MODAL);
        this.authService = authService;
        setSize(380, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Kredensial Admin"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton cancelButton = UiUtils.primary("Batal");
        JButton loginButton = UiUtils.primary("Masuk");
        cancelButton.addActionListener(e -> dispose());
        loginButton.addActionListener(e -> attemptLogin());
        buttons.add(cancelButton);
        buttons.add(loginButton);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(loginButton);
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
