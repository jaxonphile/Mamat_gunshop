package com.mamatgunshop.service;

/** Autentikasi admin sederhana untuk aplikasi demo (kredensial lokal). */
public class AuthService {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    public boolean login(String username, String password) {
        return ADMIN_USERNAME.equalsIgnoreCase(username == null ? "" : username.trim())
                && ADMIN_PASSWORD.equals(password == null ? "" : password);
    }
}
