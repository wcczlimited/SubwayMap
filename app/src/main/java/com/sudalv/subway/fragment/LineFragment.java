package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sudalv.subway.LauncherActivity;
import com.sudalv.subway.R;
import com.sudalv.subway.util.DateTimePickDialogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LineFragment extends Fragment {
    private static final String ARG_TITLE = "title";

    private String mTitle;

    private OnFragmentInteractionListener mListener;
    private View view;
    private ListView mListView;
    private EditText mStartEditText;
    private EditText mEndEditText;
    private TextView mTimeTextView;

    private String initStartDateTime = "2013年9月3日 14:44"; // 初始化开始时间
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title The title of the Fragment.
     * @return A new instance of fragment MapFragment.
     */
    public static LineFragment newInstance(String title) {
        LineFragment fragment = new LineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public LineFragment() {
        // Required empty public constructor
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
        view = inflater.inflate(R.layout.fragment_line, container, false);
        mListView = (ListView)view.findViewById(R.id.line_listView);
        MyAdapter ba = new MyAdapter(this.getActivity());
        mListView.setAdapter(ba);
        mStartEditText = (EditText)view.findViewById(R.id.line_start_text);
        mEndEditText = (EditText)view.findViewById(R.id.line_end_text);
        mStartEditText.setText(LauncherActivity.user_select_start);
        mEndEditText.setText(LauncherActivity.user_select_end);
        mTimeTextView = (TextView)view.findViewById(R.id.line_time_text);
        mTimeTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//设置日期格式
                initStartDateTime = df.format(new Date());// new Date()为获取当前系统时间
                DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
                        getActivity(), initStartDateTime);
                dateTimePicKDialog.dateTimePicKDialog(mTimeTextView);

            }
        });
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

    private class MyAdapter extends BaseAdapter{
        private Context mContext;
        public MyAdapter(Context context){
            mContext = context;
        }
        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView=LayoutInflater.from(mContext).inflate(R.layout.line_list_item, null);
            return convertView;
        }
    }

}
