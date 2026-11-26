package com.mamatgunshop.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import java.awt.Component;

/** Helper kecil untuk konsistensi tampilan Swing. */
public final class UiUtils {

    public static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception alsoIgnored) {
                // bawaan Swing tetap usable
            }
        }
    }

    public static JButton primary(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        return button;
    }

    public static JComponent titled(String title, JComponent content) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        content.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
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
