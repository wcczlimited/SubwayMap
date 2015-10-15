package com.sudalv.subway.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.sudalv.subway.R;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.finish();
        return true;
    }
}
