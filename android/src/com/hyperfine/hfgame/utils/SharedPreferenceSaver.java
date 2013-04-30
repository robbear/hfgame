package com.hyperfine.hfgame.utils;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;

/**
 * Abstract base class that can be extended to provide classes that save 
 * {@link SharedPreferences} in the most efficient way possible. 
 * Descendant classes can optionally choose to backup some {@link SharedPreferences}
 * to the Google {@link BackupService} on platforms where this is available.
 */
public class SharedPreferenceSaver {
	public final static String TAG = "HFGame";
	
	protected Context m_context;
	protected BackupManager m_backupManager;
	
	public SharedPreferenceSaver(Context context) {
		m_context = context;
		m_backupManager = new BackupManager(m_context);
	}

	/**
	 * Save the Shared Preferences modified through the Editor object.
	 * @param editor Shared Preferences Editor to commit.
	 * @param backup Backup to the cloud if possible.
	 */
	public void savePreferences(Editor editor, boolean backup) {
		if(D)Log.d(TAG, "SharedPreferenceSaver.savePreferences");
		
		editor.commit();    
		m_backupManager.dataChanged();
	}
}
