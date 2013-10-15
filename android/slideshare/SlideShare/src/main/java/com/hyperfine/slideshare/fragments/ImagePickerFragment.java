package com.hyperfine.slideshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ViewSwitcher;

import com.hyperfine.slideshare.R;
import com.hyperfine.slideshare.SlideShowApplication;
import com.hyperfine.slideshare.SlideShowJSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class ImagePickerFragment extends Fragment {
    public final static String TAG = "ImagePickerFragment";
    public final static int REQUEST_IMAGE = 1;

    private Activity m_activityParent;
    private Button m_pickButton;
    private ImageSwitcher m_imageSwitcher;
    private Drawable m_drawableImage = null;
    private File m_slideShowDirectory = null;

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

        SlideShowApplication ssa = (SlideShowApplication)activity.getApplicationContext();
        String slideShowName =  ssa.getCurrentSlideShowName();

        File rootDir = activity.getFilesDir();
        m_slideShowDirectory = new File(rootDir.getAbsolutePath() + "/" + slideShowName);
        m_slideShowDirectory.mkdir();
        if(D)Log.d(TAG, String.format("ImagePickerFragment.onAttach - m_slideShowDirectory=%s", m_slideShowDirectory));

        //
        // BUGBUG - test
        //
        listAllFilesAndDirectories(activity, rootDir);

        //
        // BUGBUG - TEST
        //
        SlideShowJSON ssj;
        try {
            ssj = ssa.getCurrentSlideShowJSON();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "ImagePickerFragment.onAttach", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "ImagePickerFragment.onAttach", e);
            e.printStackTrace();
        }

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

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        m_imageSwitcher = (ImageSwitcher)view.findViewById(R.id.picked_image);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "ImagePickerFragment.onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        m_imageSwitcher.setFactory((ViewSwitcher.ViewFactory)m_activityParent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(D)Log.d(TAG, String.format("ImagePickerFragment.onActivityResult: requestCode=%d, resultCode=%d", requestCode, resultCode));

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                if(D)Log.d(TAG, String.format("ImagePickerFragment.onActivityResult: intent data = %s", intent.getData().toString()));

                copyGalleryImageToJPG("TestImage.jpg", intent);

                InputStream stream = m_activityParent.getContentResolver().openInputStream(intent.getData());
                m_drawableImage = new BitmapDrawable(m_activityParent.getResources(), stream);
                m_imageSwitcher.setImageDrawable(m_drawableImage);
            }
            catch (IOException e) {
                if(E)Log.e(TAG, "ImagePickerFragment.onActivityResult", e);
                e.printStackTrace();
            }
            catch (Exception e) {
                if(E)Log.e(TAG, "ImagePickerFragment.onActivityResult", e);
                e.printStackTrace();
            }
            catch (OutOfMemoryError e) {
                if(E)Log.e(TAG, "ImagePickerFragment.onActivityResult", e);
                e.printStackTrace();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void listAllFilesAndDirectories(Context context, File dir) {
        if(D)Log.d(TAG, String.format("ImagePickerFragment.listAllFilesAndDirectories for %s", dir == null ? "null" : dir));

        ArrayList<File> directories = new ArrayList<File>();

        if (dir == null) {
            dir = context.getFilesDir();
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String file = files[i].getAbsolutePath();
                if(D)Log.d(TAG, String.format("ImagePickerFragment.listAllFilesAndDirectories - file: %s, isDirectory=%b, size=%d", file, files[i].isDirectory(), files[i].length()));

                if (files[i].isDirectory()) {
                    directories.add(files[i]);
                }
            }
        }

        for (int i = 0; i < directories.size(); i++) {
            listAllFilesAndDirectories(context, directories.get(i));
        }
    }

    private void copyGalleryImageToJPG(String fileName, Intent intent) {
        if(D)Log.d(TAG, "ImagePickerFragment.copyGalleryImageToJPG");

        OutputStream outStream = null;

        try {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

            Bitmap bitmapImage = BitmapFactory.decodeStream(m_activityParent.getContentResolver().openInputStream(intent.getData()));
            if (bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputBuffer)) {
                File file = new File(m_slideShowDirectory + "/" + fileName);
                file.createNewFile();
                outStream = new FileOutputStream(file);

                outputBuffer.writeTo(outStream);
            }
            else {
                if(D)Log.d(TAG, "ImagePickerFragment.copyGalleryImageToJPG failed");
            }
        }
        catch (IOException e) {
            if(E)Log.e(TAG, "ImagePickerFragment.copyGalleryImageToJPG", e);
            e.printStackTrace();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "ImagePickerFragment.copyGalleryImageToJPG", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "ImagePickerFragment.copyGalleryImageToJPG", e);
            e.printStackTrace();
        }
        finally {
            if (outStream != null) {
                try {
                    outStream.close();
                }
                catch (Exception e) {
                }
            }
        }
    }
}
