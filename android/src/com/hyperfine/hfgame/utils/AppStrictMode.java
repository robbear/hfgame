package com.hyperfine.hfgame.utils;

import android.os.StrictMode;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * Implementation that supports the Strict Mode functionality
 * available in Honeycomb and later. 
 */
public class AppStrictMode {
	public static String TAG = "HFGame";
  
	/**
	 * Enable {@link StrictMode}
	 * TODO Set your preferred Strict Mode features.
	 */
	public void enableStrictMode() {
		if(D)Log.d(TAG, "AppStrictMode.enableStrictMode");
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork()
		.penaltyLog()
		.penaltyFlashScreen()
		.build());
	}
}
