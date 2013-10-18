package com.hyperfine.slideshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

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

        File rootDir = getRootFilesDirectory(context);

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

    public static boolean saveStringToFile(Context context, String data, String folder, String fileName) {
        if(D)Log.d(TAG, String.format("Utilities.saveStringToFile: folder=%s, fileName=%s", folder, fileName));

        boolean retVal = false;

        File dirRoot = getRootFilesDirectory(context);
        File directory = new File(dirRoot.getAbsolutePath() + "/" + folder);
        File file = new File(directory, fileName);

        FileOutputStream fos = null;

        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            retVal = true;
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "Utilities.saveStringToFile", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "Utilities.saveStringToFile", e);
            e.printStackTrace();
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }
            catch (Exception e) {}
        }

        return retVal;
    }

    public static String loadStringFromFile(Context context, String folder, String fileName) {
        if(D)Log.d(TAG, String.format("Utilities.loadStringFromFile: folder=%s, fileName=%s", folder, fileName));

        FileInputStream fis = null;
        String data = null;

        File dirRoot = getRootFilesDirectory(context);
        File directory = new File(dirRoot.getAbsolutePath() + "/" + folder);
        if (directory.exists() && directory.isDirectory()) {
            File file = new File(directory, fileName);
            if (file.exists()) {
                try {
                    byte[] buffer = new byte[(int)file.length()];
                    fis = new FileInputStream(file);
                    fis.read(buffer);

                    data = new String(buffer, "UTF-8");
                }
                catch (Exception e) {
                    if(E)Log.e(TAG, "Utilities.loadStringFromFile", e);
                    e.printStackTrace();
                }
                catch (OutOfMemoryError e) {
                    if(E)Log.e(TAG, "Utilities.loadStringFromFile", e);
                    e.printStackTrace();
                }
                finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                    catch (Exception e) {}
                }
            }
            else {
                if(D)Log.d(TAG, "Utilities.loadStringFromFile - file doesn't exist. Bailing.");
            }
        }
        else {
            if(D)Log.d(TAG, "Utilities.loadStringFromFile - folder doesn't exist. Bailing.");
        }

        return data;
    }

    public static File getRootFilesDirectory(Context context) {
        if(D)Log.d(TAG, "Utilities.getRootFilesDirectory");

        File dir = null;
        if (Config.USE_CACHE) {
            dir = context.getCacheDir();
        }
        else {
            dir = context.getFilesDir();
        }

        if(D)Log.d(TAG, String.format("Utilities.getRootFilesDirectory - returning %s", dir.getAbsolutePath()));

        return dir;
    }

    public static void listAllFilesAndDirectories(Context context, File dir) {
        if(D)Log.d(TAG, String.format("Utilities.listAllFilesAndDirectories for %s", dir == null ? "null" : dir));

        ArrayList<File> directories = new ArrayList<File>();

        if (dir == null) {
            dir = getRootFilesDirectory(context);
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String file = files[i].getAbsolutePath();
                if(D)Log.d(TAG, String.format("Utilities.listAllFilesAndDirectories - file: %s, isDirectory=%b, size=%d", file, files[i].isDirectory(), files[i].length()));

                if (files[i].isDirectory()) {
                    directories.add(files[i]);
                }
            }
        }

        for (int i = 0; i < directories.size(); i++) {
            listAllFilesAndDirectories(context, directories.get(i));
        }
    }
}
