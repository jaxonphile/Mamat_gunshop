package com.mamatgunshop.service;

import com.mamatgunshop.model.CartItem;
import com.mamatgunshop.model.Customer;
import com.mamatgunshop.model.Order;
import com.mamatgunshop.model.OrderItem;
import com.mamatgunshop.model.OrderStatus;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.repository.OrderRepository;
import com.mamatgunshop.repository.ProductRepository;
import com.mamatgunshop.util.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/** Orkestrasi checkout: validasi → buat order → kurangi stok → simpan. */
public class CheckoutService {

    public static final int MIN_AGE = 21;
    private static final Pattern KTP_PATTERN = Pattern.compile("\\d{16}");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final ProductRepository products;
    private final OrderRepository orders;
    private final CartService cartService;

    public CheckoutService(ProductRepository products, OrderRepository orders, CartService cartService) {
        this.products = products;
        this.orders = orders;
        this.cartService = cartService;
    }

    public Order checkout(Customer customer) {
        validateCustomer(customer);
        cartService.validateStock();

        List<CartItem> cartItems = cartService.getCart().getItemList();
        Map<String, Product> latest = new LinkedHashMap<>();
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (CartItem item : cartItems) {
            String id = item.getProduct().getId();
            Product current = products.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan: " + id));
            latest.put(id, current);
            quantities.put(id, item.getQuantity());
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product current = latest.get(item.getProduct().getId());
            OrderItem orderItem = OrderItem.from(current, item.getQuantity());
            orderItems.add(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        LocalDateTime now = LocalDateTime.now();
        Customer customerCopy = new Customer(customer.getName().trim(),
                customer.getKtpNumber().trim(), customer.getAge());
        Order order = new Order(generateOrderId(now), now, customerCopy, orderItems, total, OrderStatus.PROCESSING);

        quantities.forEach(products::decrementStock);
        products.persist();
        orders.save(order);
        cartService.clear();
        return order;
    }

    public String buildReceipt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("            MAMAT GUNSHOP\n");
        sb.append("      Jl. Contoh No. 12, Indonesia\n");
        sb.append("========================================\n");
        sb.append("No. Order : ").append(order.getId()).append('\n');
        sb.append("Tanggal   : ").append(order.getCreatedAt().format(DATE_FORMAT)).append('\n');
        sb.append("Status    : ").append(order.getStatus().getLabel()).append('\n');
        sb.append("Pelanggan : ").append(order.getCustomer().getName()).append('\n');
        sb.append("KTP       : ").append(maskKtp(order.getCustomer().getKtpNumber())).append('\n');
        sb.append("Usia      : ").append(order.getCustomer().getAge()).append(" tahun\n");
        sb.append("----------------------------------------\n");
        for (OrderItem item : order.getItems()) {
            sb.append(String.format("%-24s x%d%n", item.getProductName(), item.getQuantity()));
            sb.append("  ").append(item.getCaliber()).append("   ")
                    .append(Money.format(item.getUnitPrice()))
                    .append(" = ").append(Money.format(item.getSubtotal())).append('\n');
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-34s", "TOTAL")).append(Money.format(order.getTotal())).append('\n');
        sb.append("========================================\n");
        sb.append("Terima kasih telah berbelanja!\n");
        return sb.toString();
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Data pembeli wajib diisi");
        }
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Nama pembeli wajib diisi");
        }
        if (customer.getKtpNumber() == null || !KTP_PATTERN.matcher(customer.getKtpNumber().trim()).matches()) {
            throw new IllegalArgumentException("Nomor KTP harus 16 digit angka");
        }
        if (customer.getAge() < MIN_AGE) {
            throw new IllegalArgumentException("Usia minimum pembeli adalah " + MIN_AGE + " tahun");
        }
    }

    private String generateOrderId(LocalDateTime now) {
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 4).toUpperCase(java.util.Locale.ROOT);
        return "MG-" + date + "-" + suffix;
    }

    private String maskKtp(String ktp) {
        if (ktp == null || ktp.length() != 16) {
            return "****";
        }
        return ktp.substring(0, 6) + "******" + ktp.substring(12);
    }
}
