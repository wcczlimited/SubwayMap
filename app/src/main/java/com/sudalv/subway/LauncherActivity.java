package com.sudalv.subway;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;

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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

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
    public MyLocationListenner myListener = new MyLocationListenner();
    private BaiduMapCallBack myBaiduMapCallBack= new BaiduMapCallBack();
    private MyLocationConfiguration.LocationMode mCurrentMode;

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
        setContentView(R.layout.activity_launcher);
        try {
            mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            // 设置抽屉
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
            initBaiduMap();
            lines = BaiduMapUtils.initSubway(getResources().openRawResource(R.raw.lines));
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

    @Override
    public void onNavigationDrawerItemSelected(String title) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (title){
            case "出行":
                fragmentManager.beginTransaction()
                        .replace(R.id.line_container, PlaceholderFragment.newInstance(title))
                        .commit();
                break;
            case "实时":
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(title))
                        .commit();
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(title))
                        .commit();
                break;
        }
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_TITLE = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String title) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SECTION_TITLE, title);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_launcher, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((LauncherActivity) activity).onSectionAttached(
                    getArguments().getString(ARG_SECTION_TITLE));
        }
    }


    public class BaiduMapCallBack implements BaiduMap.OnMapDrawFrameCallback {
        @Override
        public void onMapDrawFrame(GL10 gl10, MapStatus mapStatus) {
            int index = 0;
            for(LineItem item : lines){
                if (mBaiduMap.getProjection() != null) {
                    vertexBuffer = GLUtil.calPolylinePoint( mBaiduMap, mapStatus, item.getPos());
                    if(index %3 ==2)
                        GLUtil.drawPolyline(gl10, Color.argb(255, 207, 136, 49), vertexBuffer, 10, item.getPos().size(), mapStatus);
                    else if(index % 3 ==1)
                        GLUtil.drawPolyline(gl10, Color.argb(255, 180, 0, 0), vertexBuffer, 10, item.getPos().size(), mapStatus);
                    else
                        GLUtil.drawPolyline(gl10, Color.argb(255, 152, 191, 85), vertexBuffer, 10, item.getPos().size(), mapStatus);
                    index++;
                }
            }
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

}
