package com.mamatgunshop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/** Format dan parsing mata uang Rupiah. */
public final class Money {

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    static {
        FORMAT.setMinimumFractionDigits(0);
        FORMAT.setMaximumFractionDigits(0);
    }

    public static String format(BigDecimal value) {
        if (value == null) {
            return "Rp 0";
        }
        return "Rp " + FORMAT.format(value);
    }

    /**
     * Menerima input seperti "6850000", "6.850.000", atau "Rp 6.850.000".
     */
    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String cleaned = text.replace("Rp", "")
                .replace(".", "")
                .replace(",", ".")
                .replaceAll("[^0-9.]", "")
                .trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            Number parsed = FORMAT.parse(cleaned);
            return BigDecimal.valueOf(parsed.doubleValue());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Format angka tidak valid: " + text);
        }
    }

    private Money() {
    }
}
