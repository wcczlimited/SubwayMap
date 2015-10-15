package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.util.HistoryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    private static final String ARG_TITLE = "title";

    private String mTitle;

    private OnFragmentInteractionListener mListener;
    /*UI*/
    private View view;
    private CombinedChart mChart;
    private TextView mMileText, mCoinText, mRateText;
    private List<Integer> mMileList, mCoinList, mRateList;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @return A new instance of fragment HistoryFragment.
     */
    public static HistoryFragment newInstance(String title) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_history, container, false);
        //Set the three line UI of history
        mMileText = (TextView) view.findViewById(R.id.history_mile);
        mCoinText = (TextView) view.findViewById(R.id.history_coin);
        mRateText = (TextView) view.findViewById(R.id.history_rate);
        mMileText.setText(HistoryUtils.getTotalMiles() + "km");
        mCoinText.setText(HistoryUtils.getTotalCoins() + "");
        mRateText.setText(HistoryUtils.getAverageRate() + "%");

        //Set the chart due to the data from HistoryUtils
        mChart = (CombinedChart) view.findViewById(R.id.history_chart);
        mChart.setDescription("");
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        //draw bars behind lines
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //Set chart data
        CombinedData data = new CombinedData(HistoryUtils.getXAxis());
        mMileList = HistoryUtils.getMilesArray();
        mCoinList = HistoryUtils.getCoinArray();
        mRateList = HistoryUtils.getRateArray();
        data.setData(generateBarData(mCoinList, "绿币"));
        data.setData(generateLineData(mMileList, "出行", Color.rgb(240, 238, 70)));
        mChart.setData(data);

        //Set Select Listener of the chart
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                int index = e.getXIndex();
                mMileText.setText(mMileList.get(index) + "km");
                mCoinText.setText(mCoinList.get(index) + "");
                mRateText.setText(mRateList.get(index) + "%");
            }

            @Override
            public void onNothingSelected() {

            }
        });
        mChart.invalidate();
        return view;
    }

    private LineData generateLineData(List<Integer> data, String name, int color) {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < data.size(); index++)
            entries.add(new Entry(data.get(index), index));

        LineDataSet set = new LineDataSet(entries, name);
        set.setColor(color);
        set.setLineWidth(2.5f);
        set.setCircleColor(color);
        set.setCircleSize(5f);
        set.setFillColor(color);
        set.setDrawCubic(true);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(color);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);

        return d;
    }

    private BarData generateBarData(List<Integer> data, String name) {

        BarData d = new BarData();

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        for (int index = 0; index < data.size(); index++)
            entries.add(new BarEntry(data.get(index), index));

        BarDataSet set = new BarDataSet(entries, name);
        set.setColor(Color.rgb(60, 220, 78));
        set.setValueTextColor(Color.rgb(60, 220, 78));
        set.setValueTextSize(10f);
        d.addDataSet(set);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return d;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
