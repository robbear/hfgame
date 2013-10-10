package com.hyperfine.slideshare;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class TestImagePickerActivity extends Activity {

    public final static String TAG = "TestImagePickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "TestImagePickerActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimagepicker);
    }
}
