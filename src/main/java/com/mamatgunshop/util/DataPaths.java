package com.mamatgunshop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Lokasi file data pengguna dan akses ke data seed di dalam JAR. */
public final class DataPaths {

    public static final Path APP_DIR = Paths.get(System.getProperty("user.home"), ".mamatgunshop");

    public static Path productsFile() {
        return APP_DIR.resolve("products.json");
    }

    public static Path ordersFile() {
        return APP_DIR.resolve("orders.json");
    }

    public static InputStream seedProducts() {
        return DataPaths.class.getResourceAsStream("/data/products.json");
    }

    public static void ensureParent(Path file) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membuat direktori data: " + file, e);
        }
    }

    private DataPaths() {
    }
}
