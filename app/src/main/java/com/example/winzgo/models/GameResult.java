package com.example.winzgo.models;

public class GameResult {
    private long tradeId;
    private long numberResult;
    private String imgResult;
    private boolean mFlag;
    private String createdAt;
    private String halfWin;

    public void setImgResult(String imgResult) {
        this.imgResult = imgResult;
    }

    public String getImgResult() {
        return imgResult;
    }

    public GameResult(long tradeId, long numberResult, String imgResults, String createdAt, String halfWin) {
        this.tradeId = tradeId;
        this.numberResult = numberResult;
        this.imgResult = imgResults;
        this.createdAt = createdAt;
        this.mFlag = false;
        this.halfWin = halfWin;
    }

    public GameResult(int tradeId, int numberResult, String imgResults, boolean flag) {
        this.tradeId = tradeId;
        this.numberResult = numberResult;
        this.imgResult = imgResults;
        this.mFlag = flag;
    }

    public String getHalfWin() {
        return halfWin;
    }

    public void setHalfWin(String halfWin) {
        this.halfWin = halfWin;
    }

    public boolean ismFlag() {
        return mFlag;
    }

    public void setmFlag(boolean mFlag) {
        this.mFlag = mFlag;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public void setNumberResult(int numberResult) {
        this.numberResult = numberResult;
    }

    public long getTradeId() {
        return tradeId;
    }

    public long getNumberResult() {
        return numberResult;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
