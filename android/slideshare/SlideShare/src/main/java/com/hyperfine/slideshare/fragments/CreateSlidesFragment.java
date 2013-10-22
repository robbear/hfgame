package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ViewSwitcher;

import com.hyperfine.slideshare.R;
import com.hyperfine.slideshare.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class CreateSlidesFragment extends Fragment {
    public final static String TAG = "CreateSlidesFragment";

    public final static int REQUEST_IMAGE = 1;

    private boolean m_isRecording = false;
    private boolean m_isPlaying = false;
    private MediaRecorder m_recorder;
    private MediaPlayer m_player;
    private Activity m_activityParent;
    private String m_slideShareName;
    private String m_imageFileName = getNewImageFileName();
    private String m_audioFileName = getNewAudioFileName();
    private Button m_buttonSelectImage;
    private ImageSwitcher m_imageSwitcherSelected;
    private Button m_buttonRecord;
    private Button m_buttonPlayStop;
    private Button m_buttonPrev;
    private Button m_buttonSave;
    private Button m_buttonNext;
    private boolean m_hasAudio = false;
    private boolean m_hasImage = false;

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

        if (isDirty()) {
            Utilities.deleteFile(m_activityParent, m_slideShareName, m_imageFileName);
            Utilities.deleteFile(m_activityParent, m_slideShareName, m_audioFileName);
        }
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

        m_buttonSelectImage = (Button)view.findViewById(R.id.control_selectimage);
        m_buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, "CreateSlidesFragment.onSelectImageButtonClicked");

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        m_imageSwitcherSelected = (ImageSwitcher)view.findViewById(R.id.selected_image);

        m_buttonRecord = (Button)view.findViewById(R.id.control_record);
        m_buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, String.format("CreateSlidesFragment.onRecordButtonClicked: %s recording", m_isRecording ? "Stopping" : "Starting"));

                if (m_isRecording) {
                    stopRecording();
                }
                else {
                    startRecording();
                }
            }
        });

        m_buttonPlayStop = (Button)view.findViewById(R.id.control_playback);
        m_buttonPlayStop.setEnabled(m_hasAudio);
        m_buttonPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, String.format("CreateSlidesFragment.onPlayStopButtonClicked: %s playing", m_isPlaying ? "Stopping" : "Starting"));

                if (m_isPlaying) {
                    stopPlaying();
                }
                else {
                    startPlaying();
                }
            }
        });

        m_buttonPrev = (Button)view.findViewById(R.id.control_prev);

        m_buttonSave = (Button)view.findViewById(R.id.control_saveslide);

        m_buttonNext = (Button)view.findViewById(R.id.control_next);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        m_imageSwitcherSelected.setFactory((ViewSwitcher.ViewFactory)m_activityParent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.onActivityResult: requestCode=%d, resultCode=%d", requestCode, resultCode));

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                if(D)Log.d(TAG, String.format("CreateSlidesFragment.onActivityResult: intent data = %s", intent.getData().toString()));

                boolean success = Utilities.copyGalleryImageToJPG(m_activityParent, m_slideShareName, m_imageFileName, intent);

                if (success) {
                    // Display the image only upon successful save

                    m_hasImage = true;

                    InputStream stream = m_activityParent.getContentResolver().openInputStream(intent.getData());
                    Drawable drawableImage = new BitmapDrawable(m_activityParent.getResources(), stream);
                    m_imageSwitcherSelected.setImageDrawable(drawableImage);
                }
            }
            catch (IOException e) {
                if(E)Log.e(TAG, "CreateSlidesFragment.onActivityResult", e);
                e.printStackTrace();
            }
            catch (Exception e) {
                if(E)Log.e(TAG, "CreateSlidesFragment.onActivityResult", e);
                e.printStackTrace();
            }
            catch (OutOfMemoryError e) {
                if(E)Log.e(TAG, "CreateSlidesFragment.onActivityResult", e);
                e.printStackTrace();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void startRecording() {
        if(D)Log.d(TAG, "CreateSlidesFragment.startRecording");

        if (m_isRecording) {
            if(D)Log.d(TAG, "CreateSlidesFragment.startRecording - m_isRecording is true, so bailing");
            return;
        }

        m_recorder = new MediaRecorder();
        m_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        m_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        m_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        m_recorder.setOutputFile(Utilities.getAbsoluteFilePath(m_activityParent, m_slideShareName, m_audioFileName));

        try {
            m_recorder.prepare();
            m_recorder.start();
            m_isRecording = true;
            m_buttonRecord.setText("Stop recording");
        }
        catch (IOException e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.startRecording", e);
            e.printStackTrace();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.startRecording", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.startRecording", e);
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if(D)Log.d(TAG, "CreateSlidesFragment.stopRecording");

        if (!m_isRecording) {
            if(D)Log.d(TAG, "CreateSlidesFragment.stopRecording - m_isRecording is false so bailing");
            return;
        }

        m_recorder.stop();
        m_recorder.release();
        m_recorder = null;

        m_isRecording = false;
        m_buttonRecord.setText("Record");

        m_hasAudio = true;
        m_buttonPlayStop.setEnabled(true);
    }

    private void startPlaying() {
        if(D)Log.d(TAG, "CreateSlidesFragment.startPlaying");

        if (m_isPlaying) {
            if(D)Log.d(TAG, "CreateSlidesFragment.startPlaying - m_isPlaying is true, so bailing");
            return;
        }

        m_player = new MediaPlayer();
        m_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(D)Log.d(TAG, "CreateSlidesFragment.onPlaybackCompletion");
                stopPlaying();
            }
        });

        try {
            m_player.setDataSource(Utilities.getAbsoluteFilePath(m_activityParent, m_slideShareName, m_audioFileName));
            m_player.prepare();
            m_player.start();

            m_isPlaying = true;
            m_buttonPlayStop.setText("Stop playing");
        }
        catch (IOException e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.startPlaying", e);
            e.printStackTrace();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.startPlaying", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.startPlaying", e);
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if(D)Log.d(TAG, "CreateSlidesFragment.stopPlaying");

        if (!m_isPlaying) {
            if(D)Log.d(TAG, "CreateSlidesFragment.stopPlaying - m_isPlaying is false, so bailing");
            return;
        }

        m_player.release();
        m_player = null;

        m_isPlaying = false;
        m_buttonPlayStop.setText("Play");
    }

    private boolean isDirty() {
        return m_hasAudio || m_hasImage;
    }

    private static String getNewImageFileName() {
        return UUID.randomUUID().toString() + ".jpg";
    }

    private static String getNewAudioFileName() {
        return UUID.randomUUID().toString() + ".3gp";
    }
}
