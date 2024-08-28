package com.example.wifaq;

import java.time.LocalDate;

public class Card {

    private String cardNumber;
    private String holder;
    private LocalDate expirationDate;
    private float balance;
    private Category category;
    private Functionnality functionnality;

    // Enum for category
    public enum Category {
        WIFAQ_GOLD_NATIONALE,
        GOLD_NATIONALE,
        WIFAK_PLATIUM_INTERNATIONALE,
        WIFAK_PLATIUM_NATIONALE,
        WIFAQ_TECHNOLOGIQUE,
        WIFAQ_PLATIUM_INTERNATIONALE_AVA,
        WIFAQ_RASSIDI_PLUS_PRO,
        WIFAQ_RASSIDI_PLUS
    }

    // Enum for functionnality
    public enum Functionnality {
        MASTER, SLAVE
    }

    public Card(String cardNumber, String holder, LocalDate expirationDate, float balance, Category category, Functionnality functionnality) {
        this.cardNumber = cardNumber;
        this.holder = holder;
        this.expirationDate = expirationDate;
        this.balance = balance;
        this.category = category;
        this.functionnality = functionnality;
    }

    // Getters
    public String getCardNumber() {
        return cardNumber;
    }

    public String getHolder() {return holder;}

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public float getBalance() {
        return balance;
    }

    public Category getCategory() {
        return category;
    }

    public Functionnality getFunctionnality(){ return functionnality; }

    // Setters
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setHolder(String holder) {this.holder = holder;}

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setFunctionnality(Functionnality functionnality){ this.functionnality = functionnality;}
}
