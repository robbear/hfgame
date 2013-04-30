package com.hyperfine.hfgame.utils.base;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Abstract base class that can be extended to provide classes that save 
 * {@link SharedPreferences} in the most efficient way possible. 
 * Descendant classes can optionally choose to backup some {@link SharedPreferences}
 * to the Google {@link BackupService} on platforms where this is available.
 */
public abstract class SharedPreferenceSaver {
  
  protected Context context;
  
  protected SharedPreferenceSaver(Context context) {
    this.context = context;
  }
  
  /**
   * Save the Shared Preferences modified through the Editor object.
   * @param editor Shared Preferences Editor to commit.
   * @param backup Backup to the cloud if possible.
   */
  public void savePreferences(SharedPreferences.Editor editor, boolean backup) {}
}
