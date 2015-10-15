package com.sudalv.subway.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sudalv.subway.listitem.HistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SunWe on 2015/10/8.
 */
public class DBManager {
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DatabaseHelper(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add historyItem
     *
     * @param historys
     */
    public void add(List<HistoryItem> historys) {
        // 采用事务处理，确保数据完整性
        db.beginTransaction(); // 开始事务
        try {
            for (HistoryItem item : historys) {
                ContentValues values = new ContentValues();
                values.put("date", item.getmDate());
                values.put("mile", item.getmMile());
                values.put("coin", item.getmCoin());
                values.put("rate", item.getmRate());
                long id = db.insert(DatabaseHelper.TABLE_NAME, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * update history's mile, coin, rate
     *
     * @param item
     */
    public void updateHistoryItem(HistoryItem item) {
        db.beginTransaction(); // 开始事务
        try {
            ContentValues cv = new ContentValues();
            cv.put("mile", item.getmMile());
            cv.put("coin", item.getmCoin());
            cv.put("rate", item.getmRate());
            db.update(DatabaseHelper.TABLE_NAME, cv, "date = ?",
                    new String[]{item.getmDate()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * update history's mile, coin, rate
     *
     * @param item
     */
    public void insertHistoryItem(HistoryItem item) {
        db.beginTransaction(); // 开始事务
        try {
            ContentValues values = new ContentValues();
            values.put("date", item.getmDate());
            values.put("mile", item.getmMile());
            values.put("coin", item.getmCoin());
            values.put("rate", item.getmRate());
            long id = db.insert(DatabaseHelper.TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * query all history items, return list
     *
     * @return List<HistoryItem>
     */
    public List<HistoryItem> query() {
        List<HistoryItem> items = new ArrayList<>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            String date = c.getString(c.getColumnIndex("date"));
            int mile = c.getInt(c.getColumnIndex("mile"));
            int coin = c.getInt(c.getColumnIndex("coin"));
            int rate = c.getInt(c.getColumnIndex("rate"));
            items.add(new HistoryItem(date, coin, mile, rate));
        }
        c.close();
        return items;
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME, null);
        return c;
    }

    public boolean isTableExist() {
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME, null);
        return c.isAfterLast();
    }

    /**
     * close database
     */
    public void closeDB() {
        // 释放数据库资源
        db.close();
    }

    /**
     * drop table
     *
     * @param table
     */
    public void dropTable(String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
    }

    public void createHistoryTable() {
        StringBuffer sBuffer = new StringBuffer();

        sBuffer.append("CREATE TABLE [" + DatabaseHelper.TABLE_NAME + "] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[date] TEXT,");
        sBuffer.append("[mile] INTEGER,");
        sBuffer.append("[coin] INTEGER,");
        sBuffer.append("[rate] INTEGER)");

        // 执行创建表的SQL语句
        db.execSQL(sBuffer.toString());
    }

}
