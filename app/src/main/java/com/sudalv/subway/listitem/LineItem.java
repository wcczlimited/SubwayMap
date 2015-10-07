package com.sudalv.subway.listitem;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SunWe on 2015/10/1.
 */
public class LineItem {
    private String from;
    private String to;
    List<LatLng> pos;
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
}
