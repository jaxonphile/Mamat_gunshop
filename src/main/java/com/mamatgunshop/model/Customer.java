package com.mamatgunshop.model;

/** Data identitas pembeli yang diinput saat checkout. */
public class Customer {

    private String name;
    private String ktpNumber;
    private int age;

    public Customer() {
    }

    public Customer(String name, String ktpNumber, int age) {
        this.name = name;
        this.ktpNumber = ktpNumber;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKtpNumber() {
        return ktpNumber;
    }

    public void setKtpNumber(String ktpNumber) {
        this.ktpNumber = ktpNumber;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
