package com.example.winzgo.models;

import java.util.Objects;

public class TradeHistoryModel {
    private long bet_amount, id, result, selected, timestamp, user_id, win_amount;
    private String dateAndTime, name;
    private boolean isUp, isWinner;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TradeHistoryModel)) return false;
        TradeHistoryModel that = (TradeHistoryModel) o;
        return getBet_amount() == that.getBet_amount() && getId() == that.getId() && getResult() == that.getResult() && getSelected() == that.getSelected() && getTimestamp() == that.getTimestamp() && getUser_id() == that.getUser_id() && getWin_amount() == that.getWin_amount() && isUp() == that.isUp() && isWinner() == that.isWinner() && Objects.equals(getDateAndTime(), that.getDateAndTime()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBet_amount(), getId(), getResult(), getSelected(), getTimestamp(), getUser_id(), getWin_amount(), getDateAndTime(), getName(), isUp(), isWinner());
    }

    public long getBet_amount() {
        return bet_amount;
    }

    public void setBet_amount(long bet_amount) {
        this.bet_amount = bet_amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    public long getSelected() {
        return selected;
    }

    public void setSelected(long selected) {
        this.selected = selected;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public long getWin_amount() {
        return win_amount;
    }

    public void setWin_amount(long win_amount) {
        this.win_amount = win_amount;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }
}
