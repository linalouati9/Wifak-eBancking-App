package com.example.wifaq;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Account {
    private String username;
    private String password;
    private long userId;
    private String nCompte;

    public Account(String username, String password, long userId, String nCompte) {
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.nCompte = nCompte;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {this.password = password;}

    public long getUserId() { return userId; }

    public void setUserId(long userId) {this.userId = userId;}

    public String getNCompte() { return nCompte; }

    public void setNCompte(String nCompte) {this.nCompte = nCompte;}

    @NonNull
    @Override
    public String toString() {
        return username + password + userId + nCompte;
    }
}
