package com.mamatgunshop;

import com.mamatgunshop.ui.UiUtils;
import com.mamatgunshop.ui.MainFrame;

import javax.swing.SwingUtilities;

/** Entry point aplikasi Mamat Gunshop. */
public final class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiUtils.configureLookAndFeel();
            AppContext context = new AppContext();
            new MainFrame(context).setVisible(true);
        });
    }

    private Main() {
    }
}
