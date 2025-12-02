package com.example.winzgo.models;

public class BankAccountModel {
    private String ifscCode, bankHolderName, name, upiId, accountNumber;
    public BankAccountModel() {}

    public BankAccountModel(String ifscCode, String bankHolderName, String name, String upiId, String accountNumber) {
        this.ifscCode = ifscCode;
        this.bankHolderName = bankHolderName;
        this.name = name;
        this.upiId = upiId;
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBankHolderName() {
        return bankHolderName;
    }

    public void setBankHolderName(String bankHolderName) {
        this.bankHolderName = bankHolderName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
