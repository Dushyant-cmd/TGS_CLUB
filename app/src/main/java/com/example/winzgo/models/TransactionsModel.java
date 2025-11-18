package com.example.winzgo.models;

public class TransactionsModel {
    String status, type, dateAndTime, date;
    String amount;

    public TransactionsModel(String status, String type, String dateAndTime, String amount) {
        this.status = status;
        this.type = type;
        this.amount = amount;
        this.dateAndTime = dateAndTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TransactionsModel() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getDateAndTime() {
        return dateAndTime;
    }
}
