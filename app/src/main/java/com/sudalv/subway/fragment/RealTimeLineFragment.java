package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.listitem.LineItem;
import com.sudalv.subway.listitem.StationItem;
import com.sudalv.subway.util.BaiduMapUtils;
import com.sudalv.subway.util.GLUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RealTimeLineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RealTimeLineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealTimeLineFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "TITLE";
    private static final String ARG_POSOTION = "POSITION";

    private String mTitle;
    private ArrayList<String> mPosition;

    private OnFragmentInteractionListener mListener;

    // 定位相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    private BaiduMapCallBack myBaiduMapCallBack = new BaiduMapCallBack();
    private MyLocationConfiguration.LocationMode mCurrentMode;

    //List
    private ArrayList<StationItem> stations;
    private ArrayList<LineItem> lines;

    /*UI*/
    private View view;
    private Button btn_exit;
    private boolean isFirstLoc = true;// 是否首次定位

    //openGL
    private FloatBuffer vertexBuffer;
    private List<LatLng> stationList;
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
    private boolean mDrawLineFlag = true;

    public RealTimeLineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title
     * @param pos
     * @return A new instance of fragment RealTimeLineFragment.
     */
    public static RealTimeLineFragment newInstance(String title, ArrayList<String> pos) {
        RealTimeLineFragment fragment = new RealTimeLineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putStringArrayList(ARG_POSOTION, pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mPosition = getArguments().getStringArrayList(ARG_POSOTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        lines = new ArrayList<>();
        stations = new ArrayList<>();
        stationOverlayMap = new HashMap<>();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_real_time_line, container, false);
        initBaiduMap();
        stations = BaiduMapUtils.getRealtimeStations(mPosition);
        lines = BaiduMapUtils.getRealtimeLines(stations);
        System.out.println(lines.size() + " " + stations.size()+ " start");
        stationList = BaiduMapUtils.getRealtimeStationPosList(stations);
        drawStations();
        mBaiduMap.setOnMapDrawFrameCallback(myBaiduMapCallBack);
        return view;
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

    //初始化百度Map
    private void initBaiduMap() {
        btn_exit = (Button) view.findViewById(R.id.real_btn_exit);
        btn_exit.setText("退出实时监测");

        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("退出实时监测");
                ((LauncherActivity) getActivity()).changeUserHeader();
                getFragmentManager().popBackStack();
            }
        };
        btn_exit.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) view.findViewById(R.id.real_bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapStatusChangeListener(listener);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));
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
