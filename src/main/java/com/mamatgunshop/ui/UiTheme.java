package com.mamatgunshop.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

/** Sistem visual tunggal: warna, tipografi, spacing, dan komponen. */
public final class UiTheme {

    public static final Color INK = new Color(28, 37, 46);
    public static final Color MUTED = new Color(94, 108, 122);
    public static final Color SURFACE = Color.WHITE;
    public static final Color CANVAS = new Color(246, 248, 250);
    public static final Color LINE = new Color(219, 225, 231);
    public static final Color ROW_ALT = new Color(250, 251, 252);
    public static final Color ACCENT = new Color(20, 92, 105);
    public static final Color ACCENT_DARK = new Color(14, 70, 80);
    public static final Color ACCENT_SOFT = new Color(225, 239, 241);
    public static final Color DANGER = new Color(162, 52, 52);
    public static final Color SUCCESS = new Color(35, 108, 79);
    public static final Color WARNING = new Color(146, 94, 27);

    public static final int GAP = 8;
    public static final int PAD = 16;

    private static final Font BASE = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    /** Pasang Look & Feel yang stabil lalu timpa default-nya dengan palet kita. */
    public static void install() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing default masih usable
            }
        }
        UIManager.put("Panel.background", CANVAS);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("List.background", SURFACE);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", INK);
        UIManager.put("Table.selectionBackground", ACCENT_SOFT);
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("TableHeader.background", CANVAS);
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("Label.foreground", INK);
        UIManager.put("Button.foreground", INK);
    }

    public static Font font(int size) {
        return BASE.deriveFont((float) size);
    }

    public static Font font(int size, int style) {
        return BASE.deriveFont(style, (float) size);
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(17, Font.BOLD));
        label.setForeground(INK);
        return label;
    }

    public static JLabel subheading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(14, Font.BOLD));
        label.setForeground(INK);
        return label;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(13));
        label.setForeground(INK);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(12));
        label.setForeground(MUTED);
        return label;
    }

    public static JButton primary(String text) {
        JButton button = new JButton(text);
        button.setFont(font(13, Font.BOLD));
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton secondary(String text) {
        JButton button = new JButton(text);
        button.setFont(font(13));
        button.setBackground(SURFACE);
        button.setForeground(INK);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton danger(String text) {
        JButton button = new JButton(text);
        button.setFont(font(13));
        button.setBackground(SURFACE);
        button.setForeground(DANGER);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 176, 176)),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton ghost(String text) {
        JButton button = new JButton(text);
        button.setFont(font(12));
        button.setBackground(new Color(0, 0, 0, 0));
        button.setForeground(ACCENT);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void styleComboBox(javax.swing.JComboBox<?> combo) {
        combo.setFont(font(13));
        combo.setBackground(SURFACE);
        combo.setForeground(INK);
    }

    public static JTextField field() {
        JTextField field = new JTextField();
        field.setFont(font(13));
        field.setForeground(INK);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        return field;
    }

    public static javax.swing.JPasswordField passwordField() {
        javax.swing.JPasswordField field = new javax.swing.JPasswordField();
        field.setFont(font(13));
        field.setForeground(INK);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        return field;
    }

    public static void styleSpinner(javax.swing.JSpinner spinner) {
        spinner.setFont(font(13));
        spinner.setBorder(BorderFactory.createLineBorder(LINE));
        java.awt.Component editor = spinner.getEditor();
        if (editor instanceof JTextField text) {
            text.setFont(font(13));
            text.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));
        }
    }

    public static JTextArea area(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setFont(font(13));
        area.setForeground(INK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return area;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    }

    public static JPanel card() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(cardBorder());
        return panel;
    }

    public static JScrollPane scroll(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        return scroll;
    }

    public static Border sectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(LINE),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        font(12, Font.BOLD), MUTED),
                BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    public static TableCellRenderer tableRenderer() {
        return new ModelTableCellRenderer();
    }

    public static TableCellRenderer headerRenderer() {
        return new ModelHeaderRenderer();
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(SURFACE);
        table.setForeground(INK);
        table.setFont(font(13));
        table.setSelectionBackground(ACCENT_SOFT);
        table.setSelectionForeground(INK);
        int headerWidth = table.getTableHeader().getPreferredSize().width;
        table.getTableHeader().setPreferredSize(new Dimension(headerWidth, 32));
        table.getTableHeader().setDefaultRenderer(headerRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        TableCellRenderer renderer = tableRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(java.math.BigDecimal.class, renderer);
    }

    private static final class ModelHeaderRenderer extends DefaultTableCellRenderer {
        ModelHeaderRenderer() {
            setFont(font(12, Font.BOLD));
            setForeground(MUTED);
            setBackground(CANVAS);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    private static final class ModelTableCellRenderer extends DefaultTableCellRenderer {
        ModelTableCellRenderer() {
            setFont(font(13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (value instanceof java.math.BigDecimal money) {
                setText(com.mamatgunshop.util.Money.format(money));
            } else {
                setText(value == null ? "" : value.toString());
            }

            boolean numeric = value instanceof Number || value instanceof java.math.BigDecimal;
            setHorizontalAlignment(numeric ? JLabel.RIGHT : JLabel.LEFT);

            if (selected) {
                setBackground(ACCENT_SOFT);
                setForeground(INK);
            } else {
                setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                boolean outOfStock = "0".equals(String.valueOf(value))
                        && table.getColumnName(column).toLowerCase().contains("stok");
                setForeground(outOfStock ? DANGER : INK);
            }
            return this;
        }
    }

    private UiTheme() {
    }
}
