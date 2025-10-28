package com.example.winzgo.models;

public class UpiAdapterModel {
    private String upiId;
    private boolean isCopied = false;

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public boolean isCopied() {
        return isCopied;
    }

    public void setCopied(boolean copied) {
        isCopied = copied;
    }
}
