package com.hyperfine.slideshare;

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

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class TestImagePickerActivity extends Activity implements ViewSwitcher.ViewFactory {

    public final static String TAG = "TestImagePickerActivity";

    private SharedPreferences m_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "TestImagePickerActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimagepicker);

        m_prefs = getSharedPreferences(SSPreferences.PREFS, Context.MODE_PRIVATE);

        //
        // BUGBUG
        //
        runTests();
    }

    @Override
    public View makeView() {
        if(D)Log.d(TAG, "TestImagePickerActivity.makeView");

        ImageView view = new ImageView(this);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    private void runTests() {
        if(D)Log.d(TAG, "TestImagePickerActivity.runTests");

        File rootDir = getFilesDir();
        String slideShowName = m_prefs.getString(SSPreferences.PREFS_SSNAME, SSPreferences.DEFAULT_SSNAME);

        File slideShowDirectory = new File(rootDir.getAbsolutePath() + "/" + slideShowName);
        slideShowDirectory.mkdir();
        if(D)Log.d(TAG, String.format("TestImagePickerActivity.runTests - slideShowDirectory=%s", slideShowDirectory));

        //
        // BUGBUG - TEST to list all files and directories
        //
        listAllFilesAndDirectories(rootDir);

        //
        // BUGBUG - TEST to generate or load user UUID from preferences
        //
        UUID userUuid;
        String userUuidString = m_prefs.getString(SSPreferences.PREFS_USERUUID, null);
        if (userUuidString == null) {
            UUID uuid = UUID.randomUUID();
            if(D)Log.d(TAG, String.format("TestImagePickerActivity.onAttach - generated uuid=%s", uuid.toString()));
            userUuid = uuid;
        }
        else {
            userUuid = UUID.fromString(userUuidString);
        }
        if(D)Log.d(TAG, String.format("TestImagePickerActivity.onAttach - userUuid=%s", userUuid.toString()));
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putString(SSPreferences.PREFS_USERUUID, userUuid.toString());
        editor.commit();

        //
        // BUGBUG - TEST for SlideShowJSON methods
        //
        SlideShowJSON ssj;
        try {
            ssj = new SlideShowJSON();
            if(D)Log.d(TAG, String.format("Default SlideShowJSON: %s", ssj.toString()));
            String urlBase = Config.baseSlideShareUrl + userUuid.toString() + "/" + slideShowName + "/";

            String lastSlideUuid = "";
            for (int i = 0; i < 5; i++) {
                lastSlideUuid = UUID.randomUUID().toString();
                ssj.upsertSlide(lastSlideUuid, String.format("%s%d.jpg", urlBase, i), String.format("%s%d.3gp", urlBase, i));
            }
            if(D)Log.d(TAG, String.format("SlideShowJSON after upsert (add) %d slides: %s", ssj.getSlides().length(), ssj.toString()));
            ssj.upsertSlide(lastSlideUuid, String.format("%slastslide.jpg", urlBase), String.format("%slastslide.3gp", urlBase));
            if(D)Log.d(TAG, String.format("SlideShowJSON after upsert (update) %d slides: %s", ssj.getSlides().length(), ssj.toString()));
            JSONObject slide = ssj.getSlide(lastSlideUuid);
            if(D)Log.d(TAG, String.format("SlideShowJSON getSlide(%s) returns %s", lastSlideUuid, slide.toString()));
            ssj.removeSlide(lastSlideUuid);
            if(D)Log.d(TAG, String.format("SlideShowJSON after remove. %d slides: %s", ssj.getSlides().length(), ssj.toString()));

            // Save and load tests
            boolean retVal = ssj.save(this, slideShowName, "test.json");
            if(D)Log.d(TAG, String.format("Saved SlideShowJSON with retVal=%b", retVal));
            listAllFilesAndDirectories(rootDir);
            ssj = SlideShowJSON.load(this, slideShowName, "test.json");
            if(D)Log.d(TAG, String.format("SlideShowJSON after load: %s", ssj == null ? "null" : ssj.toString()));
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "TestImagePickerActivity.onAttach", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "TestImagePickerActivity.onAttach", e);
            e.printStackTrace();
        }
    }

    private void listAllFilesAndDirectories(File dir) {
        if(D)Log.d(TAG, String.format("TestImagePickerActivity.listAllFilesAndDirectories for %s", dir == null ? "null" : dir));

        ArrayList<File> directories = new ArrayList<File>();

        if (dir == null) {
            dir = getFilesDir();
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String file = files[i].getAbsolutePath();
                if(D)Log.d(TAG, String.format("TestImagePickerActivity.listAllFilesAndDirectories - file: %s, isDirectory=%b, size=%d", file, files[i].isDirectory(), files[i].length()));

                if (files[i].isDirectory()) {
                    directories.add(files[i]);
                }
            }
        }

        for (int i = 0; i < directories.size(); i++) {
            listAllFilesAndDirectories(directories.get(i));
        }
    }
}
