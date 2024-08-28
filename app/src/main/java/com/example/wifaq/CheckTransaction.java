package com.example.wifaq;

import java.time.LocalDate;

public class CheckTransaction {

    private int checkId;
    private float amount;
    private String amountLetters;
    private String beneficiaryName;
    private String sourceRIB;
    private String sourceHolder;
    private String provence;
    private LocalDate date;
    private String beneficiaryAccountNumber;
    private String memo;

    public CheckTransaction(int checkId, float amount, String amountLetters, String beneficiaryName, String sourceRIB, String sourceHolder, String provence, LocalDate date, String beneficiaryAccountNumber, String memo) {
        this.checkId = checkId;
        this.amount = amount;
        this.amountLetters = amountLetters;
        this.beneficiaryName = beneficiaryName;
        this.sourceRIB = sourceRIB;
        this.sourceHolder = sourceHolder;
        this.provence = provence;
        this.date = date;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.memo = memo;
    }

    public int getCheckId() {
        return checkId;
    }

    public float getAmount() {
        return amount;
    }

    public String getAmountLetters() {
        return amountLetters;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public String getSourceRIB() {
        return sourceRIB;
    }

    public String getProvence() {
        return provence;
    }

    public String getSourceHolder() {
        return sourceHolder;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public String getMemo() {
        return memo;
    }

    public void setCheckId(int checkId) {
        this.checkId = checkId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setAmountLetters(String amountLetters) {
        this.amountLetters = amountLetters;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public void setSourceRIB(String sourceRIB) {
        this.sourceRIB = sourceRIB;
    }

    public void setSourceHolder(String sourceHolder) {
        this.sourceHolder = sourceHolder;
    }

    public void setProvence(String provence) {
        this.provence = provence;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
