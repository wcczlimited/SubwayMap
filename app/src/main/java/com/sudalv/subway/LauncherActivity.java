package com.sudalv.subway;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.sudalv.subway.fragment.MapFragment;
import com.sudalv.subway.fragment.NavigationDrawerFragment;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

public class LauncherActivity extends Activity
        implements  NavigationDrawerFragment.NavigationDrawerCallbacks{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    // 定位相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    private BaiduMapCallBack myBaiduMapCallBack= new BaiduMapCallBack();
    private MyLocationConfiguration.LocationMode mCurrentMode;

    // UI相关
    private Button mapModeButton;
    private boolean isFirstLoc = true;// 是否首次定位

    private List<LineItem> lines;
    private List<StationItem> stations;

    //openGL
    private FloatBuffer vertexBuffer;
    private List<LatLng> stationList;
    private Map<Marker,String> stationOverlayMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());//这句话一定要放在最开始
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        try {
            mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            // 设置抽屉
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
            initBaiduMap();
            lines = BaiduMapUtils.initSubway(getResources().openRawResource(R.raw.lines));
            stations = BaiduMapUtils.initStations(getResources().openRawResource(R.raw.subway));
            stationList = BaiduMapUtils.getStationPosList();
            stationOverlayMap = new HashMap<>();
            drawStations();
            mBaiduMap.setOnMapDrawFrameCallback(myBaiduMapCallBack);
        }catch (Exception e){
            e.printStackTrace();
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
                        mCurrentMode = LocationMode.FOLLOWING;
                        break;
                    case FOLLOWING:
                        mapModeButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        break;
                }
            }
        };
        mapModeButton.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapStatusChangeListener(listener);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));
        mBaiduMap.setOnMarkerClickListener(Markerlistener);
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

    private void drawStations(){
        for (StationItem item : stations) {
            //定义Maker坐标点
            LatLng point = item.getmPos();
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_track);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            Marker temp= (Marker)mBaiduMap.addOverlay(option);
            stationOverlayMap.put(temp,item.getmName());
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String title) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        System.out.println(title);
        if(title.equals("实时")){
            int num = getFragmentManager().getBackStackEntryCount();
            String numString = "++++++++++++++++++++++++++++++++++Fragment回退栈数量：" + num;
            for (int i = 0; i < num; i++) {
                FragmentManager.BackStackEntry backstatck = getFragmentManager().getBackStackEntryAt(i);
            }
            return;
        }

        fragmentManager.beginTransaction()
                .add(R.id.container, MapFragment.newInstance(title))
                .commit();
    }

    public void onSectionAttached(String title) {
        mTitle = title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public class BaiduMapCallBack implements BaiduMap.OnMapDrawFrameCallback {
        @Override
        public void onMapDrawFrame(GL10 gl10, MapStatus mapStatus) {
            int lineWidth = 10;
            switch ((int)mapStatus.zoom){
                case 11:
                    lineWidth = 8;
                    break;
                case 12:
                    lineWidth = 10;
                    break;
                case 13:
                    lineWidth = 12;
                    break;
                case 14:
                    lineWidth = 15;
                    break;
                case 15:
                    lineWidth = 17;
                    break;
                case 16:
                    lineWidth = 19;
                    break;
                case 17:
                    lineWidth = 22;
                    break;
                case 18:
                    lineWidth = 24;
                    break;
            }
            if(mapStatus.zoom >11) {
                int index = 0;
                for (LineItem item : lines) {
                    if (mBaiduMap.getProjection() != null) {
                        vertexBuffer = GLUtil.calPolylinePoint(mBaiduMap, mapStatus, item.getPos());
                        if (index % 3 == 2)
                            GLUtil.drawPolyline(gl10, Color.argb(255, 207, 136, 49), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        else if (index % 3 == 1)
                            GLUtil.drawPolyline(gl10, Color.argb(255, 180, 0, 0), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        else
                            GLUtil.drawPolyline(gl10, Color.argb(255, 152, 191, 85), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        index++;
                    }
                }
            }
        }
    }

    BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {
        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         * @param status 地图状态改变开始时的地图状态
         */
        public void onMapStatusChangeStart(MapStatus status){
        }
        /**
         * 地图状态变化中
         * @param status 当前地图状态
         */
        public void onMapStatusChange(MapStatus status){
            if(status.zoom < 13){
                if(!stationOverlayMap.isEmpty()) {
                    for (Map.Entry<Marker,String> overlay : stationOverlayMap.entrySet()) {
                        overlay.getKey().remove();
                    }
                    stationOverlayMap.clear();
                }
            }
        }
        /**
         * 地图状态改变结束
         * @param status 地图状态改变结束后的地图状态
         */
        public void onMapStatusChangeFinish(MapStatus status){
            if(stationOverlayMap.isEmpty()) {
                if (status.zoom >= 13) {
                    drawStations();
                }
            }
        }
    };

    BaiduMap.OnMarkerClickListener Markerlistener = new BaiduMap.OnMarkerClickListener() {
        /**
         * 地图 Marker 覆盖物点击事件监听函数
         * @param marker 被点击的 marker
         */
        public boolean onMarkerClick(Marker marker){
            System.out.println(stationOverlayMap.get(marker));
            return false;
        }
    };

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
