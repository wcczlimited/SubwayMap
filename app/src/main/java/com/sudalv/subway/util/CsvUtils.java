package com.sudalv.subway.util;

/**
 * Created by weicheng on 10/18/15.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    private static String fileName = null;
    private static BufferedReader br = null;
    private static ArrayList<String> list = new ArrayList<String>();

    public CsvUtils() {

    }

    public static void initCsv(InputStream file) throws Exception {
        list = new ArrayList<String>();
        br = new BufferedReader(new InputStreamReader(file));
        String stemp;
        while ((stemp = br.readLine()) != null) {
            list.add(stemp);
        }
    }
    public static List getList() {
        return list;
    }

    public static String getRow(int index){
        return list.get(index);
    }
    /**
     * 获取行数
     * @return
     */
    public int getRowNum() {
        return list.size();
    }
}
