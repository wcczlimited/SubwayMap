package com.sudalv.subway;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity implements BaiduMap.OnMapDrawFrameCallback {
    private SlidingMenu slidingMenu;
    // 定位相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;

    // UI相关
    private Button mapModeButton;
    private boolean isFirstLoc = true;// 是否首次定位

    private List<LineItem> lines;

    //openGL
    private FloatBuffer vertexBuffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());//这句话一定要放在最开始
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSlidingMenu();
        initBaiduMap();
        initSubway();
        mBaiduMap.setOnMapDrawFrameCallback(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_home);
    }

    public void onMapDrawFrame(GL10 gl, MapStatus drawingMapStatus) {
        int index = 0;
        for(LineItem item : lines){
            if (mBaiduMap.getProjection() != null) {
                vertexBuffer = GLUtil.calPolylinePoint( mBaiduMap, drawingMapStatus, item.getPos());
                if(index %3 ==2)
                    GLUtil.drawPolyline(gl, Color.argb(255, 207, 136, 49), vertexBuffer, 10, item.getPos().size(), drawingMapStatus);
                else if(index % 3 ==1)
                    GLUtil.drawPolyline(gl, Color.argb(255, 180, 0, 0), vertexBuffer, 10, item.getPos().size(), drawingMapStatus);
                else
                    GLUtil.drawPolyline(gl, Color.argb(255, 152, 191, 85), vertexBuffer, 10, item.getPos().size(), drawingMapStatus);
                index++;
            }
        }
    }


    private void initSubway(){
        try {
            InputStream input = getResources().openRawResource(R.raw.subway);
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
            }
            input.close();
            input = getResources().openRawResource(R.raw.lines);
            buffer = new byte[input.available()];
            input.read(buffer);
            json = new String(buffer, "utf-8");
            obj = new JSONObject(json);
            lines = new ArrayList<LineItem>();
            addItemToList("line1", obj);
            addItemToList("line5",obj);
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
            addItemToList("line16",obj);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void addItemToList(String lineName, JSONObject obj) throws Exception{
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
            lines.add(tempItem);
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    //初始化百度Map
    private void initBaiduMap(){
        mapModeButton = (Button) findViewById(R.id.mapmode);
        mCurrentMode = LocationMode.NORMAL;
        mapModeButton.setText("普通");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        mapModeButton.setText("流量");
                        break;
                    case FOLLOWING:
                        mapModeButton.setText("普通");
                        break;
                }
            }
        };
        mapModeButton.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    //初始化slidingmenu
    private void initSlidingMenu(){
        slidingMenu = new SlidingMenu(this);//创建对象
        slidingMenu.setMode(SlidingMenu.LEFT);//设定模式，SlidingMenu在左边
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);//配置slidingmenu偏移出来的尺寸
        //slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//全屏都可以拖拽触摸，打开slidingmenu
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//附加到当前的Aty上去
        slidingMenu.setMenu(R.layout.slidingmenumain);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //重载
        switch(keyCode){
            case KeyEvent.KEYCODE_MENU:
                slidingMenu.toggle(true);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        super.onResume();
    }
    @Override
    protected void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }
}
