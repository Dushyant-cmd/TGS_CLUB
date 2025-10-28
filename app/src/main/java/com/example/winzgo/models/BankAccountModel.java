package com.example.winzgo.models;

public class BankAccountModel {
    long accountNumber;
    String ifscCode, micrCode, name;

    public BankAccountModel() {

    }
    public BankAccountModel(long accountNumber, String ifscCode, String micrCode, String name) {
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.micrCode = micrCode;
        this.name = name;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getMicrCode() {
        return micrCode;
    }

    public String getName() {
        return name;
    }
}
