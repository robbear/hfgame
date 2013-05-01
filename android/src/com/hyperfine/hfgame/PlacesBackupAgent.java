package com.hyperfine.hfgame;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * A class that specifies which of the shared preferences you want to backup
 * to the Google Backup Service.
 */
public class PlacesBackupAgent extends BackupAgentHelper {
	public final static String TAG = "HFGame";
	
	@Override
	public void onCreate() {
		if(D)Log.d(TAG, "PlacesBackupAgent.onCreate");
		
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, Config.PlacesConstants.SHARED_PREFERENCE_FILE);
		addHelper(Config.PlacesConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, helper);
		// TODO Add additional helpers for each of the preferences you want to backup.
	}
}