package com.mamatgunshop.model;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/** Kriteria pencarian dan filter katalog. */
public class FilterCriteria {

    private String keyword = "";
    private final Set<String> calibers = new LinkedHashSet<>();
    private final Set<GunType> types = new LinkedHashSet<>();
    private final Set<String> brands = new LinkedHashSet<>();
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStockOnly;
    private SortOption sort = SortOption.NAME_ASC;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword;
    }

    public Set<String> getCalibers() {
        return calibers;
    }

    public void setCalibers(Set<String> calibers) {
        this.calibers.clear();
        if (calibers != null) {
            this.calibers.addAll(calibers);
        }
    }

    public Set<GunType> getTypes() {
        return types;
    }

    public void setTypes(Set<GunType> types) {
        this.types.clear();
        if (types != null) {
            this.types.addAll(types);
        }
    }

    public Set<String> getBrands() {
        return brands;
    }

    public void setBrands(Set<String> brands) {
        this.brands.clear();
        if (brands != null) {
            this.brands.addAll(brands);
        }
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public boolean isInStockOnly() {
        return inStockOnly;
    }

    public void setInStockOnly(boolean inStockOnly) {
        this.inStockOnly = inStockOnly;
    }

    public SortOption getSort() {
        return sort;
    }

    public void setSort(SortOption sort) {
        this.sort = sort == null ? SortOption.NAME_ASC : sort;
    }

    /** Mengembalikan true jika tidak ada kriteria aktif selain sort/default. */
    public boolean isEmpty() {
        return keyword.isBlank() && calibers.isEmpty() && types.isEmpty() && brands.isEmpty()
                && minPrice == null && maxPrice == null && !inStockOnly;
    }

    public void clear() {
        keyword = "";
        calibers.clear();
        types.clear();
        brands.clear();
        minPrice = null;
        maxPrice = null;
        inStockOnly = false;
        sort = SortOption.NAME_ASC;
    }
}
