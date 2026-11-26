package com.mamatgunshop.ui;

import com.mamatgunshop.model.FilterCriteria;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.SortOption;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;

/** Panel filter kiri: pencarian + kriteria + sortir. */
public class FilterPanel extends JPanel {

    private final Runnable onChange;
    private final JTextField searchField = new JTextField();
    private final JTextField minPriceField = new JTextField();
    private final JTextField maxPriceField = new JTextField();
    private final JCheckBox stockOnlyBox = new JCheckBox("Stok tersedia saja");
    private final JComboBox<SortOption> sortCombo = new JComboBox<>(SortOption.values());

    private final DefaultListModel<String> caliberModel = new DefaultListModel<>();
    private final DefaultListModel<GunType> typeModel = new DefaultListModel<>();
    private final DefaultListModel<String> brandModel = new DefaultListModel<>();
    private final JList<String> caliberList = new JList<>(caliberModel);
    private final JList<GunType> typeList = new JList<>(typeModel);
    private final JList<String> brandList = new JList<>(brandModel);

    private boolean updating = false;

    public FilterPanel(Runnable onChange) {
        this.onChange = onChange;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(searchBlock());
        add(UiUtils.titled("Kaliber", listScroll(caliberList, 5)));
        add(UiUtils.titled("Tipe Senjata", listScroll(typeList, 4)));
        add(UiUtils.titled("Merek", listScroll(brandList, 5)));
        add(priceBlock());
        add(stockOnlyBox);
        add(sortBlock());

        JButton resetButton = UiUtils.primary("Reset Filter");
        resetButton.addActionListener(e -> reset());
        add(resetButton);

        addLiveListener(searchField);
        addLiveListener(minPriceField);
        addLiveListener(maxPriceField);
        installSelectionListener(caliberList);
        installSelectionListener(typeList);
        installSelectionListener(brandList);
        stockOnlyBox.addActionListener(e -> fireChange());
        sortCombo.addActionListener(e -> fireChange());
    }

    private JPanel searchBlock() {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cari produk"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        panel.add(new JLabel("Nama / merek / kaliber:"), java.awt.BorderLayout.NORTH);
        panel.add(searchField, java.awt.BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane listScroll(JList<?> list, int rows) {
        list.setVisibleRowCount(rows);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(220, rows * 24 + 8));
        return scroll;
    }

    private JPanel priceBlock() {
        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 4, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Rentang harga (Rp)"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        panel.add(new JLabel("Minimum"));
        panel.add(new JLabel("Maksimum"));
        panel.add(minPriceField);
        panel.add(maxPriceField);
        return panel;
    }

    private JPanel sortBlock() {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 2));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Urutkan"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        panel.add(sortCombo, java.awt.BorderLayout.CENTER);
        return panel;
    }

    public void setOptions(List<String> calibers, List<GunType> types, List<String> brands) {
        updating = true;
        List<String> oldCalibers = caliberList.getSelectedValuesList();
        List<GunType> oldTypes = typeList.getSelectedValuesList();
        List<String> oldBrands = brandList.getSelectedValuesList();

        caliberModel.clear();
        calibers.forEach(caliberModel::addElement);
        typeModel.clear();
        types.forEach(typeModel::addElement);
        brandModel.clear();
        brands.forEach(brandModel::addElement);

        for (String value : oldCalibers) {
            if (calibers.contains(value)) {
                caliberList.setSelectedValue(value, false);
            }
        }
        for (GunType value : oldTypes) {
            if (types.contains(value)) {
                typeList.setSelectedValue(value, false);
            }
        }
        for (String value : oldBrands) {
            if (brands.contains(value)) {
                brandList.setSelectedValue(value, false);
            }
        }
        updating = false;
        fireChange();
    }

    public FilterCriteria getCriteria() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setKeyword(searchField.getText());
        criteria.setCalibers(new HashSet<>(caliberList.getSelectedValuesList()));
        criteria.setTypes(new HashSet<>(typeList.getSelectedValuesList()));
        criteria.setBrands(new HashSet<>(brandList.getSelectedValuesList()));
        try {
            criteria.setMinPrice(Money.parse(minPriceField.getText()));
            criteria.setMaxPrice(Money.parse(maxPriceField.getText()));
        } catch (IllegalArgumentException ignored) {
            // input belum valid saat diketik — kriteria harga diabaikan sementara
        }
        criteria.setInStockOnly(stockOnlyBox.isSelected());
        criteria.setSort((SortOption) sortCombo.getSelectedItem());
        return criteria;
    }

    public void reset() {
        updating = true;
        searchField.setText("");
        minPriceField.setText("");
        maxPriceField.setText("");
        caliberList.clearSelection();
        typeList.clearSelection();
        brandList.clearSelection();
        stockOnlyBox.setSelected(false);
        sortCombo.setSelectedItem(SortOption.NAME_ASC);
        updating = false;
        fireChange();
    }

    private void fireChange() {
        if (!updating) {
            onChange.run();
        }
    }

    private void installSelectionListener(JList<?> list) {
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fireChange();
            }
        });
    }

    private void addLiveListener(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }
        });
    }
}
