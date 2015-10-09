package com.sudalv.subway.util;

import android.content.Context;

import com.sudalv.subway.listitem.HistoryItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SunWe on 2015/10/8.
 */
public class HistoryUtils {
    private static List<HistoryItem> historyList;
    private static DBManager dbManager;
    public static void readHistoryFromFile(InputStream input) {
        historyList = new ArrayList<>();
        try {
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            String json = new String(buffer, "utf-8");
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject temp = arr.getJSONObject(i);
                String date = temp.getString("date");
                int coin = temp.getInt("coin");
                int miles = temp.getInt("miles");
                int rate = temp.getInt("rate");
                historyList.add(new HistoryItem(date, coin, miles, rate));
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getHistoryFromDataBase(Context context) {
        dbManager = new DBManager(context);
        historyList = new ArrayList<>();
        historyList = dbManager.query();
        dbManager.closeDB();
    }

    public static List<HistoryItem> getHistoryList() {
        return historyList;
    }

    public static String[] getXAxis() {
        List<String> dateList = new ArrayList<>();
        String[] result = new String[historyList.size()];
        for (HistoryItem item : historyList) {
            dateList.add(item.getmDate());
        }
        return dateList.toArray(result);
    }

    public static List<Integer> getCoinArray() {
        List<Integer> CoinList = new ArrayList<>();
        for (HistoryItem item : historyList) {
            CoinList.add(item.getmCoin());
        }
        return CoinList;
    }

    public static List<Integer> getMilesArray() {
        List<Integer> MilesList = new ArrayList<>();
        for (HistoryItem item : historyList) {
            MilesList.add(item.getmMile());
        }
        return MilesList;
    }

    public static List<Integer> getRateArray() {
        List<Integer> RateList = new ArrayList<>();
        for (HistoryItem item : historyList) {
            RateList.add(item.getmRate());
        }
        return RateList;
    }

    public static int getTotalMiles() {
        int result = 0;
        for (HistoryItem item : historyList) {
            result += item.getmMile();
        }
        return result;
    }

    public static int getTotalCoins() {
        int result = 0;
        for (HistoryItem item : historyList) {
            result += item.getmCoin();
        }
        return result;
    }

    public static int getAverageRate() {
        int result = 0;
        for (HistoryItem item : historyList) {
            result += item.getmRate();
        }
        return result / historyList.size();
    }
}
