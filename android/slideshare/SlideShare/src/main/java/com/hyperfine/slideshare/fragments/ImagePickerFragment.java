package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hyperfine.slideshare.R;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class ImagePickerFragment extends Fragment {
    public final static String TAG = "ImagePickerFragment";

    private Activity m_activityParent;
    private Button m_pickButton;

    public static ImagePickerFragment newInstance() {
        if(D)Log.d(TAG, "ImagePickerFragment.newInstance");

        ImagePickerFragment f = new ImagePickerFragment();

        // f.setPropertyX();
        // f.setPropertyY();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(D) Log.d(TAG, "ImagePickerFragment.onCreate");

        super.onCreate(savedInstanceState);

        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            // Set instance state
        }
    }

    @Override
    public void onDestroy() {
        if(D)Log.d(TAG, "ImagePickerFragment.onDestroy");

        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(D)Log.d(TAG, "ImagePickerFragment.onPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        if(D)Log.d(TAG, "ImagePickerFragment.onResume");

        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        if(D)Log.d(TAG, "ImagePickerFragment.onAttach");

        super.onAttach(activity);

        m_activityParent = activity;

        // if (activity instanceof SomeActivityInterface) {
        // }
        // else {
        //     throw new ClassCastException(activity.toString() + " must implement SomeActivityInterface");
    }

    @Override
    public void onDetach() {
        if(D)Log.d(TAG, "ImagePickerFragment.onDetach");

        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(D)Log.d(TAG, "ImagePickerFragment.onCreateView");

        View view = inflater.inflate(R.layout.fragment_imagepicker, container, false);

        m_pickButton = (Button)view.findViewById(R.id.control_imagepicker);
        m_pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, "ImagePickerFragment.onPickButtonClicked");
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "ImagePickerFragment.onActivityCreated");

        super.onActivityCreated(savedInstanceState);
    }
}
