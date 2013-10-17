package com.hyperfine.slideshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.File;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class Utilities {
    public final static String TAG = "Utilities";

    //
    // Creates or gets the SlideShare directory for SlideShare name, slideShareName.
    // This method also sets the SSPreferences.SSNAME to slideShareName.
    //
    public static File createOrGetSlideShareDirectory(Context context, String slideShareName) {
        if(D)Log.d(TAG, String.format("Utilities.createOrGetSlideShareDirectory: dirName=%s", slideShareName));

        SharedPreferences prefs = context.getSharedPreferences(SSPreferences.PREFS, Context.MODE_PRIVATE);

        File rootDir = context.getFilesDir();

        File slideShareDirectory = new File(rootDir.getAbsolutePath() + "/" + slideShareName);
        slideShareDirectory.mkdir();

        Editor editor = prefs.edit();
        editor.putString(SSPreferences.PREFS_SSNAME, slideShareName);
        editor.commit();

        return slideShareDirectory;
    }

    private static File createFile(File directory, String fileName) {
        if(D)Log.d(TAG, String.format("Utilities.createFile: directory=%s, fileName=%s", directory.getAbsolutePath(), fileName));

        File file = null;

        try {
            file = new File(directory.getAbsolutePath() + "/" + fileName);
            file.createNewFile();
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "Utilities.createFile", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "Utilities.createFile", e);
            e.printStackTrace();
        }

        return file;
    }

    public static File createFile(Context context, String directoryName, String fileName) {
        if(D)Log.d(TAG, String.format("Utilities.createFile: directoryName=%s, fileName=%s", directoryName, fileName));

        File directory = createOrGetSlideShareDirectory(context, directoryName);
        if (directory == null) {
            if(D)Log.d(TAG, "Utilities.createFile - createOrGetSlideShareDirectory returned null. Bailing.");
            return null;
        }

        return createFile(directory, fileName);
    }
}
