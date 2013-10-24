package com.hyperfine.slideshare;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.hyperfine.slideshare.fragments.PlaySlidesFragment;

import java.io.File;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class PlaySlidesActivity extends Activity implements ViewSwitcher.ViewFactory {
    public final static String TAG = "PlaySlidesActivity";

    private SharedPreferences m_prefs;
    private PlaySlidesFragment m_playSlidesFragment;
    private File m_slideShareDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "PlaySlidesActivity.onCreate");

        super.onCreate(savedInstanceState);
        m_prefs = getSharedPreferences(SSPreferences.PREFS, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_playslides);

        String slideShareName = m_prefs.getString(SSPreferences.PREFS_SSNAME, SSPreferences.DEFAULT_SSNAME);

        // BUGBUG TODO: Replace with dialog to create/fetch SlideShare name
        m_slideShareDirectory = Utilities.createOrGetSlideShareDirectory(this, slideShareName);
        if (m_slideShareDirectory == null) {
            if(D)Log.d(TAG, "PlaySlidesActivity.onCreate - m_slideShareDirectory is null. Bad!!!");
        }

        FragmentManager fm = getFragmentManager();
        m_playSlidesFragment = (PlaySlidesFragment)fm.findFragmentByTag(PlaySlidesFragment.class.toString());
        if (m_playSlidesFragment != null) {
            m_playSlidesFragment.setSlideShareName(slideShareName);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if(D)Log.d(TAG, "PlaySlidesActivity.onAttachFragment");

        if (fragment instanceof PlaySlidesFragment) {
            if(D)Log.d(TAG, "PlaySlidesActivity.onAttachFragment - found our PlaySlidesFragment");
            String slideShareName = m_prefs.getString(SSPreferences.PREFS_SSNAME, SSPreferences.DEFAULT_SSNAME);
            m_playSlidesFragment = (PlaySlidesFragment)fragment;
            m_playSlidesFragment.setSlideShareName(slideShareName);
        }
    }

    @Override
    public View makeView() {
        if(D)Log.d(TAG, "PlaySlidesActivity.makeView");

        ImageView view = new ImageView(this);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }
}
