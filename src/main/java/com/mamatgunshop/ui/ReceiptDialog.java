package com.mamatgunshop.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Menampilkan struk transaksi dengan opsi simpan & cetak. */
public class ReceiptDialog extends JDialog {

    private final JTextArea area = new JTextArea();

    public ReceiptDialog(Window owner, String receiptText) {
        super(owner, "Struk Transaksi", Dialog.ModalityType.APPLICATION_MODAL);
        setSize(500, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        area.setText(receiptText);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton saveButton = UiUtils.primary("Simpan .txt");
        JButton printButton = UiUtils.primary("Cetak");
        JButton closeButton = UiUtils.primary("Tutup");
        saveButton.addActionListener(e -> save());
        printButton.addActionListener(e -> print());
        closeButton.addActionListener(e -> dispose());
        buttons.add(saveButton);
        buttons.add(printButton);
        buttons.add(closeButton);
        add(buttons, BorderLayout.SOUTH);
    }

    private void save() {
        JFileChooser chooser = new JFileChooser();
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        chooser.setSelectedFile(new File("struk-mamat-" + stamp + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(chooser.getSelectedFile().toPath(), area.getText(), StandardCharsets.UTF_8);
            UiUtils.showInfo(this, "Struk tersimpan di:\n" + chooser.getSelectedFile());
        } catch (Exception ex) {
            UiUtils.showError(this, "Gagal menyimpan struk: " + ex.getMessage());
        }
    }

    private void print() {
        try {
            area.print();
        } catch (Exception ex) {
            UiUtils.showError(this, "Gagal mencetak: " + ex.getMessage());
        }
    }
}
