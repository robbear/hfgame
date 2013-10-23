package com.hyperfine.slideshare;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

//
// SlideJSON example (see SlideShareJSON):
//
// { image: "http://foo.com/1.jpg", audio: "http://foo.com/1.3gp" }
//
public class SlideJSON extends JSONObject {
    public final static String TAG = "SlideJSON";

    public SlideJSON() throws JSONException {
        if(D)Log.d(TAG, "SlideJSON.SlideJSON");

        put(SlideShareJSON.KEY_IMAGE, null);
        put(SlideShareJSON.KEY_AUDIO, null);
    }

    public SlideJSON(String json) throws JSONException {
        super(json);

        if(D)Log.d(TAG, String.format("SlideJSON.SlideJSON constructed from string: %s", json));
    }

    public void setImage(String userUuid, String slideShareName, String imageFile) throws JSONException {
        String urlString = Config.baseSlideShareUrl + userUuid + "/" + slideShareName + "/" + imageFile;
        if(D)Log.d(TAG, String.format("SlideJSON.setImage: %s", urlString));
        put(SlideShareJSON.KEY_IMAGE, urlString);
    }

    public void setAudio(String userUuid, String slideShareName, String audioFile) throws JSONException {
        String urlString = Config.baseSlideShareUrl + userUuid + "/" + slideShareName + "/" + audioFile;
        if(D)Log.d(TAG, String.format("SlideJSON.setAudio: %s", urlString));
        put(SlideShareJSON.KEY_AUDIO, urlString);
    }

    public String getImageUrlString() throws JSONException {
        String s = getString(SlideShareJSON.KEY_IMAGE);
        if(D)Log.d(TAG, String.format("SlideJSON.getImageUrlString returns %s", s));
        return s;
    }

    public String getImageFilename() throws JSONException, MalformedURLException {
        String s = getImageUrlString();
        URL url = new URL(s);
        String fileName = url.getFile();
        if(D)Log.d(TAG, String.format("SlideJSON.getImageFilename returns %s", fileName));
        return fileName;
    }

    public String getAudioUrlString() throws JSONException {
        String s = getString(SlideShareJSON.KEY_AUDIO);
        if(D)Log.d(TAG, String.format("SlideJSON.getAudioUrlString returns %s", s));
        return s;
    }

    public String getAudioFilename() throws JSONException, MalformedURLException {
        String s = getAudioUrlString();
        URL url = new URL(s);
        String fileName = url.getFile();
        if(D)Log.d(TAG, String.format("SlideJSON.getAudioFilename returns %s", fileName));
        return fileName;
    }
}
