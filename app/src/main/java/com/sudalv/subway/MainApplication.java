package com.sudalv.subway;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.beardedhen.androidbootstrap.TypefaceProvider;

/**
 * Created by SunWe on 2015/10/9.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        TypefaceProvider.registerDefaultIconSets();
    }
}
