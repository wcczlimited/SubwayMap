package com.sudalv.subway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class BeforeLaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_launch);
        final Intent mainIntent = new Intent(BeforeLaunchActivity.this, LauncherActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BeforeLaunchActivity.this.startActivity(mainIntent);
                BeforeLaunchActivity.this.finish();
            }
        }, 3000);
    }
}
