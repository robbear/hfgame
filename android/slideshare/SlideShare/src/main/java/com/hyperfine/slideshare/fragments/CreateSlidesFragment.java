package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.hyperfine.slideshare.Config;
import com.hyperfine.slideshare.R;
import com.hyperfine.slideshare.SlideJSON;
import com.hyperfine.slideshare.SlideShareJSON;
import com.hyperfine.slideshare.Utilities;

import java.io.IOException;
import java.util.UUID;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class CreateSlidesFragment extends Fragment {
    public final static String TAG = "CreateSlidesFragment";

    private final static int REQUEST_IMAGE = 1;
    private final static String INSTANCE_STATE_IMAGEFILE = "instance_state_imagefile";
    private final static String INSTANCE_STATE_AUDIOFILE = "instance_state_audiofile";
    private final static String INSTANCE_STATE_SLIDEUUID = "instance_state_slideuuid";

    private SlideShareJSON m_ssj = null;
    private boolean m_isRecording = false;
    private boolean m_isPlaying = false;
    private MediaRecorder m_recorder;
    private MediaPlayer m_player;
    private Activity m_activityParent;
    private String m_slideShareName;
    private String m_slideUuid = null;
    private String m_imageFileName = null;
    private String m_audioFileName = null;
    private Button m_buttonSelectImage;
    private ImageSwitcher m_imageSwitcherSelected;
    private Button m_buttonRecord;
    private Button m_buttonPlayStop;
    private Button m_buttonPrev;
    private Button m_buttonSave;
    private Button m_buttonNext;

    private static CreateSlidesFragment newInstance(String slideShareName) {
        if(D)Log.d(TAG, "CreateSlidesFragment.newInstance");

        CreateSlidesFragment f = new CreateSlidesFragment();

        f.setSlideShareName(slideShareName);

        return f;
    }

    public void setSlideShareName(String name) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.setSlideShareName: %s", name));

        m_slideShareName = name;

        //
        // Note: setSlideShareName is called only by the parent activity and is done
        // at the time of onAttachFragment. It's only at this point we can have the
        // parent activity context and load or create the SlideShareJSON file.
        initializeSlideShareJSON();
    }

    public void setImageFileName(String fileName) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.setImageFileName: %s", fileName));

        m_imageFileName = fileName;
    }

    public void setAudioFileName(String fileName) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.setAudioFileName: %s", fileName));

        m_audioFileName = fileName;
    }

    public void setSlideUuid(String s) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.setSlideUuid: %s", s));

        m_slideUuid = s;
    }

    private void initializeSlideShareJSON() {
        if(D)Log.d(TAG, "CreateSlidesFragment.initializeSlideShareJSON");

        m_ssj = SlideShareJSON.load(m_activityParent, m_slideShareName, Config.slideShareJSONFilename);
        if (m_ssj == null) {
            try {
                m_ssj = new SlideShareJSON();
                m_ssj.save(m_activityParent, m_slideShareName, Config.slideShareJSONFilename);
            }
            catch (Exception e) {
                if(E)Log.e(TAG, "CreateSlidesFragment.initializeSlideShareJSON (FATAL)", e);
                e.printStackTrace();
            }
            catch (OutOfMemoryError e) {
                if(E)Log.e(TAG, "CreateSlidesFragment.initializeSlideShareJSON (FATAL)", e);
                e.printStackTrace();
            }
        }

        if(D)Log.d(TAG, "CreateSlidesFragment.initializeSlideShareJSON: here is the current JSON:");
        Utilities.printSlideShareJSON(m_ssj);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onCreate");

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if(D)Log.d(TAG, "CreateSlidesFragment.onCreate - populating from savedInstanceState");

            setImageFileName(savedInstanceState.getString(INSTANCE_STATE_IMAGEFILE));
            setAudioFileName(savedInstanceState.getString(INSTANCE_STATE_AUDIOFILE));
            setSlideUuid(savedInstanceState.getString(INSTANCE_STATE_SLIDEUUID));
        }

        if (m_slideUuid == null) {
            m_slideUuid = UUID.randomUUID().toString();
        }

        if(D)Log.d(TAG, String.format("CreateSlidesFragment.onCreate - m_slideUuid=%s", m_slideUuid));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "CreateSlidesFragment.onSaveInstanceState");

        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(INSTANCE_STATE_IMAGEFILE, m_imageFileName);
        savedInstanceState.putString(INSTANCE_STATE_AUDIOFILE, m_audioFileName);
        savedInstanceState.putString(INSTANCE_STATE_SLIDEUUID, m_slideUuid);
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

        m_buttonSelectImage = (Button)view.findViewById(R.id.control_selectimage);
        m_buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "CreateSlidesFragment.onSelectImageButtonClicked");

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
        m_buttonPlayStop.setEnabled(hasAudio());
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

        fillImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(D)Log.d(TAG, String.format("CreateSlidesFragment.onActivityResult: requestCode=%d, resultCode=%d", requestCode, resultCode));

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            if(D)Log.d(TAG, String.format("CreateSlidesFragment.onActivityResult: intent data = %s", intent.getData().toString()));

            String imageFileName = m_imageFileName;
            if (imageFileName == null) {
                imageFileName = getNewImageFileName();
            }

            boolean success = Utilities.copyGalleryImageToJPG(m_activityParent, m_slideShareName, imageFileName, intent);

            if (success) {
                // Display the image only upon successful save
                m_imageFileName = imageFileName;
                fillImage();
            }
            else {
                // Clean up - remove the image file
                Utilities.deleteFile(m_activityParent, m_slideShareName, imageFileName);
                m_imageFileName = null;
            }

            updateSlideShareJSON();
        }
        else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void updateSlideShareJSON() {
        if(D)Log.d(TAG, "CreateSlidesFragment.updateSlideShareJSON");
        if(D)Log.d(TAG, "Current JSON:");
        Utilities.printSlideShareJSON(m_ssj);

        try {
            m_ssj.upsertSlide(m_slideUuid, m_imageFileName, m_audioFileName);
            m_ssj.save(m_activityParent, m_slideShareName, Config.slideShareJSONFilename);
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.updateSlideShareJSON", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "CreateSlidesFragment.updateSlideShareJSON", e);
            e.printStackTrace();
        }

        if(D)Log.d(TAG, "After update:");
        Utilities.printSlideShareJSON(m_ssj);
    }

    private void fillImage() {
        if(D)Log.d(TAG, "CreateSlidesFragment.fillImage");

        if (m_imageFileName == null) {
            if(D)Log.d(TAG, "CreateSlidesFragment.fillImage - m_imageFileName is null. Bailing.");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(Utilities.getAbsoluteFilePath(m_activityParent, m_slideShareName, m_imageFileName));
        Drawable drawableImage = new BitmapDrawable(m_activityParent.getResources(), bitmap);
        m_imageSwitcherSelected.setImageDrawable(drawableImage);
    }

    private void startRecording() {
        if(D)Log.d(TAG, "CreateSlidesFragment.startRecording");

        if (m_isRecording) {
            if(D)Log.d(TAG, "CreateSlidesFragment.startRecording - m_isRecording is true, so bailing");
            return;
        }

        if (m_audioFileName == null) {
            m_audioFileName = getNewAudioFileName();
            updateSlideShareJSON();
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
        return hasAudio() || hasImage();
    }

    private boolean hasAudio() {
        return m_audioFileName != null;
    }

    private boolean hasImage() {
        return m_imageFileName != null;
    }

    private static String getNewImageFileName() {
        return UUID.randomUUID().toString() + ".jpg";
    }

    private static String getNewAudioFileName() {
        return UUID.randomUUID().toString() + ".3gp";
    }
}
