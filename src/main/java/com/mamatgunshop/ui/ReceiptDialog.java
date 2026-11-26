package com.mamatgunshop.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Menampilkan struk transaksi dengan opsi simpan & cetak. */
public class ReceiptDialog extends JDialog {

    private final JTextArea area = UiTheme.area(20, 48);

    public ReceiptDialog(Window owner, String receiptText) {
        super(owner, "Struk Transaksi", Dialog.ModalityType.APPLICATION_MODAL);
        setSize(540, 600);
        setMinimumSize(new Dimension(460, 480));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UiTheme.CANVAS);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));
        header.add(UiTheme.heading("Struk transaksi"), BorderLayout.NORTH);
        header.add(UiTheme.muted("Simpan atau cetak untuk arsip lokal."), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        area.setText(receiptText);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretPosition(0);
        JScrollPane scroll = UiTheme.scroll(area);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                UiTheme.cardBorder(), scroll.getBorder()));
        add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JButton saveButton = UiTheme.primary("Simpan .txt");
        JButton printButton = UiTheme.secondary("Cetak");
        JButton closeButton = UiTheme.ghost("Tutup");
        saveButton.addActionListener(e -> save());
        printButton.addActionListener(e -> print());
        closeButton.addActionListener(e -> dispose());
        buttons.add(closeButton);
        buttons.add(printButton);
        buttons.add(saveButton);
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
