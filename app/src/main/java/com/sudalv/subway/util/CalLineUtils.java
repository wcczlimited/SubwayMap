package com.sudalv.subway.util;

import com.sudalv.subway.listitem.StationItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by SunWe on 2015/10/13.
 */
public class CalLineUtils {
    private static Map<String, List<String>> mGraph;
    private static List<List<String>> mResult;
    private static Map<String, List<Integer>> mStationInfo;
    private static Map<Integer, List<StationItem>> mLinesMap;

    public static void initLineUtils(List<StationItem> stations) {
        System.out.println("--------CalculateUtils initLineUtils");
        mLinesMap = new HashMap<>();
        mGraph = new HashMap<>();
        mStationInfo = new HashMap<>();
        for (StationItem item : stations) {
            int line = item.getmLine();
            if (mLinesMap.containsKey(line)) {
                mLinesMap.get(line).add(item);
            } else {
                List<StationItem> temp = new ArrayList<>();
                temp.add(item);
                mLinesMap.put(line, temp);
            }
            if (!mGraph.containsKey(item.getmName())) {
                List<String> temp = new ArrayList<>();
                mGraph.put(item.getmName(), temp);
            }
            if (!mStationInfo.containsKey(item.getmName())) {
                List<Integer> temp = new ArrayList<>();
                temp.add(item.getmLine());
                mStationInfo.put(item.getmName(), temp);
            } else {
                mStationInfo.get(item.getmName()).add(item.getmLine());
            }
        }
//        for(Map.Entry<Integer,List<StationItem>> item: mLinesMap.entrySet()){
//            List<StationItem> line = item.getValue();
//            System.out.print(item.getKey()+" ");
//            for(StationItem station : line){
//                System.out.print(station.getmName() + " ");
//            }
//            System.out.println();
//        }
        for (Map.Entry<Integer, List<StationItem>> item : mLinesMap.entrySet()) {
            List<StationItem> line = item.getValue();
            for (int i = 0; i < line.size(); i++) {
                if (i == 0) {
                    if (!mGraph.get(line.get(i).getmName()).contains(line.get(i + 1).getmName())) {
                        mGraph.get(line.get(i).getmName()).add(line.get(i + 1).getmName());
                    }
                    if (!mGraph.get(line.get(i + 1).getmName()).contains(line.get(i).getmName())) {
                        mGraph.get(line.get(i + 1).getmName()).add(line.get(i).getmName());
                    }
                    continue;
                }
                if (i == line.size() - 1) {
                    if (!mGraph.get(line.get(i).getmName()).contains(line.get(i - 1).getmName())) {
                        mGraph.get(line.get(i).getmName()).add(line.get(i - 1).getmName());
                    }
                    if (!mGraph.get(line.get(i - 1).getmName()).contains(line.get(i).getmName())) {
                        mGraph.get(line.get(i - 1).getmName()).add(line.get(i).getmName());
                    }
                    continue;
                }
                if (!mGraph.get(line.get(i).getmName()).contains(line.get(i + 1).getmName())) {
                    mGraph.get(line.get(i).getmName()).add(line.get(i + 1).getmName());
                }
                if (!mGraph.get(line.get(i + 1).getmName()).contains(line.get(i).getmName())) {
                    mGraph.get(line.get(i + 1).getmName()).add(line.get(i).getmName());
                }
                if (!mGraph.get(line.get(i).getmName()).contains(line.get(i - 1).getmName())) {
                    mGraph.get(line.get(i).getmName()).add(line.get(i - 1).getmName());
                }
                if (!mGraph.get(line.get(i - 1).getmName()).contains(line.get(i).getmName())) {
                    mGraph.get(line.get(i - 1).getmName()).add(line.get(i).getmName());
                }
            }
        }
//        for(Map.Entry<String,List<String>> item : mGraph.entrySet()){
//            List<String> nei = item.getValue();
//            String name = item.getKey();
//            System.out.print(name+" ");
//            for(String neis: nei){
//                System.out.print(neis+" ");
//            }
//            System.out.println();
//        }

//        for(Map.Entry<String,List<String>> item: mGraph.entrySet()){
//            System.out.print(item.getKey()+"->");
//            for(String name: item.getValue()){
//                System.out.print(name+" ");
//            }
//            System.out.println();
//        }
    }

    public static List<List<String>> getResult(String start, String end) {
        mResult = new ArrayList<>();
        if (start.equals(end)) {
            return mResult;
        }
        List<String> directRes = FindDirectPath(start, end);
        if (directRes.size() != 0) {
            mResult.add(directRes);
            return mResult;
        }
        Queue<List<String>> pathQueue = new LinkedList<>();
        List<String> tempPath = new ArrayList<>();
        tempPath.add(start);
        pathQueue.add(tempPath);

        String lastNode = start;
        while (pathQueue.size() > 0) {
            List<String> path = pathQueue.poll();
            //如果已经超过最短路径，直接返回
            if (mResult.size() > 0 && path.size() > mResult.get(0).size()) {
                continue;
            }
            //路径的最后一个节点
            lastNode = path.size() > 0 ? path.get(path.size() - 1) : start;

            for (String item : mGraph.get(lastNode)) {
                if (item.equals(lastNode)) {
                    continue;
                }
                if (item.equals(end)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(end);
                    mResult.add(newPath);
                } else if (!path.contains(item)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(item);
                    pathQueue.add(newPath);
                }
            }
        }
        return mResult;
    }

    public static List<List<String>> getSimplePath(List<List<String>> path) {
        List<List<String>> newPath = new ArrayList<>();
        try {
            for (int i = 0; i < path.size(); ++i) {
                List<String> temp = new ArrayList<>();
                temp.add(path.get(i).get(0));
                List<Integer> statline = new ArrayList<>(mStationInfo.get(path.get(i).get(0)));
                List<Integer> second = new ArrayList<>(mStationInfo.get(path.get(i).get(1)));
                statline.retainAll(second);
                int lastlineid = second.get(0);
                for (int j = 1; j < path.get(i).size(); j++) {
                    List<Integer> lastLine = new ArrayList<>(mStationInfo.get(path.get(i).get(j - 1)));
                    List<Integer> currLine = new ArrayList<>(mStationInfo.get(path.get(i).get(j)));
//                    for (Integer item : lastLine){
//                        System.out.print(item +" ");
//                    }
//                    System.out.println();
//                    for (Integer item : currLine){
//                        System.out.print(item +" ");
//                    }
//                    System.out.println();
                    lastLine.retainAll(currLine);
                    int curlineid = lastLine.get(0);
                    if (curlineid != lastlineid) {
                        temp.add(path.get(i).get(j - 1));
                        lastlineid = curlineid;
                    }
                }
                temp.add(path.get(i).get(path.get(i).size() - 1));
                newPath.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newPath;
    }

    private static List<String> FindDirectPath(String start, String end) {
        List<String> result = new ArrayList<>();

        List<Integer> startLine = mStationInfo.get(start);
        List<Integer> endLine = mStationInfo.get(end);
        boolean flag = false;
        int line = 0;
        for (int i = 0; i < startLine.size(); i++) {
            for (int j = 0; j < endLine.size(); ++j) {
                if (startLine.get(i).equals(endLine.get(j))) {
                    flag = true;
                    line = startLine.get(i);
                }
            }
        }
        if (!flag)
            return result;
        List<StationItem> lineList = mLinesMap.get(line);
        int startIndex = 0, endIndex = 0;
        for (int i = 0; i < lineList.size(); i++) {
            startIndex = lineList.get(i).getmName().equals(start) ? i : startIndex;
            endIndex = lineList.get(i).getmName().equals(end) ? i : endIndex;
        }
        for (int i = startIndex; i <= endIndex; ++i) {
            result.add(lineList.get(i).getmName());
        }
        return result;
    }

}