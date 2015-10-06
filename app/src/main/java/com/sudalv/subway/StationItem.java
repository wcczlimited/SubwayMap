package com.sudalv.subway;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by SunWe on 2015/10/6.
 */
public class StationItem {
    String mName;
    int mId;
    int mLine;
    LatLng mPos;
    public StationItem(String name, int id, int line,  LatLng pos){
        mName = name;
        mPos = pos;
        mId = id;
        mLine = line;
    }
    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public LatLng getmPos() {
        return mPos;
    }

    public void setmPos(LatLng mPos) {
        this.mPos = mPos;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public int getmLine() {
        return mLine;
    }

    public void setmLine(int mLine) {
        this.mLine = mLine;
    }
}
