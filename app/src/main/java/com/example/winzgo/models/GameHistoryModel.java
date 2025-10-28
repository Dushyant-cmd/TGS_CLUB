package com.example.winzgo.models;

public class GameHistoryModel {
    long id, bet, win, result, timestamp;
    String selected;
    String dateAndTime;

    public GameHistoryModel(long id, long bet, long win, long result, String selected, String dateAndTime, long timestamp) {
        this.id = id;
        this.bet = bet;
        this.win = win;
        this.result = result;
        this.selected = selected;
        this.timestamp = timestamp;
        this.dateAndTime = dateAndTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public long getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public long getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
