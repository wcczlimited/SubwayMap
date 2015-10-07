package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.soundcloud.android.crop.Crop;
import com.sudalv.subway.LauncherActivity;
import com.sudalv.subway.R;

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
    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;
    private static final int SELECT_PIC_KITKAT = 3;
    /*头像名称*/
    private static final String IMAGE_FILE_NAME = "faceImage.jpg";
    /*UI*/
    private BootstrapCircleThumbnail faceImage;
    private BootstrapButton btn_setting;

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

    public UserFragment() {
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
        view = inflater.inflate(R.layout.fragment_user, container, false);
        File face = new File(getActivity().getFilesDir(),"faceimage_cropped");
        faceImage = (BootstrapCircleThumbnail)view.findViewById(R.id.user_face_image);
        if(face.exists()){
            faceImage.setImage(BitmapFactory.decodeFile(face.getPath()));
        }
        faceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crop.pickImage(getActivity(), getFragmentManager().findFragmentByTag(mTitle));
            }
        });
        btn_setting = (BootstrapButton)view.findViewById(R.id.user_btn_edit);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment currentFragment = fragmentManager.findFragmentByTag("用户设置");
                ft.addToBackStack(mTitle);
                if(currentFragment == null) {
                    currentFragment = UserSettingFragment.newInstance("用户设置",LauncherActivity.user_name,LauncherActivity.user_sex);
                    ft.add(R.id.container, currentFragment, "用户设置");
                }
                if(currentFragment.isDetached()){
                    ft.attach(currentFragment);
                }
                ft.show(currentFragment);
                ft.commit();
            }
        });
        return view;
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
            faceImage.setImage(BitmapFactory.decodeFile(face.getPath()));
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
