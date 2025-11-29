package com.example.winzgo.models;

import java.util.Objects;

public class CoinPredictionHistoryModel {
    private long bet_amount, id, timestamp, user_id, win_amount;
    private String dateAndTime, name, result, selected;

    private boolean isWinner;

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
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

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CoinPredictionHistoryModel that = (CoinPredictionHistoryModel) o;
        return bet_amount == that.bet_amount && id == that.id && result == that.result && selected == that.selected && timestamp == that.timestamp && user_id == that.user_id && win_amount == that.win_amount && isWinner == that.isWinner && Objects.equals(dateAndTime, that.dateAndTime) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bet_amount, id, result, selected, timestamp, user_id, win_amount, dateAndTime, name, isWinner);
    }
}
