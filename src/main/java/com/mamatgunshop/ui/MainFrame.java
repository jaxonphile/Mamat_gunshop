package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.FilterCriteria;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.model.SortOption;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Jendela utama: app bar, toolbar, katalog, detail, dan bar aksi. */
public class MainFrame extends JFrame {

    private final AppContext context;
    private final FilterPanel filterPanel;
    private final ProductTableModel tableModel = new ProductTableModel();
    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = UiTheme.field();
    private final JComboBox<SortOption> sortCombo = new JComboBox<>(SortOption.values());
    private final JLabel resultLabel = UiTheme.muted("");
    private final JLabel statusLabel = UiTheme.muted("Siap.");

    private final JLabel detailName = UiTheme.heading("Pilih produk");
    private final JLabel detailMeta = UiTheme.muted("Klik baris pada tabel untuk melihat detail.");
    private final JLabel detailPrice = UiTheme.subheading("Rp 0");
    private final JLabel detailStock = UiTheme.muted("");
    private final JTextArea detailDescription = UiTheme.area(3, 30);
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JButton addButton = UiTheme.primary("Tambah ke keranjang");
    private final JButton cartButton = UiTheme.secondary("Keranjang (0)");

    private Product selectedProduct;

    public MainFrame(AppContext context) {
        this.context = context;
        setTitle("Mamat Gunshop — Katalog Senjata");
        setSize(1220, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UiTheme.CANVAS);

        filterPanel = new FilterPanel(this::refreshCatalog);
        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout(0, 0));
        add(buildAppBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        UiTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshCatalog();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshCatalog();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshCatalog();
            }
        });
        sortCombo.addActionListener(e -> refreshCatalog());
        addButton.addActionListener(e -> addToCart());
        cartButton.addActionListener(e -> openCart());

        detailDescription.setEditable(false);
        detailDescription.setBackground(UiTheme.SURFACE);

        refreshOptions();
        refreshCatalog();
        updateCartButton();
        setStatus("Katalog siap. Gunakan filter kaliber untuk mempersempit pilihan.");
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(UiTheme.SURFACE);

        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export katalog ke CSV...");
        exportItem.addActionListener(e -> exportCsv());
        JMenuItem exitItem = new JMenuItem("Keluar");
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Bantuan");
        JMenuItem aboutItem = new JMenuItem("Tentang");
        aboutItem.addActionListener(e -> UiUtils.showInfo(this,
                "Mamat Gunshop v1.0.0\nKatalog & kasir senjata (demo lokal).\nJava 25 • Swing • Gradle"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel buildAppBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setBackground(UiTheme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setBackground(UiTheme.SURFACE);

        JPanel mark = new JPanel();
        mark.setBackground(UiTheme.ACCENT);
        mark.setPreferredSize(new Dimension(38, 38));
        JLabel initials = new JLabel("MG", javax.swing.SwingConstants.CENTER);
        initials.setFont(UiTheme.font(14, Font.BOLD));
        initials.setForeground(Color.WHITE);
        mark.add(initials);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(UiTheme.SURFACE);
        JLabel title = UiTheme.heading("Mamat Gunshop");
        JLabel subtitle = UiTheme.muted("Katalog & kasir senjata api — data demo lokal");
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);

        brand.add(mark, BorderLayout.WEST);
        brand.add(titles, BorderLayout.CENTER);
        bar.add(brand, BorderLayout.WEST);
        bar.add(UiTheme.muted("v1.0"), BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildContent() {
        JScrollPane filterScroll = UiTheme.scroll(filterPanel);
        filterScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(UiTheme.CANVAS);
        main.add(buildToolbar(), BorderLayout.NORTH);
        main.add(UiTheme.scroll(table), BorderLayout.CENTER);
        main.add(buildDetail(), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterScroll, main);
        split.setDividerLocation(292);
        split.setResizeWeight(0.0);
        split.setBorder(null);
        return split;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(UiTheme.CANVAS);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UiTheme.CANVAS);
        right.add(resultLabel);
        right.add(UiTheme.muted("Urutkan"));
        right.add(sortCombo);

        toolbar.add(UiTheme.label("Cari"), BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildDetail() {
        JPanel card = UiTheme.card();
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                UiTheme.cardBorder()));

        JPanel header = new JPanel(new BorderLayout(2, 2));
        header.setBackground(UiTheme.SURFACE);
        header.add(detailName, BorderLayout.NORTH);
        header.add(detailMeta, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(UiTheme.SURFACE);
        body.add(UiTheme.scroll(detailDescription), BorderLayout.CENTER);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UiTheme.SURFACE);
        detailPrice.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        detailStock.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        addButton.setAlignmentX(JLabel.RIGHT_ALIGNMENT);

        JPanel qty = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        qty.setBackground(UiTheme.SURFACE);
        qty.add(UiTheme.muted("Jumlah"));
        qty.add(qtySpinner);

        side.add(detailPrice);
        side.add(Box.createVerticalStrut(4));
        side.add(detailStock);
        side.add(Box.createVerticalStrut(10));
        side.add(qty);
        side.add(Box.createVerticalStrut(8));
        side.add(addButton);

        body.add(side, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(UiTheme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.LINE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(UiTheme.SURFACE);
        buttons.add(cartButton);

        JButton historyButton = UiTheme.secondary("Riwayat order");
        historyButton.addActionListener(e -> openHistory());
        JButton adminButton = UiTheme.secondary("Admin");
        adminButton.addActionListener(e -> openAdmin());
        JButton exportButton = UiTheme.ghost("Export CSV");
        exportButton.addActionListener(e -> exportCsv());
        buttons.add(historyButton);
        buttons.add(adminButton);
        buttons.add(exportButton);

        bar.add(buttons, BorderLayout.WEST);
        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    private void refreshOptions() {
        filterPanel.setOptions(
                context.catalog().availableCalibers(),
                context.catalog().availableTypes(),
                context.catalog().availableBrands());
    }

    private void refreshCatalog() {
        FilterCriteria criteria = filterPanel.getCriteria();
        criteria.setKeyword(searchField.getText());
        criteria.setSort((SortOption) sortCombo.getSelectedItem());

        List<Product> products = context.catalog().search(criteria);
        tableModel.setProducts(products);
        resultLabel.setText(products.size() + " dari " + context.inventory().findAll().size() + " produk");

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            selectedProduct = null;
            clearDetail("Tidak ada produk yang cocok dengan kriteria ini.");
        }
        updateDetail();
    }

    private void clearDetail(String message) {
        detailName.setText("Pilih produk");
        detailMeta.setText(message);
        detailPrice.setText("Rp 0");
        detailStock.setText("");
        detailDescription.setText("");
        addButton.setEnabled(false);
        qtySpinner.setEnabled(false);
    }

    private void updateDetail() {
        selectedProduct = tableModel.productAt(table.getSelectedRow());
        if (selectedProduct == null) {
            return;
        }
        qtySpinner.setEnabled(true);
        SpinnerNumberModel model = (SpinnerNumberModel) qtySpinner.getModel();
        model.setMaximum(Math.max(1, selectedProduct.getStock()));
        if ((Integer) qtySpinner.getValue() > Math.max(1, selectedProduct.getStock())) {
            qtySpinner.setValue(Math.max(1, selectedProduct.getStock()));
        }

        detailName.setText(selectedProduct.getName());
        detailMeta.setText(selectedProduct.getBrand() + "  •  "
                + (selectedProduct.getType() == null ? "-" : selectedProduct.getType().getLabel())
                + "  •  " + selectedProduct.getCaliber()
                + "  •  kapasitas " + selectedProduct.getCapacity() + " peluru");
        detailPrice.setText(Money.format(selectedProduct.getPrice()));
        detailStock.setText(selectedProduct.isInStock()
                ? "Stok tersedia: " + selectedProduct.getStock()
                : "Stok habis");
        detailStock.setForeground(selectedProduct.isInStock() ? UiTheme.SUCCESS : UiTheme.DANGER);
        detailDescription.setText(selectedProduct.getDescription() == null
                ? "" : selectedProduct.getDescription());
        detailDescription.setCaretPosition(0);
        addButton.setEnabled(selectedProduct.isInStock());
    }

    private void addToCart() {
        if (selectedProduct == null) {
            setStatus("Pilih produk terlebih dahulu.", UiTheme.DANGER);
            return;
        }
        int quantity = (Integer) qtySpinner.getValue();
        try {
            context.cart().add(selectedProduct.getId(), quantity);
            updateCartButton();
            setStatus(quantity + " × " + selectedProduct.getName() + " ditambahkan ke keranjang.",
                    UiTheme.SUCCESS);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), UiTheme.DANGER);
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color == null ? UiTheme.MUTED : color);
    }

    private void setStatus(String message) {
        setStatus(message, UiTheme.MUTED);
    }

    private void updateCartButton() {
        cartButton.setText("Keranjang (" + context.cart().getCart().getItemCount() + ")");
    }

    private void openCart() {
        new CartDialog(this, context, this::updateCartButton).setVisible(true);
        updateCartButton();
    }

    private void openHistory() {
        new OrderHistoryDialog(this, context, this::refreshCatalog).setVisible(true);
    }

    private void openAdmin() {
        if (!AuthDialog.show(this, context.auth())) {
            return;
        }
        new AdminDialog(this, context, () -> {
            refreshOptions();
            refreshCatalog();
        }).setVisible(true);
    }

    private void exportCsv() {
        List<Product> products = context.inventory().findAll();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("mamat-gunshop-katalog.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        StringBuilder csv = new StringBuilder("id,nama,merek,tipe,kaliber,kapasitas,harga,stok,deskripsi\n");
        for (Product p : products) {
            csv.append(escape(p.getId())).append(',')
                    .append(escape(p.getName())).append(',')
                    .append(escape(p.getBrand())).append(',')
                    .append(escape(p.getType() == null ? "" : p.getType().name())).append(',')
                    .append(escape(p.getCaliber())).append(',')
                    .append(p.getCapacity()).append(',')
                    .append(p.getPrice() == null ? 0 : p.getPrice()).append(',')
                    .append(p.getStock()).append(',')
                    .append(escape(p.getDescription()))
                    .append('\n');
        }
        try {
            Files.writeString(target, csv.toString(), StandardCharsets.UTF_8);
            setStatus("Diekspor " + products.size() + " produk ke " + target.getFileName(), UiTheme.SUCCESS);
        } catch (Exception ex) {
            UiUtils.showError(this, "Gagal menulis CSV: " + ex.getMessage());
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
