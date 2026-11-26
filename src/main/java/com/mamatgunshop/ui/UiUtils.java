package com.mamatgunshop.ui;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.Component;

/** Kompatibilitas helper lama; sebagian besar UI memakai UiTheme langsung. */
public final class UiUtils {

    public static void configureLookAndFeel() {
        UiTheme.install();
    }

    public static JButton primary(String text) {
        return UiTheme.primary(text);
    }

    public static JComponent titled(String title, JComponent content) {
        content.setBorder(UiTheme.sectionBorder(title));
        return content;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Mamat Gunshop — Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Mamat Gunshop", JOptionPane.INFORMATION_MESSAGE);
    }

    private UiUtils() {
    }
}
