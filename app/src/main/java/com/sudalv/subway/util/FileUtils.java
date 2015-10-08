package com.sudalv.subway.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Created by SunWe on 2015/10/8.
 */
public class FileUtils {
    private static String encoding = "GBK";

    public static void outToFile(File dir, String filename, String str) {
        File file = new File(dir, filename);
        try {
            file.createNewFile(); // 创建文件
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte bt[] = new byte[1024];
        bt = str.getBytes();
        try {
            FileOutputStream in = new FileOutputStream(file);
            in.write(bt, 0, bt.length);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(File dir, String filename) {
        String result = null;
        File file = new File(dir, filename);
        if (!file.exists()) {
            return "";
        }
        try {
            FileInputStream out = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                result = tempString;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(filename + " " + result);
        return result;
    }
}
