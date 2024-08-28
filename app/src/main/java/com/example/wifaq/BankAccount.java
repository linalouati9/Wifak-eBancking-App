package com.example.wifaq;

public class BankAccount {

    private String nCompte;
    private String RIB;
    private float balance;

    public BankAccount(String nCompte, String RIB, float balance){
        this.nCompte = nCompte;
        this.RIB = RIB;
        this.balance = balance;
    }

    public String getNCompte(){ return this.nCompte;}
    public String getRIB() { return this.RIB;}
    public float getBalance(){ return this.balance;}

    public void setNCompte(String nCompte){ this.nCompte = nCompte;}
    public void setRIB(String RIB){ this.RIB = RIB;}
    public void setBalance(float balance){ this.balance = balance;}
}
