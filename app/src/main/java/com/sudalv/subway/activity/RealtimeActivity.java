package com.sudalv.subway.activity;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.sudalv.subway.R;
import com.sudalv.subway.listitem.HistoryItem;
import com.sudalv.subway.listitem.LineItem;
import com.sudalv.subway.listitem.StationItem;
import com.sudalv.subway.util.BaiduMapUtils;
import com.sudalv.subway.util.GLUtil;
import com.sudalv.subway.util.HistoryUtils;

import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

public class RealtimeActivity extends Activity {
    private ArrayList<String> mPosition;
    private boolean mDrawLineFlag = true;
    //出行相关
    private double mTotalDistance = 0;
    private int coin = 0;

    // 定位相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    private BaiduMapCallBack myBaiduMapCallBack = new BaiduMapCallBack();

    //List
    private ArrayList<StationItem> stations;
    private ArrayList<LineItem> lines;

    /*UI*/
    private Button btn_exit;
    private boolean isFirstLoc = true;// 是否首次定位

    //openGL
    private FloatBuffer vertexBuffer;
    private Map<Marker, String> stationOverlayMap;
    BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {
        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         *
         * @param status 地图状态改变开始时的地图状态
         */
        public void onMapStatusChangeStart(MapStatus status) {
        }

        /**
         * 地图状态变化中
         *
         * @param status 当前地图状态
         */
        public void onMapStatusChange(MapStatus status) {
            if (status.zoom < 13) {
                if (!stationOverlayMap.isEmpty()) {
                    for (Map.Entry<Marker, String> overlay : stationOverlayMap.entrySet()) {
                        overlay.getKey().remove();
                    }
                    stationOverlayMap.clear();
                }
            }
        }

        /**
         * 地图状态改变结束
         *
         * @param status 地图状态改变结束后的地图状态
         */
        public void onMapStatusChangeFinish(MapStatus status) {
            if (stationOverlayMap.isEmpty()) {
                if (status.zoom >= 13) {
                    drawStations();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime);
        mPosition = getIntent().getStringArrayListExtra("position");
        coin = getIntent().getIntExtra("coin", 0);
        lines = new ArrayList<>();
        stations = new ArrayList<>();
        stationOverlayMap = new HashMap<>();
        initBaiduMap();
        stations = BaiduMapUtils.getRealtimeStations(mPosition);
        lines = BaiduMapUtils.getRealtimeLines(stations);
        drawStations();
        mBaiduMap.setOnMapDrawFrameCallback(myBaiduMapCallBack);
        for (int i = 1; i < stations.size(); i++) {
            mTotalDistance += getDistanceFromTwoPoints(stations.get(i).getmPos(), stations.get(i - 1).getmPos());
        }
    }

    //初始化百度Map
    private void initBaiduMap() {
        btn_exit = (Button) findViewById(R.id.real_btn_exit);
        btn_exit.setText("退出实时监测");

        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("退出实时监测");
                finish();
            }
        };
        btn_exit.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.real_bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapStatusChangeListener(listener);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this.getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void drawStations() {
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_track);
        for (StationItem item : stations) {
            //定义Maker坐标点
            LatLng point = item.getmPos();
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            Marker temp = (Marker) mBaiduMap.addOverlay(option);
            stationOverlayMap.put(temp, item.getmName());
        }
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

    private double getDistanceFromTwoPoints(LatLng pos1, LatLng pos2) {
        double pk = 180 / 3.14;
        double a1 = pos1.latitude / pk;
        double a2 = pos1.longitude / pk;
        double b1 = pos2.latitude / pk;
        double b2 = pos2.longitude / pk;
        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        return 6366000 * tt;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public class BaiduMapCallBack implements BaiduMap.OnMapDrawFrameCallback {
        @Override
        public void onMapDrawFrame(GL10 gl10, MapStatus mapStatus) {
            if (!mDrawLineFlag)
                return;
            int lineWidth = 10;
            switch ((int) mapStatus.zoom) {
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
            if (mapStatus.zoom > 11) {
                int index = 0;
                for (LineItem item : lines) {
                    if (mBaiduMap.getProjection() != null) {
                        vertexBuffer = GLUtil.calPolylinePoint(mBaiduMap, mapStatus, item.getPos());
                        if (item.getIsBusy() == 2)
                            GLUtil.drawPolyline(gl10, Color.argb(255, 207, 136, 49), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        else if (item.getIsBusy() == 1)
                            GLUtil.drawPolyline(gl10, Color.argb(255, 180, 0, 0), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        else
                            GLUtil.drawPolyline(gl10, Color.argb(255, 152, 191, 85), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        index++;
                    }
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
            if (stations.size() == 0)
                return;
            LatLng lastpos = stations.get(0).getmPos();
            double distance = getDistanceFromTwoPoints(lastpos,
                    new LatLng(location.getLatitude(), location.getLongitude()));
            if (distance < 3000) {
                StationItem temp = stations.remove(0);
                Toast.makeText(RealtimeActivity.this, "你已经经过了" + temp.getmName(), Toast.LENGTH_LONG).show();
            }
            if (stations.size() == 0) {
                Toast.makeText(RealtimeActivity.this, "您完成了本次出行, 距离为" + mTotalDistance + "，获得绿币" + coin, Toast.LENGTH_LONG).show();
                HistoryItem item = HistoryUtils.getLastHistroyRecord(RealtimeActivity.this);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
                String currDate = df.format(new Date());// new Date()为获取当前系统时间
                System.out.println(currDate);
                System.out.println(item.getmDate());
                int mile = (int) Math.ceil(mTotalDistance / 1000);
                if (item == null || !item.getmDate().equals(currDate)) {
                    HistoryItem thisItem = new HistoryItem(currDate, coin, mile, 50);
                    HistoryUtils.insetHistoryItem(RealtimeActivity.this, thisItem);
                } else {
                    HistoryItem thisItem = new HistoryItem(currDate, coin, mile, 50);
                    HistoryUtils.updateHistoryItem(RealtimeActivity.this, thisItem);
                }
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
