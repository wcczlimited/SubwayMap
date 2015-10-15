package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.soundcloud.android.crop.Crop;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.util.FileUtils;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserSettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserSettingFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER_NAME = "username";
    private static final String ARG_USER_SEX = "usersex";
    private static final String ARG_TITLE = "title";

    private String mUsername;
    private int mUserSex;
    private String mTitle;

    /*UI*/
    private BootstrapEditText usernameText;
    private BootstrapButton btn_male, btn_female, btn_confirm;
    private BootstrapCircleThumbnail settingfaceImage;

    private OnFragmentInteractionListener mListener;
    private View view;

    public UserSettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username User's nickname
     * @param usersex User's sex
     * @return A new instance of fragment UserSettingFragment.
     */
    public static UserSettingFragment newInstance(String title, String username, int usersex) {
        UserSettingFragment fragment = new UserSettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_USER_NAME, username);
        args.putInt(ARG_USER_SEX, usersex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsername = getArguments().getString(ARG_USER_NAME);
            mUserSex = getArguments().getInt(ARG_USER_SEX);
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_setting, container, false);
        //set User face Image
        File face = new File(getActivity().getFilesDir(), "faceimage_cropped");
        settingfaceImage = (BootstrapCircleThumbnail) view.findViewById(R.id.user_setting_face_image);
        if (face.exists()) {
            settingfaceImage.setImageBitmap(BitmapFactory.decodeFile(face.getPath()));
        }
        settingfaceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crop.pickImage(getActivity(), getFragmentManager().findFragmentByTag(mTitle));
            }
        });
        usernameText = (BootstrapEditText)view.findViewById(R.id.username);
        usernameText.setText(mUsername);
        btn_female = (BootstrapButton)view.findViewById(R.id.user_btn_female);
        btn_male = (BootstrapButton)view.findViewById(R.id.user_btn_male);
        btn_confirm = (BootstrapButton)view.findViewById(R.id.user_btn_confirm);
        if(mUserSex==0){
            btn_female.setEnabled(false);
        }else{
            btn_male.setEnabled(false);
        }
        btn_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_female.setEnabled(false);
                btn_male.setEnabled(true);
                mUserSex = 0;
            }
        });
        btn_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_male.setEnabled(false);
                btn_female.setEnabled(true);
                mUserSex = 1;
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "请输入用户名", Toast.LENGTH_LONG).show();
                    return;
                }
                LauncherActivity.user_name = usernameText.getText().toString();
                LauncherActivity.user_sex = mUserSex;
                FileUtils.outToFile(getActivity().getFilesDir(), "username", LauncherActivity.user_name);
                FileUtils.outToFile(getActivity().getFilesDir(), "usersex", LauncherActivity.user_sex + "");
                ((LauncherActivity) getActivity()).changeUserHeader();
                getFragmentManager().popBackStack();
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
        Crop.of(source, destination).asSquare().start(getActivity(), getFragmentManager().findFragmentByTag(mTitle));
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            File face = new File(getActivity().getFilesDir(), "faceimage_cropped");
            settingfaceImage.setImageBitmap(BitmapFactory.decodeFile(face.getPath()));
            ((LauncherActivity) getActivity()).changeUserHeader();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
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

}
