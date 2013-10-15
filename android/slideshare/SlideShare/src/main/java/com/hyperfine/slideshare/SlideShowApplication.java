package com.hyperfine.slideshare;

import android.app.Application;

import org.json.JSONException;

public class SlideShowApplication extends Application {
    public final static String TAG = "SlideShowApplication";

    private String m_currentSlideShowName = "Test Slides";
    private SlideShowJSON m_ssj = null;

    public String getCurrentSlideShowName() {
        return m_currentSlideShowName;
    }

    public void setCurrentSlideShowName(String name) {
        m_currentSlideShowName = name;
    }

    public SlideShowJSON getCurrentSlideShowJSON() throws JSONException {
        if (m_ssj == null) {
            m_ssj = new SlideShowJSON();
        }

        return m_ssj;
    }

    public void setCurrentSlideShowJSON(SlideShowJSON ssj) {
        m_ssj = ssj;
    }
}
