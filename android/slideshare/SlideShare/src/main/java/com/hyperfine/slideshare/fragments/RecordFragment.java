package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.hyperfine.slideshare.R;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class RecordFragment extends Fragment {
    public final static String TAG = "RecordFragment";

    private boolean m_isRecording = false;

    private ImageButton m_recordButton;

    public static RecordFragment newInstance() {
        if(D)Log.d(TAG, "RecordFragment.newInstance");

        RecordFragment f = new RecordFragment();

        // f.setPropertyX();
        // f.setPropertyY();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(D) Log.d(TAG, "RecordFragment.onCreate");

        super.onCreate(savedInstanceState);

        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            // Set instance state
        }
    }

    @Override
    public void onDestroy() {
        if(D)Log.d(TAG, "RecordFragment.onDestroy");

        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(D)Log.d(TAG, "RecordFragment.onPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        if(D)Log.d(TAG, "RecordFragment.onResume");

        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        if(D)Log.d(TAG, "RecordFragment.onAttach");

        super.onAttach(activity);

        // if (activity instanceof SomeActivityInterface) {
        // }
        // else {
        //     throw new ClassCastException(activity.toString() + " must implement SomeActivityInterface");
    }

    @Override
    public void onDetach() {
        if(D)Log.d(TAG, "RecordFragment.onDetach");

        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(D)Log.d(TAG, "RecordFragment.onCreateView");

        View view = inflater.inflate(R.layout.fragment_record, container, false);

        m_recordButton = (ImageButton)view.findViewById(R.id.control_record);
        m_recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, "RecordFragment.onRecordButtonClicked");
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "RecordFragment.onActivityCreated");

        super.onActivityCreated(savedInstanceState);
    }
}
