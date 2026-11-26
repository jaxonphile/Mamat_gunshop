package com.mamatgunshop.service;

import com.mamatgunshop.model.FilterCriteria;
import com.mamatgunshop.model.GunType;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.model.SortOption;
import com.mamatgunshop.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Logika pencarian, filter, dan sortir katalog. */
public class CatalogService {

    private final ProductRepository repository;

    public CatalogService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> search(FilterCriteria criteria) {
        FilterCriteria c = criteria == null ? new FilterCriteria() : criteria;
        String keyword = c.getKeyword().trim().toLowerCase(Locale.ROOT);

        List<Product> result = repository.findAll().stream()
                .filter(p -> matches(p, c, keyword))
                .sorted(comparator(c.getSort()))
                .toList();
        return result;
    }

    public List<String> availableCalibers() {
        return repository.findAll().stream()
                .map(p -> p.getCaliber() == null ? "" : p.getCaliber().trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> availableBrands() {
        return repository.findAll().stream()
                .map(p -> p.getBrand() == null ? "" : p.getBrand().trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<GunType> availableTypes() {
        return repository.findAll().stream()
                .map(Product::getType)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(GunType::name))
                .toList();
    }

    private boolean matches(Product p, FilterCriteria c, String keyword) {
        if (!keyword.isEmpty()) {
            String haystack = String.join(" ",
                    safe(p.getName()), safe(p.getBrand()), safe(p.getCaliber()), safe(p.getDescription()))
                    .toLowerCase(Locale.ROOT);
            if (!haystack.contains(keyword)) {
                return false;
            }
        }
        if (!c.getCalibers().isEmpty()
                && !c.getCalibers().contains(safe(p.getCaliber()).trim())) {
            return false;
        }
        if (!c.getTypes().isEmpty() && !c.getTypes().contains(p.getType())) {
            return false;
        }
        if (!c.getBrands().isEmpty() && !c.getBrands().contains(safe(p.getBrand()).trim())) {
            return false;
        }
        BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
        if (c.getMinPrice() != null && price.compareTo(c.getMinPrice()) < 0) {
            return false;
        }
        if (c.getMaxPrice() != null && price.compareTo(c.getMaxPrice()) > 0) {
            return false;
        }
        return !c.isInStockOnly() || p.isInStock();
    }

    private Comparator<Product> comparator(SortOption sort) {
        if (sort == null) {
            sort = SortOption.NAME_ASC;
        }
        return switch (sort) {
            case NAME_ASC -> Comparator.comparing(p -> safe(p.getName()), String.CASE_INSENSITIVE_ORDER);
            case NAME_DESC -> Comparator.comparing((Product p) -> safe(p.getName()),
                    String.CASE_INSENSITIVE_ORDER).reversed();
            case PRICE_ASC -> Comparator.comparing(p -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice());
            case PRICE_DESC -> Comparator.comparing((Product p) -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice()).reversed();
            case CALIBER_ASC -> Comparator.comparing(p -> safe(p.getCaliber()), String.CASE_INSENSITIVE_ORDER);
            case STOCK_DESC -> Comparator.comparingInt(Product::getStock).reversed();
        };
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
