package com.sudalv.subway.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.sudalv.subway.R;

import java.io.File;

public class UserImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_image);
        File face = new File(this.getFilesDir(), "faceimage_cropped");
        ImageView headerView = (ImageView) findViewById(R.id.userimage_image);
        if (face.exists()) {
            headerView.setImageURI(Uri.fromFile(face));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.finish();
        return true;
    }
}
