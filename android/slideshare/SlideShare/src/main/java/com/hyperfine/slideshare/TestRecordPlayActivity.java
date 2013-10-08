package com.hyperfine.slideshare;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class TestRecordPlayActivity extends Activity {

    public final static String TAG = "TestRecordPlayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "TestRecordPlayActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testrecordplay);
    }
}
