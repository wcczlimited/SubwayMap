package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Toast;

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
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.listitem.LineItem;
import com.sudalv.subway.listitem.StationItem;
import com.sudalv.subway.util.BaiduMapUtils;
import com.sudalv.subway.util.CsvUtils;
import com.sudalv.subway.util.GLUtil;

import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment{
    private static final String ARG_TITLE = "title";

    private String mTitle;
    private View view;
    private OnFragmentInteractionListener mListener;

    // 定位相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    private BaiduMapCallBack myBaiduMapCallBack= new BaiduMapCallBack();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private boolean isFirstLoc = true;// 是否首次定位
    private List<LineItem> lines;
    private List<StationItem> stations;

    // UI相关
    private BootstrapButton mapModeButton, mPickerConfirm;
    private NumberPicker mHourPicker, mMinutePicker;
    //openGL
    private FloatBuffer vertexBuffer;
    private List<LatLng> stationList;
    private Map<Marker,String> stationOverlayMap;
    BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {
        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         * @param status 地图状态改变开始时的地图状态
         */
        public void onMapStatusChangeStart(MapStatus status) {
        }

        /**
         * 地图状态变化中
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
    BaiduMap.OnMarkerClickListener Markerlistener = new BaiduMap.OnMarkerClickListener() {
        /**
         * 地图 Marker 覆盖物点击事件监听函数
         * @param marker 被点击的 marker
         */
        public boolean onMarkerClick(Marker marker) {
            showPopwindow(marker);
            return true;
        }
    };
    private boolean mDrawLineFlag = true;

    public MapFragment() {
        // Required empty public constructor
        lines = new ArrayList<>();
        stations = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title The title of the Fragment.
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance(String title) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    //初始化百度Map
    private void initBaiduMap(){
        mapModeButton = (BootstrapButton) view.findViewById(R.id.mapmode);
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mapModeButton.setText("流量");

        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        mapModeButton.setText("流量");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mDrawLineFlag = true;
                        break;
                    case FOLLOWING:
                        mapModeButton.setText("普通");
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        mDrawLineFlag = false;
                        break;
                }
                refreshMapview();
            }
        };
        mapModeButton.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapStatusChangeListener(listener);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));
        mBaiduMap.setOnMarkerClickListener(Markerlistener);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this.getActivity().getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapView.refreshDrawableState();
    }

    private void drawStations(){
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
            Marker temp= (Marker)mBaiduMap.addOverlay(option);
            stationOverlayMap.put(temp,item.getmName());
        }
    }

    private void showPopwindow(final Marker marker) {
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.station_popup, null);

        // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()
        final PopupWindow window = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        window.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        window.setBackgroundDrawable(dw);

        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.AnimBottom);
        // 在底部显示
        window.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);

        Button start = (Button) view.findViewById(R.id.btn_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((LauncherActivity) getActivity()).onNavigationDrawerItemSelected("出行");
                String station = stationOverlayMap.get(marker);
                LauncherActivity.user_select_start = station;
                Toast.makeText(getActivity(),"起点设置为"+station,Toast.LENGTH_SHORT).show();
                window.dismiss();
            }
        });

        Button end = (Button)view.findViewById(R.id.btn_end);
        end.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String station = stationOverlayMap.get(marker);
                LauncherActivity.user_select_end = station;
                Toast.makeText(getActivity(),"终点设置为"+station,Toast.LENGTH_SHORT).show();
                window.dismiss();
            }
        });

        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });

        //popWindow消失监听方法
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                System.out.println("popWindow消失");
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplication());
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            view = inflater.inflate(R.layout.fragment_launcher, container, false);
            mHourPicker = (NumberPicker) view.findViewById(R.id.hourPicker);
            mMinutePicker = (NumberPicker) view.findViewById(R.id.minutePicker);
            initPicker();
            initBaiduMap();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
            String curDate = df.format(new Date());// new Date()为获取当前系统时间
            String curTime = curDate.split(" ")[1];
            int hour = Integer.parseInt(curTime.split(":")[0]);
            int min = Integer.parseInt(curTime.split(":")[1]);
            int id = (hour - 5) * 4 + min / 15 + R.raw.csv_grad_ll_01;
            if (hour >= 23 || hour < 5)
                id = R.raw.csv_grad_ll_01;
            CsvUtils.initCsv(getResources().openRawResource(id));
            stations = BaiduMapUtils.initStations(getResources().openRawResource(R.raw.subway));
            lines = BaiduMapUtils.initSubway(getResources().openRawResource(R.raw.lines));
            stationList = BaiduMapUtils.getStationPosList();
            stationOverlayMap = new HashMap<>();
            drawStations();
            mBaiduMap.setOnMapDrawFrameCallback(myBaiduMapCallBack);
            mPickerConfirm = (BootstrapButton) view.findViewById(R.id.picker_confirm);
            mPickerConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int hour = mHourPicker.getValue();
                    int min = mMinutePicker.getValue();
                    int index = (hour - 5) * 4 + min / 15;
                    int id = R.raw.csv_grad_ll_01 + index;
                    try {
                        CsvUtils.initCsv(getResources().openRawResource(id));
                        lines = BaiduMapUtils.updateLineBusy();
                        refreshMapview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void refreshMapview() {
        LatLng temp = mBaiduMap.getMapStatus().target;
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(temp);
        mBaiduMap.animateMapStatus(u);
    }

    private void initPicker() {
        mHourPicker.setMaxValue(22);
        mHourPicker.setMinValue(5);
        mMinutePicker.setMaxValue(59);
        mMinutePicker.setMinValue(0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((LauncherActivity) activity).onSectionAttached(
                getArguments().getString(ARG_TITLE));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                        if (item.getIsBusy() == 1)
                            GLUtil.drawPolyline(gl10, Color.argb(255, 207, 136, 49), vertexBuffer, lineWidth, item.getPos().size(), mapStatus);
                        else if (item.getIsBusy() == 2)
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
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
