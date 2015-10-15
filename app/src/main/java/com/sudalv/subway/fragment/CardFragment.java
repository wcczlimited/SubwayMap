package com.sudalv.subway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.api.view.BootstrapTextView;
import com.sudalv.subway.R;
import com.sudalv.subway.activity.LauncherActivity;
import com.sudalv.subway.util.FileUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";
    private static String mCardText1 = null;
    private static String mCardText2 = null;
    private String mTitle;
    private View view;
    private OnFragmentInteractionListener mListener;

    /*UI*/
    private BootstrapTextView card1, card2;
    private BootstrapButton btn_delete1, btn_delete2, btn_add, btn_confirm;
    private FrameLayout card1Layout, card2Layout;

    public CardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title
     * @return A new instance of fragment CardFragment.
     */
    public static CardFragment newInstance(String title) {
        CardFragment fragment = new CardFragment();
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
        view = inflater.inflate(R.layout.fragment_card, container, false);
        card1 = (BootstrapTextView) view.findViewById(R.id.card_card1_text);
        card2 = (BootstrapTextView) view.findViewById(R.id.card_card2_text);
        btn_delete1 = (BootstrapButton) view.findViewById(R.id.card_card1_btn_delete);
        btn_delete2 = (BootstrapButton) view.findViewById(R.id.card_card2_btn_delete);
        btn_add = (BootstrapButton) view.findViewById(R.id.card_add);
        btn_confirm = (BootstrapButton) view.findViewById(R.id.card_confirm);
        card1Layout = (FrameLayout) view.findViewById(R.id.card_layout1);
        card2Layout = (FrameLayout) view.findViewById(R.id.card_layout2);
        String raw_card_text = FileUtils.readFromFile(getActivity().getFilesDir(), "card");
        String[] arr = raw_card_text.split(",");
        System.out.println(raw_card_text);
        System.out.println(arr);
        if (arr.length == 0 || raw_card_text.equals("")) {
            card1Layout.setVisibility(View.INVISIBLE);
            LinearLayout div = (LinearLayout) view.findViewById(R.id.card_div);
            div.setVisibility(View.INVISIBLE);
            card2Layout.setVisibility(View.INVISIBLE);
        } else if (arr.length == 1) {
            LinearLayout div = (LinearLayout) view.findViewById(R.id.card_div);
            div.setVisibility(View.INVISIBLE);
            card2Layout.setVisibility(View.INVISIBLE);
            mCardText1 = arr[0];
            card1.setMarkdownText(mCardText1);
        } else if (arr.length == 2) {
            card1.setMarkdownText(arr[0]);
            card2.setMarkdownText(arr[1]);
        }
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCardText1 != null && mCardText2 != null) {
                    Toast.makeText(getActivity(), "您已经有两张交通卡", Toast.LENGTH_LONG).show();
                    return;
                }
                showPopwindow();
            }
        });
        btn_delete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCardText2 != null) {
                    mCardText1 = mCardText2;
                    mCardText2 = null;
                    LinearLayout div = (LinearLayout) view.findViewById(R.id.card_div);
                    div.setVisibility(View.INVISIBLE);
                    card2Layout.setVisibility(View.INVISIBLE);
                } else if (mCardText2 == null) {
                    mCardText1 = null;
                    card1Layout.setVisibility(View.INVISIBLE);
                }
                refreshUI();
            }
        });
        btn_delete2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardText2 = null;
                refreshUI();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void showPopwindow() {
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.addcard_popup, null);

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

        final BootstrapEditText editText = (BootstrapEditText) view.findViewById(R.id.add_card_edittext);
        BootstrapButton confirm = (BootstrapButton) view.findViewById(R.id.addcard_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CardFragment.mCardText1 == null)
                    CardFragment.mCardText1 = editText.getText().toString();
                else if (CardFragment.mCardText1 != null && CardFragment.mCardText2 == null)
                    CardFragment.mCardText2 = editText.getText().toString();
                Toast.makeText(getActivity(), "增加交通卡" + editText.getText().toString(), Toast.LENGTH_SHORT).show();
                refreshUI();
                window.dismiss();
            }
        });

        BootstrapButton cancel = (BootstrapButton) view.findViewById(R.id.addcard_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });

    }

    private void refreshUI() {
        String str = "";
        if (mCardText1 != null && mCardText2 == null) {
            str = mCardText1;
        } else if (mCardText1 != null && mCardText2 != null) {
            str = mCardText1 + "," + mCardText2;
        }
        FileUtils.outToFile(getActivity().getFilesDir(), "card", str);
        if (CardFragment.mCardText1 != null && CardFragment.mCardText2 == null) {
            card1Layout.setVisibility(View.VISIBLE);
            card1.setMarkdownText(CardFragment.mCardText1);
            LinearLayout div = (LinearLayout) view.findViewById(R.id.card_div);
            div.setVisibility(View.INVISIBLE);
            card2Layout.setVisibility(View.INVISIBLE);
            return;
        }
        if (CardFragment.mCardText1 != null && CardFragment.mCardText2 != null) {
            card1Layout.setVisibility(View.VISIBLE);
            card1.setMarkdownText(CardFragment.mCardText1);
            LinearLayout div = (LinearLayout) view.findViewById(R.id.card_div);
            div.setVisibility(View.VISIBLE);
            card2Layout.setVisibility(View.VISIBLE);
            card2.setMarkdownText(CardFragment.mCardText2);
            return;
        }
        if (CardFragment.mCardText1 == null) {
            card1Layout.setVisibility(View.INVISIBLE);
            LinearLayout div = (LinearLayout) view.findViewById(R.id.card_div);
            div.setVisibility(View.INVISIBLE);
            card2Layout.setVisibility(View.INVISIBLE);
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
