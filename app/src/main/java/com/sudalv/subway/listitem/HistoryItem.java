package com.sudalv.subway.listitem;

/**
 * Created by SunWe on 2015/10/8.
 */
public class HistoryItem {
    private String mDate;
    private int mCoin;
    private int mMile;
    private int mRate;

    public HistoryItem(String date, int coin, int mile, int rate) {
        mDate = date;
        mCoin = coin;
        mMile = mile;
        mRate = rate;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public int getmCoin() {
        return mCoin;
    }

    public void setmCoin(int mCoin) {
        this.mCoin = mCoin;
    }

    public int getmMile() {
        return mMile;
    }

    public void setmMile(int mMile) {
        this.mMile = mMile;
    }

    public int getmRate() {
        return mRate;
    }

    public void setmRate(int mRate) {
        this.mRate = mRate;
    }

}
