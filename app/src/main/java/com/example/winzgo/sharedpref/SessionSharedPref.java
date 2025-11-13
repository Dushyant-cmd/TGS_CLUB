package com.example.winzgo.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionSharedPref {
    Context context;
    SharedPreferences sharedPreferences;
    //if want to any operation on shared preferences file then call this constructor with their
    //context(instance of Activity by which android understand what Activity it is and to add style window
    //according to specific them).
    public SessionSharedPref(Context context1) {
        context = context1;
        sharedPreferences = context1.getSharedPreferences("com.example.winzgo", Context.MODE_PRIVATE);
    }
    public void clearFile() {
        //clear SP file.
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
    }
    //below is factory method which work as virtual constructor and returns SharedPreferences object and to make this class as singleton
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public boolean isInstructions() {
        return sharedPreferences.getBoolean("isInstructions", false);
    }

    public void setInstructions(boolean isInstructions) {
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putBoolean("isInstructions", isInstructions);
        setData.apply();
    }

    public long getBalance() {
        return sharedPreferences.getLong("balance", 0);//get long type key(balance) value by pass def value if not exist create new key/value
        //-pair in SP with default value passed in getter argument
    }

    public void setBalance(long balance) {
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putLong("balance", balance);
        setData.apply();//to apply changes in SP in background
    }

    public long getId() {
        return sharedPreferences.getLong("userId", 0);
    }

    public void setId(long id) {
        //Editor interface instance in order to update or write data in SP file
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putLong("userId", id);//save long in sp
        setData.apply();
    }

    public String getMobile() {
        return sharedPreferences.getString("mobile", "no");
    }

    public void setMobile(String mobile) {
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putString("mobile", mobile);//save string in sp
        setData.apply();
    }

    public String getName() {
        return sharedPreferences.getString("name", "no");
    }

    public void setName(String name) {
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putString("name", name);
        setData.apply();
    }

    public String getRefer() {
        return sharedPreferences.getString("refer", "no");
    }

    public void setRefer(String refer) {
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putString("refer", refer);
        setData.apply();//apply changes(add or remove) to add or store data(key/value) pair in SP
    }

    public boolean getLoginStatus() {
        return sharedPreferences.getBoolean("loginStatus", false);
    }

    public void setLoginStatus(boolean status) {
        SharedPreferences.Editor setData = sharedPreferences.edit();
        setData.putBoolean("loginStatus", status);
        setData.apply();
    }

    public void setGameId(String gameId) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("gameId", gameId);
        edit.apply();//apply changes in background
    }

    public String getGameId() {
        return sharedPreferences.getString("gameId", "no");
    }

    public static void setStr(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.apply();//apply changes in background
    }

    public static String getStr(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value);
        edit.apply();//apply changes in background
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void clearFile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }
}
