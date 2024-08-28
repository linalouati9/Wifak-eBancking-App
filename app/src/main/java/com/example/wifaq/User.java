package com.example.wifaq;

import java.time.LocalDate;
import java.util.Date;

public class User {

    private long cin;
    private String name;
    private String firstName;
    private LocalDate birth;
    private String phone;
    private String email;
    private String address;

    // Constructor
    public User(long cin, String name, String firstName, LocalDate birth, String phone, String email, String address) {
        this.cin = cin;
        this.name = name;
        this.firstName = firstName;
        this.birth = birth;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // Getters and Setters
    public long getCin() {
        return cin;
    }

    public void setCin(long cin) {
        this.cin = cin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
