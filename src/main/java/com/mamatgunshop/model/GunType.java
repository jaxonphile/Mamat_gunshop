package com.mamatgunshop.model;

/** Tipe/jenis senjata yang dijual toko. */
public enum GunType {
    PISTOL("Pistol"),
    REVOLVER("Revolver"),
    RIFLE("Rifle"),
    SHOTGUN("Shotgun"),
    SMG("SMG");

    private final String label;

    GunType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Mencari tipe berdasarkan label tampilan (case-insensitive). */
    public static GunType fromLabel(String label) {
        for (GunType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipe senjata tidak dikenal: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
