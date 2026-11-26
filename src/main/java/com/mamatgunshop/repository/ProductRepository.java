package com.mamatgunshop.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mamatgunshop.model.Product;
import com.mamatgunshop.util.DataPaths;
import com.mamatgunshop.util.JsonSupport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/** Menyimpan dan memuat produk dari file JSON. */
public class ProductRepository {

    private static final Logger LOG = Logger.getLogger(ProductRepository.class.getName());
    private static final TypeReference<List<Product>> LIST_TYPE = new TypeReference<>() {
    };

    private final Path file;
    private final List<Product> products = new ArrayList<>();

    /** Repository produksi: memakai ~/.mamatgunshop/products.json + seed jika belum ada. */
    public ProductRepository() {
        this(DataPaths.productsFile(), true);
    }

    /** Repository untuk pengujian: file arbitrary tanpa seed. */
    public ProductRepository(Path file) {
        this(file, false);
    }

    private ProductRepository(Path file, boolean seedFromResources) {
        this.file = file;
        load(seedFromResources);
    }

    private void load(boolean seedFromResources) {
        try {
            DataPaths.ensureParent(file);
            if (!Files.exists(file)) {
                if (seedFromResources) {
                    try (InputStream seed = DataPaths.seedProducts()) {
                        if (seed != null) {
                            Files.copy(seed, file);
                        }
                    }
                }
                if (!Files.exists(file)) {
                    JsonSupport.mapper().writeValue(file.toFile(), List.of());
                }
            }
            List<Product> loaded = JsonSupport.mapper().readValue(file.toFile(), LIST_TYPE);
            products.clear();
            if (loaded != null) {
                products.addAll(loaded);
            }
        } catch (IOException e) {
            LOG.warning("Gagal memuat " + file + " — memakai daftar kosong. Detail: " + e.getMessage());
            products.clear();
        }
    }

    public synchronized List<Product> findAll() {
        return products.stream().map(Product::copy).toList();
    }

    public synchronized Optional<Product> findById(String id) {
        return products.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .map(Product::copy);
    }

    /** Menambah atau mengganti produk berdasarkan id, lalu menyimpan. */
    public synchronized Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        if (product.getId() == null || product.getId().isBlank()) {
            product.setId(UUID.randomUUID().toString());
        }
        products.removeIf(p -> p.getId().equalsIgnoreCase(product.getId()));
        products.add(product.copy());
        persist();
        return product.copy();
    }

    public synchronized boolean deleteById(String id) {
        boolean removed = products.removeIf(p -> p.getId().equalsIgnoreCase(id));
        if (removed) {
            persist();
        }
        return removed;
    }

    /** Mengurangi stok tanpa menyimpan; pemanggil wajib memanggil persist() setelahnya. */
    public synchronized void decrementStock(String id, int quantity) {
        Product product = products.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan: " + id));
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Stok " + product.getName() + " tidak mencukupi");
        }
        product.setStock(product.getStock() - quantity);
    }

    public synchronized void persist() {
        try {
            JsonSupport.mapper().writeValue(file.toFile(), products);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan produk ke " + file, e);
        }
    }
}
