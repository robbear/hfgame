package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
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

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class PlaySlidesFragment extends Fragment {
    public final static String TAG = "PlaySlidesFragment";

    private final static String INSTANCE_STATE_CURRENTINDEX = "index_state_currentindex";
    private final static String INSTANCE_STATE_IMAGEFILENAME = "index_state_imagefilename";
    private final static String INSTANCE_STATE_AUDIOFILENAME = "index_state_audiofilename";

    private SharedPreferences m_prefs = null;
    private SlideShareJSON m_ssj = null;
    private Activity m_activityParent;
    private String m_slideShareName;
    private ImageSwitcher m_imageSwitcher;
    private Button m_buttonPrev;
    private Button m_buttonNext;
    private int m_slideCount = 0;
    private int m_currentSlideIndex = -1;
    private String m_imageFileName;
    private String m_audioFileName;
    private MediaPlayer m_player;
    private boolean m_isPlaying = false;
    private boolean m_ignoreAudio = false;

    private static PlaySlidesFragment newInstance(String slideShareName) {
        if(D)Log.d(TAG, "PlaySlidesFragment.newInstance");

        PlaySlidesFragment f = new PlaySlidesFragment();

        f.setSlideShareName(slideShareName);

        return f;
    }

    public void setSlideShareName(String name) {
        if(D)Log.d(TAG, String.format("PlaySlidesFragment.setSlideShareName: %s", name));

        m_slideShareName = name;

        //
        // Note: setSlideShareName is called only by the parent activity and is done
        // at the time of onAttachFragment. It's only at this point we can have the
        // parent activity context and load or create the SlideShareJSON file.
        //
        initializeSlideShareJSON();
    }

    private void initializeSlideShareJSON() {
        if(D)Log.d(TAG, "PlaySlidesFragment.initializeSlideShareJSON");

        m_ssj = SlideShareJSON.load(m_activityParent, m_slideShareName, Config.slideShareJSONFilename);
        if (m_ssj == null) {
            if(D)Log.d(TAG, "PlaySlidesFragment.initializeSlideShareJSON - failed to load json file");
            // BUGBUG TODO - feedback?
            return;
        }

        try {
            m_slideCount = m_ssj.getSlideCount();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.initializeSlideShareJSON", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.initializeSlideShareJSON", e);
            e.printStackTrace();
        }

        m_currentSlideIndex = 0;
        setCurrentImageAndAudioFileNames(m_currentSlideIndex);

        if(D)Log.d(TAG, "PlaySlidesFragment.initializeSlideShareJSON: here is the JSON:");
        Utilities.printSlideShareJSON(m_ssj);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "PlaySlidesFragment.onCreate");

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if(D)Log.d(TAG, "PlaySlidesFragment.onCreate - populating from savedInstanceState");

            m_ignoreAudio = true;

            m_currentSlideIndex = savedInstanceState.getInt(INSTANCE_STATE_CURRENTINDEX);
            m_imageFileName = savedInstanceState.getString(INSTANCE_STATE_IMAGEFILENAME);
            m_audioFileName = savedInstanceState.getString(INSTANCE_STATE_AUDIOFILENAME);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "PlaySlidesFragment.onSaveInstanceState");

        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(INSTANCE_STATE_CURRENTINDEX, m_currentSlideIndex);
        savedInstanceState.putString(INSTANCE_STATE_IMAGEFILENAME, m_imageFileName);
        savedInstanceState.putString(INSTANCE_STATE_AUDIOFILENAME, m_audioFileName);
    }

    @Override
    public void onDestroy() {
        if(D)Log.d(TAG, "PlaySlidesFragment.onDestroy");

        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(D)Log.d(TAG, "PlaySlidesFragment.onPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        if(D)Log.d(TAG, "PlaySlidesFragment.onResume");

        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        if(D)Log.d(TAG, "PlaySlidesFragment.onAttach");

        super.onAttach(activity);

        m_activityParent = activity;

        // if (activity instanceof SomeActivityInterface) {
        // }
        // else {
        //     throw new ClassCastException(activity.toString() + " must implement SomeActivityInterface");
    }

    @Override
    public void onDetach() {
        if(D)Log.d(TAG, "PlaySlidesFragment.onDetach");

        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(D)Log.d(TAG, "PlaySlidesFragment.onCreateView");

        View view = inflater.inflate(R.layout.fragment_playslides, container, false);

        m_imageSwitcher = (ImageSwitcher)view.findViewById(R.id.current_image);
        m_imageSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, "PlaySlidesFragment.onImageClicked");

                if (m_audioFileName != null) {
                    startPlaying();
                }
            }
        });

        m_buttonPrev = (Button)view.findViewById(R.id.control_prev);
        m_buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, "PlaySlidesFragment.onPrevButtonClicked");

                if (m_currentSlideIndex <= 0) {
                    // Wrap around
                    m_currentSlideIndex = m_slideCount - 1;
                }
                else {
                    m_currentSlideIndex--;
                }

                setCurrentImageAndAudioFileNames(m_currentSlideIndex);
                renderImageAndPlayAudio();
            }
        });

        m_buttonNext = (Button)view.findViewById(R.id.control_next);
        m_buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(D)Log.d(TAG, "PlaySlidesFragment.onNextButtonClicked");

                if (m_currentSlideIndex >= m_slideCount - 1) {
                    // Wrap around
                    m_currentSlideIndex = 0;
                }
                else {
                    m_currentSlideIndex++;
                }

                setCurrentImageAndAudioFileNames(m_currentSlideIndex);
                renderImageAndPlayAudio();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "PlaySlidesFragment.onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        m_imageSwitcher.setFactory((ViewSwitcher.ViewFactory)m_activityParent);

        renderImageAndPlayAudio();
    }

    private void renderImageAndPlayAudio() {
        if(D)Log.d(TAG, String.format("PlaySlidesFragment.renderImageAndPlayAudio: slide index = %d", m_currentSlideIndex));

        if (m_imageFileName == null) {
            m_imageSwitcher.setImageDrawable(null);
        }
        else {
            Bitmap bitmap = BitmapFactory.decodeFile(Utilities.getAbsoluteFilePath(m_activityParent, m_slideShareName, m_imageFileName));
            Drawable drawableImage = new BitmapDrawable(m_activityParent.getResources(), bitmap);
            m_imageSwitcher.setImageDrawable(drawableImage);
        }

        if (m_audioFileName != null) {
            startPlaying();
        }
    }

    private void startPlaying() {
        if(D)Log.d(TAG, "PlaySlidesFragment.startPlaying");

        if (m_isPlaying) {
            if(D)Log.d(TAG, "PlaySlidesFragment.startPlaying - m_isPlaying is true, so bailing");
            return;
        }

        if (m_ignoreAudio) {
            if(D)Log.d(TAG, "PlaySlidesFragment.startPlaying - m_ignoreAudio is true, so skipping audio playback.");
            m_ignoreAudio = false;
            return;
        }

        m_player = new MediaPlayer();
        m_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(D)Log.d(TAG, "PlaySlidesFragment.onPlaybackCompletion");
                stopPlaying();
            }
        });

        try {
            m_player.setDataSource(Utilities.getAbsoluteFilePath(m_activityParent, m_slideShareName, m_audioFileName));
            m_player.prepare();
            m_player.start();

            m_isPlaying = true;
        }
        catch (IOException e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.startPlaying", e);
            e.printStackTrace();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.startPlaying", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.startPlaying", e);
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if(D)Log.d(TAG, "PlaySlidesFragment.stopPlaying");

        if (!m_isPlaying) {
            if(D)Log.d(TAG, "PlaySlidesFragment.stopPlaying - m_isPlaying is false, so bailing");
            return;
        }

        m_player.release();
        m_player = null;

        m_isPlaying = false;
    }

    private void setCurrentImageAndAudioFileNames(int index) {
        if(D)Log.d(TAG, String.format("PlaySlidesFragment.setCurrentImageAndAudioFileNames(%d)", index));

        try {
            SlideJSON sj = m_ssj.getSlide(index);
            m_imageFileName = sj.getImageFilename();
            m_audioFileName = sj.getAudioFilename();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.getCurrentImageFileName");
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "PlaySlidesFragment.getCurrentImageFileName");
            e.printStackTrace();
        }

        return;
    }
}
