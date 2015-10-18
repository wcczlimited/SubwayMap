package com.sudalv.subway.util;

import com.baidu.mapapi.model.LatLng;
import com.sudalv.subway.listitem.LineItem;
import com.sudalv.subway.listitem.StationItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SunWe on 2015/10/6.
 */
public class BaiduMapUtils {
    private static List<LineItem> lines = new ArrayList<LineItem>();
    private static List<StationItem> stations = new ArrayList<StationItem>();
    private static List<LatLng> stationList = new ArrayList<LatLng>();
    private static HashMap<Integer, StationItem> idToStat = new HashMap<>();
    private static int busyIndex = 0;
    public static List<StationItem> initStations(InputStream input){
        System.out.println("----------BaiduMapUtils initStations");
        lines = new ArrayList<LineItem>();
        stations = new ArrayList<StationItem>();
        stationList = new ArrayList<LatLng>();
        idToStat = new HashMap<>();
        try {
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            String json = new String(buffer, "utf-8");
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("stations");
            for(int i=0; i<arr.length();i++){
                JSONObject temp = arr.getJSONObject(i);
                String name = temp.getString("name");
                double locX = temp.getDouble("locX");
                double locY = temp.getDouble("locY");
                int line = temp.getInt("line");
                int id = temp.getInt("id");
                stations.add(new StationItem(name,id,line,new LatLng(locY,locX)));
                idToStat.put(id, new StationItem(name, id, line, new LatLng(locY, locX)));
            }
            input.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        //Initialize subway Find Path
        CalLineUtils.initLineUtils(stations);
        return stations;
    }

    public static List<LatLng> getStationPosList(){
        stationList = new ArrayList<>();
        for(StationItem item : stations){
            stationList.add(item.getmPos());
        }
        return stationList;
    }

    public static ArrayList<LatLng> getRealtimeStationPosList(ArrayList<StationItem> stat) {
        ArrayList<LatLng> Real = new ArrayList<>();
        for (StationItem item : stat) {
            Real.add(item.getmPos());
        }
        return Real;
    }

    public static List<LineItem> initSubway(InputStream input){
        try {
            busyIndex = 0;
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            String json = new String(buffer, "utf-8");
            JSONObject obj = new JSONObject(json);
            addItemToList("line5",obj);
            addItemToList("line1",obj);
            addItemToList("line8",obj);
            addItemToList("line4",obj);
            addItemToList("line3",obj);
            addItemToList("line2",obj);
            addItemToList("line6",obj);
            addItemToList("line7",obj);
            addItemToList("line9",obj);
            addItemToList("line10",obj);
            addItemToList("line11",obj);
            addItemToList("line12",obj);
            addItemToList("line13",obj);
            addItemToList("line16", obj);
            input.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return lines;
    }

    private static void addItemToList( String lineName, JSONObject obj) throws Exception{
        JSONArray arr = obj.getJSONArray(lineName);
        for(int i=0; i<arr.length();i++){
            JSONObject tempObject = arr.getJSONObject(i);
            LineItem tempItem = new LineItem(tempObject.getString("from"),tempObject.getString("to"));
            JSONArray temparr = tempObject.getJSONArray("pos");
            for(int j=0; j<temparr.length();j++){
                JSONObject temp = temparr.getJSONObject(j);
                double locX = temp.getDouble("locX");
                double locY = temp.getDouble("locY");
                tempItem.addPos(locX,locY);
            }
            String busytemp = CsvUtils.getRow(busyIndex);
            String[] busyarr = busytemp.split(",");
            int grad = 0;
            if(busyarr[2].equals("0.8")){
                grad = 1;
            }else if(busyarr[2].equals("1")){
                grad = 2;
            }
            tempItem.setIsBusy(grad);
            lines.add(tempItem);
            busyIndex++;
        }
    }

    public static ArrayList<StationItem> getRealtimeStations(ArrayList<String> StList) {
        ArrayList<StationItem> res = new ArrayList<>();
        for (String name : StList) {
            for (StationItem item : stations) {
                if (name.equals(item.getmName())) {
                    res.add(item);
                }
            }
        }
        return res;
    }

    public static ArrayList<LineItem> getRealtimeLines(ArrayList<StationItem> StList) {
        ArrayList<LineItem> res = new ArrayList<>();
        String from = StList.get(0).getmName();
        for (int i = 1; i < StList.size(); i++) {
            String to = StList.get(i).getmName();
            for (LineItem line : lines) {
                if ((idToStat.get(line.getFrom()).getmName().equals(from) && idToStat.get(line.getTo()).getmName().equals(to)) || (idToStat.get(line.getFrom()).getmName().equals(to) && idToStat.get(line.getTo()).getmName().equals(from))) {
                    res.add(line);
                }
            }
            from = to;
        }
        return res;
    }
}
