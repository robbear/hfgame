package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyperfine.slideshare.R;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class CreateSlidesFragment extends Fragment {
    public final static String TAG = "CreateSlidesFragment";

    private boolean m_isRecording = false;
    private boolean m_isPlaying = false;
    private MediaRecorder m_recorder;
    private MediaPlayer m_player;
    private Activity m_activityParent;
    private String m_slideShareName;

    private static CreateSlidesFragment newInstance(String slideShareName) {
        if(D)Log.d(TAG, "CreateSlidesFragment.newInstance");

        CreateSlidesFragment f = new CreateSlidesFragment();

        f.setSlideShareName(slideShareName);

        return f;
    }

    public void setSlideShareName(String name) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.setSlideShareName: %s", name));

        m_slideShareName = name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onCreate");

        super.onCreate(savedInstanceState);

        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            // Set instance state
        }
    }

    @Override
    public void onDestroy() {
        if(D)Log.d(TAG, "CreateSlidesFragment.onDestroy");

        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(D)Log.d(TAG, "CreateSlidesFragment.onPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        if(D)Log.d(TAG, "CreateSlidesFragment.onResume");

        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onAttach");

        super.onAttach(activity);

        m_activityParent = activity;

        // if (activity instanceof SomeActivityInterface) {
        // }
        // else {
        //     throw new ClassCastException(activity.toString() + " must implement SomeActivityInterface");
    }

    @Override
    public void onDetach() {
        if(D)Log.d(TAG, "CreateSlidesFragment.onDetach");

        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onCreateView");

        View view = inflater.inflate(R.layout.fragment_createslides, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onActivityCreated");

        super.onActivityCreated(savedInstanceState);
    }
}
