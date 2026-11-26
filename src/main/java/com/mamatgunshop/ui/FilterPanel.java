package com.mamatgunshop.ui;

import com.mamatgunshop.model.FilterCriteria;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Panel filter dengan checkbox eksplisit dan ringkasan filter aktif (chip). */
public class FilterPanel extends JPanel {

    private final Runnable onChange;
    private final Map<String, JCheckBox> caliberChecks = new LinkedHashMap<>();
    private final Map<GunType, JCheckBox> typeChecks = new LinkedHashMap<>();
    private final Map<String, JCheckBox> brandChecks = new LinkedHashMap<>();

    private final JPanel caliberPanel = new JPanel(new GridLayout(0, 1, 2, 4));
    private final JPanel typePanel = new JPanel(new GridLayout(0, 1, 2, 4));
    private final JPanel brandPanel = new JPanel(new GridLayout(0, 1, 2, 4));
    private final JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

    private final JTextField minPriceField = UiTheme.field();
    private final JTextField maxPriceField = UiTheme.field();
    private final JCheckBox stockOnlyBox = checkBox("Stok tersedia saja");
    private final JLabel validationLabel = UiTheme.muted("");

    private boolean updating = false;

    public FilterPanel(Runnable onChange) {
        this.onChange = onChange;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UiTheme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UiTheme.subheading("Filter katalog");
        title.setAlignmentX(LEFT_ALIGNMENT);
        add(title);

        JPanel caliberSection = section("Kaliber", caliberPanel);
        JPanel typeSection = section("Tipe senjata", typePanel);
        JPanel brandSection = section("Merek", brandPanel);
        caliberSection.setAlignmentX(LEFT_ALIGNMENT);
        typeSection.setAlignmentX(LEFT_ALIGNMENT);
        brandSection.setAlignmentX(LEFT_ALIGNMENT);

        add(caliberSection);
        add(javax.swing.Box.createVerticalStrut(UiTheme.GAP));
        add(typeSection);
        add(javax.swing.Box.createVerticalStrut(UiTheme.GAP));
        add(brandSection);
        add(javax.swing.Box.createVerticalStrut(UiTheme.GAP));
        add(section("Rentang harga", pricePanel()));
        add(javax.swing.Box.createVerticalStrut(UiTheme.GAP));

        JPanel availability = new JPanel(new BorderLayout());
        availability.setBackground(UiTheme.SURFACE);
        availability.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.LINE),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        availability.add(stockOnlyBox, BorderLayout.WEST);
        availability.setAlignmentX(LEFT_ALIGNMENT);
        add(availability);
        add(javax.swing.Box.createVerticalStrut(UiTheme.GAP));

        JPanel active = section("Filter aktif", chipsPanel);
        active.setAlignmentX(LEFT_ALIGNMENT);
        add(active);

        javax.swing.JButton reset = resetButton();
        reset.setAlignmentX(LEFT_ALIGNMENT);
        add(reset);

        validationLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(javax.swing.Box.createVerticalStrut(4));
        add(validationLabel);

        installListeners();
    }

    private JPanel section(String title, JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.SURFACE);
        wrapper.setBorder(UiTheme.sectionBorder(title));
        content.setBackground(UiTheme.SURFACE);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel pricePanel() {
        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 8, 8));
        panel.setBackground(UiTheme.SURFACE);
        panel.add(UiTheme.muted("Minimum"));
        panel.add(UiTheme.muted("Maksimum"));
        panel.add(minPriceField);
        panel.add(maxPriceField);
        return panel;
    }

    private static JCheckBox checkBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setFont(UiTheme.font(13));
        box.setForeground(UiTheme.INK);
        box.setBackground(UiTheme.SURFACE);
        box.setFocusPainted(false);
        return box;
    }

    public void setOptions(List<String> calibers, List<GunType> types, List<String> brands) {
        updating = true;
        Set<String> oldCalibers = selected(caliberChecks);
        Set<GunType> oldTypes = selected(typeChecks);
        Set<String> oldBrands = selected(brandChecks);

        rebuildChecks(caliberPanel, caliberChecks, calibers, oldCalibers);
        rebuildChecks(typePanel, typeChecks, types, oldTypes);
        rebuildChecks(brandPanel, brandChecks, brands, oldBrands);

        updating = false;
        fireChange();
    }

    private <T> void rebuildChecks(JPanel panel, Map<T, JCheckBox> map,
                                   List<T> options, Set<T> keepSelected) {
        map.clear();
        panel.removeAll();
        for (T option : options) {
            JCheckBox box = checkBox(String.valueOf(option));
            box.setSelected(keepSelected.contains(option));
            box.addItemListener(e -> fireChange());
            map.put(option, box);
            panel.add(box);
        }
        panel.revalidate();
        panel.repaint();
    }

    public FilterCriteria getCriteria() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setCalibers(selected(caliberChecks));
        criteria.setTypes(selected(typeChecks));
        criteria.setBrands(selected(brandChecks));
        try {
            criteria.setMinPrice(Money.parse(minPriceField.getText()));
            criteria.setMaxPrice(Money.parse(maxPriceField.getText()));
            validationLabel.setText("");
            validationLabel.setForeground(UiTheme.MUTED);
        } catch (IllegalArgumentException e) {
            criteria.setMinPrice(null);
            criteria.setMaxPrice(null);
            validationLabel.setText("Format harga tidak valid — filter harga diabaikan.");
            validationLabel.setForeground(UiTheme.DANGER);
        }
        criteria.setInStockOnly(stockOnlyBox.isSelected());
        return criteria;
    }

    private <T> Set<T> selected(Map<T, JCheckBox> map) {
        Set<T> result = new LinkedHashSet<>();
        map.forEach((key, box) -> {
            if (box.isSelected()) {
                result.add(key);
            }
        });
        return result;
    }

    private void installListeners() {
        stockOnlyBox.addItemListener(e -> fireChange());
        addLiveListener(minPriceField);
        addLiveListener(maxPriceField);
    }

    private void addLiveListener(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                fireChange();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                fireChange();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                fireChange();
            }
        });
    }

    private void fireChange() {
        rebuildChips();
        if (!updating) {
            onChange.run();
        }
    }

    private void rebuildChips() {
        chipsPanel.removeAll();
        chipsPanel.setBackground(UiTheme.SURFACE);
        boolean any = false;
        any |= addChips(caliberChecks);
        any |= addChips(typeChecks);
        any |= addChips(brandChecks);
        if (stockOnlyBox.isSelected()) {
            chipsPanel.add(chip("Stok tersedia", () -> stockOnlyBox.setSelected(false)));
            any = true;
        }
        if (!minPriceField.getText().isBlank() || !maxPriceField.getText().isBlank()) {
            chipsPanel.add(chip("Harga", () -> {
                minPriceField.setText("");
                maxPriceField.setText("");
            }));
            any = true;
        }
        if (!any) {
            JLabel empty = UiTheme.muted("Belum ada filter dipilih");
            chipsPanel.add(empty);
        }
        chipsPanel.revalidate();
        chipsPanel.repaint();
    }

    private <T> boolean addChips(Map<T, JCheckBox> map) {
        for (Map.Entry<T, JCheckBox> entry : map.entrySet()) {
            if (entry.getValue().isSelected()) {
                chipsPanel.add(chip(String.valueOf(entry.getKey()),
                        () -> entry.getValue().setSelected(false)));
            }
        }
        return map.values().stream().anyMatch(JCheckBox::isSelected);
    }

    private javax.swing.JButton chip(String text, Runnable onRemove) {
        javax.swing.JButton chip = UiTheme.ghost(text + "  ×");
        chip.setToolTipText("Hapus filter " + text);
        chip.addActionListener(e -> {
            updating = true;
            onRemove.run();
            updating = false;
            fireChange();
        });
        return chip;
    }

    public void reset() {
        updating = true;
        caliberChecks.values().forEach(box -> box.setSelected(false));
        typeChecks.values().forEach(box -> box.setSelected(false));
        brandChecks.values().forEach(box -> box.setSelected(false));
        minPriceField.setText("");
        maxPriceField.setText("");
        stockOnlyBox.setSelected(false);
        validationLabel.setText("");
        updating = false;
        fireChange();
    }

    public javax.swing.JButton resetButton() {
        javax.swing.JButton button = UiTheme.ghost("Reset semua filter");
        button.addActionListener(e -> reset());
        return button;
    }
}
