package com.sudalv.subway.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.util.FileUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_PARAM2 = "param2";

    private String mTitle;
    /*UI*/
    private View view;
    private Switch sw_wakelock;
    private BootstrapButton btn_quit;


    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title
     * @return A new instance of fragment SettingFragment.
     */
    public static SettingFragment newInstance(String title) {
        SettingFragment fragment = new SettingFragment();
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
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        sw_wakelock = (Switch) view.findViewById(R.id.setting_switch_wakelock);
        String raw_wake = FileUtils.readFromFile(getActivity().getFilesDir(), "wake_lock");
        if (raw_wake.equals("1")) {
            sw_wakelock.setChecked(true);
        } else {
            sw_wakelock.setChecked(false);
        }
        sw_wakelock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FileUtils.outToFile(getActivity().getFilesDir(), "wake_lock", "1");
                    LauncherActivity.wake = true;
                    return;
                }
                FileUtils.outToFile(getActivity().getFilesDir(), "wake_lock", "0");
                LauncherActivity.wake = false;
            }
        });
        btn_quit = (BootstrapButton) view.findViewById(R.id.setting_btn_quit);
        btn_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
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
    }
}