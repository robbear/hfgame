package com.hyperfine.slideshare;

import android.app.Application;

public class SlideShowApplication extends Application {
    public final static String TAG = "SlideShowApplication";

    private String m_currentSlideShowName = "Test Slides";

    public String getCurrentSlideShowName() {
        return m_currentSlideShowName;
    }

    public void setCurrentSlideShowName(String name) {
        m_currentSlideShowName = name;
    }
}
