package com.sudalv.subway.listitem;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SunWe on 2015/10/1.
 */
public class LineItem {
    public int isBusy = 0;
    List<LatLng> pos;
    private String from;
    private String to;
    public LineItem(String from, String to){
        this.from = from;
        this.to = to;
        pos = new ArrayList<LatLng>();
    }
    public void addPos(double locX, double locY){
        pos.add(new LatLng(locY,locX));
    }
    public List<LatLng> getPos(){
        return pos;
    }

    public int getFrom() {
        return Integer.parseInt(from);
    }

    public int getTo() {
        return Integer.parseInt(to);
    }

    public int getIsBusy() {
        return isBusy;
    }

    public void setIsBusy(int busy) {
        isBusy = busy;
    }
}
