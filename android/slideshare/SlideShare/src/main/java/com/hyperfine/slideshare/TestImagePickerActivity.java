package com.hyperfine.slideshare;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class TestImagePickerActivity extends Activity implements ViewSwitcher.ViewFactory {

    public final static String TAG = "TestImagePickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "TestImagePickerActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimagepicker);
    }

    @Override
    public View makeView() {
        if(D)Log.d(TAG, "TestImagePickerActivity.makeView");

        ImageView view = new ImageView(this);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }
}
