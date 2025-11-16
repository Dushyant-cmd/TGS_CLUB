package com.example.winzgo.models;

import com.example.winzgo.models.BankAccountModel;

public class UserDocumentModel {
    long balance, id, coinBalance, tradeProBalance;
    BankAccountModel bankDetails;
    String mobile, name, refer, fcmToken;

    public long getBalance() {
        return balance;
    }

    public long getId() {
        return id;
    }

    public BankAccountModel getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankAccountModel bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getMobile() {
        return mobile;
    }

    public String getName() {
        return name;
    }

    public String getRefer() {
        return refer;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRefer(String refer) {
        this.refer = refer;
    }

    public UserDocumentModel() {
    }

    public UserDocumentModel(long balance, long id, BankAccountModel bankDetails, String mobile, String name, String refer) {
        this.balance = balance;
        this.id = id;
        if (bankDetails != null) {
            this.bankDetails = bankDetails;
        } else {
            this.bankDetails = null;
        }
        this.mobile = mobile;
        this.name = name;
        this.refer = refer;
    }
}
