package com.example.wifaq;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Transaction {

    private String sourceCardNumber;
    private String destCardNumber;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private float amount;
    private Type type;

    public enum Type {
        PAYMENT,
        TRANSFER,
        DEBIT,
        CASH_IN,
        CASH_OUT
    }

    // Constructor
    public Transaction(String sourceCardNumber, String destCardNumber, LocalDate transactionDate, LocalTime transactionTime, float amount, Type type) {
        this.sourceCardNumber = sourceCardNumber;
        this.destCardNumber = destCardNumber;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
        this.amount = amount;
        this.type = type;
    }

    // Getter

    public String getSourceCardNumber() {
        return sourceCardNumber;
    }

    public String getDestCardNumber() {
        return destCardNumber;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public LocalTime getTransactionTime() {
        return transactionTime;
    }

    public float getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }

    // Setters
    public void setSourceCardNumber(String sourceCardNumber) {
        this.sourceCardNumber = sourceCardNumber;
    }

    public void setDestCardNumber(String destCardNumber) {
        this.destCardNumber = destCardNumber;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionTime(LocalTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
