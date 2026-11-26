package com.mamatgunshop.ui;

import com.mamatgunshop.AppContext;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.util.Money;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Jendela utama: header, panel filter, tabel katalog, dan panel aksi. */
public class MainFrame extends JFrame {

    private final AppContext context;
    private final FilterPanel filterPanel;
    private final ProductTableModel tableModel = new ProductTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel();
    private final JTextArea detailArea = new JTextArea();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JButton addButton = UiUtils.primary("Tambah ke Keranjang");
    private final JButton cartButton = UiUtils.primary("Keranjang (0)");

    public MainFrame(AppContext context) {
        this.context = context;
        setTitle("Mamat Gunshop — Katalog Senjata");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        filterPanel = new FilterPanel(this::refreshCatalog);
        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout(8, 8));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        table.setRowHeight(26);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
            }
        });
        addButton.addActionListener(e -> addToCart());
        cartButton.addActionListener(e -> openCart());

        refreshOptions();
        refreshCatalog();
        updateCartButton();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

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
                "Mamat Gunshop v1.0.0\nAplikasi katalog & kasir senjata (demo lokal).\nJava 25 • Swing • Gradle"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 4, 14));

        JLabel title = new JLabel("MAMAT GUNSHOP");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Cari senjata sesuai kaliber, merek, dan budget Anda");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JSplitPane buildContent() {
        JScrollPane filterScroll = new JScrollPane(filterPanel);
        filterScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Katalog Produk"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterScroll, tableScroll);
        split.setDividerLocation(285);
        split.setResizeWeight(0.0);
        return split;
    }

    private JPanel buildBottom() {
        JPanel root = new JPanel(new BorderLayout(8, 6));
        root.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.add(cartButton);

        JButton historyButton = UiUtils.primary("Riwayat Order");
        historyButton.addActionListener(e -> openHistory());

        JButton adminButton = UiUtils.primary("Admin");
        adminButton.addActionListener(e -> openAdmin());

        JButton exportButton = UiUtils.primary("Export CSV");
        exportButton.addActionListener(e -> exportCsv());

        actions.add(historyButton);
        actions.add(adminButton);
        actions.add(exportButton);
        actions.add(statusLabel);
        root.add(actions, BorderLayout.NORTH);

        JPanel detail = new JPanel(new BorderLayout(4, 4));
        detail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Detail Produk"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setPreferredSize(new Dimension(620, 110));
        detail.add(detailScroll, BorderLayout.CENTER);
        root.add(detail, BorderLayout.CENTER);

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
        addPanel.add(new JLabel("Jumlah:"));
        addPanel.add(qtySpinner);
        addPanel.add(javax.swing.Box.createVerticalStrut(6));
        addPanel.add(addButton);
        root.add(addPanel, BorderLayout.EAST);
        return root;
    }

    private void refreshOptions() {
        filterPanel.setOptions(
                context.catalog().availableCalibers(),
                context.catalog().availableTypes(),
                context.catalog().availableBrands());
    }

    private void refreshCatalog() {
        List<Product> products = context.catalog().search(filterPanel.getCriteria());
        tableModel.setProducts(products);
        statusLabel.setText("Menampilkan " + products.size()
                + " produk • klik kaliber di sebelah kiri untuk memfilter");
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            detailArea.setText("Tidak ada produk yang cocok dengan kriteria ini.");
            addButton.setEnabled(false);
        }
        updateDetail();
    }

    private void updateDetail() {
        Product product = tableModel.productAt(table.getSelectedRow());
        if (product == null) {
            addButton.setEnabled(false);
            return;
        }
        detailArea.setText(
                "Nama       : " + product.getName() + "\n"
                        + "Merek      : " + product.getBrand() + "\n"
                        + "Tipe       : " + (product.getType() == null ? "-" : product.getType().getLabel()) + "\n"
                        + "Kaliber    : " + product.getCaliber() + "\n"
                        + "Kapasitas  : " + product.getCapacity() + " peluru\n"
                        + "Harga      : " + Money.format(product.getPrice()) + "\n"
                        + "Stok       : " + product.getStock() + "\n\n"
                        + "Deskripsi  : " + (product.getDescription() == null ? "-" : product.getDescription()));
        detailArea.setCaretPosition(0);
        addButton.setEnabled(product.isInStock());
    }

    private void addToCart() {
        Product product = tableModel.productAt(table.getSelectedRow());
        if (product == null) {
            UiUtils.showError(this, "Pilih produk terlebih dahulu.");
            return;
        }
        int quantity = (Integer) qtySpinner.getValue();
        try {
            context.cart().add(product.getId(), quantity);
            updateCartButton();
            UiUtils.showInfo(this, quantity + " × " + product.getName() + " ditambahkan ke keranjang.");
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
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
            UiUtils.showInfo(this, "Berhasil mengekspor " + products.size() + " produk ke:\n" + target);
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
