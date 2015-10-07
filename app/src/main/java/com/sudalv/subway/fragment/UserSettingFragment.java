package com.sudalv.subway.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.sudalv.subway.LauncherActivity;
import com.sudalv.subway.R;

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
    private BootstrapButton btn_male, btn_female;

    private OnFragmentInteractionListener mListener;
    private View view;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username User's nickname
     * @param usersex User's sex
     * @return A new instance of fragment UserSettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserSettingFragment newInstance(String title, String username, int usersex) {
        UserSettingFragment fragment = new UserSettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_USER_NAME, username);
        args.putInt(ARG_USER_SEX, usersex);
        fragment.setArguments(args);
        return fragment;
    }

    public UserSettingFragment() {
        // Required empty public constructor
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
        usernameText = (BootstrapEditText)view.findViewById(R.id.username);
        btn_female = (BootstrapButton)view.findViewById(R.id.user_btn_female);
        btn_male = (BootstrapButton)view.findViewById(R.id.user_btn_male);
        btn_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_female.setEnabled(false);
                btn_male.setEnabled(true);
            }
        });
        btn_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_male.setEnabled(false);
                btn_female.setEnabled(true);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
