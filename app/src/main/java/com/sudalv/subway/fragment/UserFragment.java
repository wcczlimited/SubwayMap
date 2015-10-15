package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.soundcloud.android.crop.Crop;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.AboutActivity;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.activity.UserImageActivity;
import com.sudalv.subway.util.DBManager;
import com.sudalv.subway.util.DatabaseHelper;
import com.sudalv.subway.util.HistoryUtils;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {
    private static final String ARG_TITLE = "title";

    private String mTitle;
    private View view;

    private OnFragmentInteractionListener mListener;
    /*UI*/
    private BootstrapCircleThumbnail faceImage;
    private BootstrapButton btn_setting;
    private Button btn_history, btn_card, btn_about;
    private TextView mMileText, mCoinText, mRateText;

    public UserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @return A new instance of fragment UserFragment.
     */
    public static UserFragment newInstance(String title) {
        UserFragment fragment = new UserFragment();
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
        view = inflater.inflate(R.layout.fragment_user, container, false);
        //read history data from local file && set history UI

        DBManager dbManager = new DBManager(getActivity());
        if (dbManager.isTableExist()) {
            HistoryUtils.readHistoryFromFile(getResources().openRawResource(R.raw.history));
            System.out.println("First Create an default Database");
            dbManager.dropTable(DatabaseHelper.TABLE_NAME);
            dbManager.createHistoryTable();
            dbManager.add(HistoryUtils.getHistoryList());
            dbManager.closeDB();
        }
        HistoryUtils.getHistoryFromDataBase(getActivity());
        mMileText = (TextView) view.findViewById(R.id.user_miles_text);
        mCoinText = (TextView) view.findViewById(R.id.user_coins_text);
        mRateText = (TextView) view.findViewById(R.id.user_rates_text);
        mMileText.setText(HistoryUtils.getTotalMiles() + "");
        mCoinText.setText(HistoryUtils.getTotalCoins() + "");
        mRateText.setText(HistoryUtils.getAverageRate() + "%");

        //set User face Image
        File face = new File(getActivity().getFilesDir(),"faceimage_cropped");
        faceImage = (BootstrapCircleThumbnail)view.findViewById(R.id.user_face_image);
        if(face.exists()){
            faceImage.setImageBitmap(BitmapFactory.decodeFile(face.getPath()));
        }
        faceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userImageIntent = new Intent(getActivity(), UserImageActivity.class);
                getActivity().startActivity(userImageIntent);
            }
        });
        /*setting button configuration*/
        btn_setting = (BootstrapButton)view.findViewById(R.id.user_btn_edit);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment currentFragment = fragmentManager.findFragmentByTag("用户设置");
                ft.addToBackStack(mTitle);
                if (currentFragment == null) {
                    currentFragment = UserSettingFragment.newInstance("用户设置", LauncherActivity.user_name, LauncherActivity.user_sex);
                    ft.add(R.id.container, currentFragment, "用户设置");
                }
                if (currentFragment.isDetached()) {
                    ft.attach(currentFragment);
                }
                ft.show(currentFragment);
                ft.commit();
            }
        });
        /*history button configuration*/
        btn_history = (Button) view.findViewById(R.id.btn_chuxing_history);
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment currentFragment = fragmentManager.findFragmentByTag("出行历史");
                ft.addToBackStack(mTitle);
                if (currentFragment == null) {
                    currentFragment = HistoryFragment.newInstance("出行历史");
                    ft.add(R.id.container, currentFragment, "出行历史");
                }
                if (currentFragment.isDetached()) {
                    ft.attach(currentFragment);
                }
                ft.show(currentFragment);
                ft.commit();
            }
        });
        /*card button configuration*/
        btn_card = (Button) view.findViewById(R.id.btn_jiaotongka);
        btn_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment currentFragment = fragmentManager.findFragmentByTag("我的交通卡");
                ft.addToBackStack(mTitle);
                if (currentFragment == null) {
                    currentFragment = CardFragment.newInstance("我的交通卡");
                    ft.add(R.id.container, currentFragment, "我的交通卡");
                }
                if (currentFragment.isDetached()) {
                    ft.attach(currentFragment);
                }
                ft.show(currentFragment);
                ft.commit();
            }
        });

        /*about button configuration*/
        btn_about = (Button) view.findViewById(R.id.btn_about);
        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        File face = new File(getActivity().getFilesDir(), "faceimage_cropped");
        if (face.exists()) {
            faceImage.setImageBitmap(BitmapFactory.decodeFile(face.getPath()));
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getFilesDir(), "faceimage_cropped"));
        Crop.of(source, destination).asSquare().start(getActivity(),getFragmentManager().findFragmentByTag(mTitle));
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode ==  Activity.RESULT_OK) {
            File face = new File(getActivity().getFilesDir(),"faceimage_cropped");
            faceImage.setImageBitmap(BitmapFactory.decodeFile(face.getPath()));
            ((LauncherActivity) getActivity()).changeUserHeader();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
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

}
