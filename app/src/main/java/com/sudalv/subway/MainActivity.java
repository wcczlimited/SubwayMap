package com.sudalv.subway;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
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
    private float[] vertexs;
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
    }

    public void onMapDrawFrame(GL10 gl, MapStatus drawingMapStatus) {
        for(LineItem item : lines){
            if (mBaiduMap.getProjection() != null) {
                calPolylinePoint(drawingMapStatus,item.getPos());
                drawPolyline(gl, Color.argb(255, 0, 0, 0), vertexBuffer, 10, item.getPos().size(), drawingMapStatus);
            }
        }
    }

    public void calPolylinePoint(MapStatus mspStatus, List<LatLng> points) {
        PointF[] polyPoints = new PointF[points.size()];
        vertexs = new float[3 * points.size()];
        int i = 0;
        for (LatLng xy :points) {
            polyPoints[i] = mBaiduMap.getProjection().toOpenGLLocation(xy,
                    mspStatus);
            vertexs[i * 3] = polyPoints[i].x;
            vertexs[i * 3 + 1] = polyPoints[i].y;
            vertexs[i * 3 + 2] = 0.0f;
            i++;
        }
        vertexBuffer = makeFloatBuffer(vertexs);
    }

    private FloatBuffer makeFloatBuffer(float[] fs) {
        ByteBuffer bb = ByteBuffer.allocateDirect(fs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(fs);
        fb.position(0);
        return fb;
    }

    private void drawPolyline(GL10 gl, int color, FloatBuffer lineVertexBuffer,
                              float lineWidth, int pointSize, MapStatus drawingMapStatus) {

        gl.glEnable(GL10.GL_BLEND);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        float colorA = Color.alpha(color) / 255f;
        float colorR = Color.red(color) / 255f;
        float colorG = Color.green(color) / 255f;
        float colorB = Color.blue(color) / 255f;

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVertexBuffer);
        gl.glColor4f(colorR, colorG, colorB, colorA);
        gl.glLineWidth(lineWidth);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, pointSize);

        gl.glDisable(GL10.GL_BLEND);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
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
