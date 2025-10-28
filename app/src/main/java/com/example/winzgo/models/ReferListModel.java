package com.example.winzgo.models;

public class ReferListModel {
    //Variable to store String type value in ReferListModel class object
    private String mName, mIsSignUp, mIsRecharge;

    public ReferListModel(String name, String isSignUp, String isRecharge) {
        this.mName = name;
        this.mIsSignUp = isSignUp;
        this.mIsRecharge = isRecharge;
    }

    public String getName() {
        return mName;
    }

    public String isSignUp() {
        return mIsSignUp;
    }

    public String isRecharge() {
        return mIsRecharge;
    }
}
