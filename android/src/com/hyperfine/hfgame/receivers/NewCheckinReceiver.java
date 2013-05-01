package com.hyperfine.hfgame.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.services.CheckinNotificationService;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * Manifest Receiver that listens for broadcasts announcing a successful checkin.
 * This class starts the CheckinNotification Service that will trigger a notification
 * announcing the successful checkin. We don't want notifications for this app to 
 * be announced while the app is running, so this receiver is disabled whenever the
 * main Activity is visible. 
 */
public class NewCheckinReceiver extends BroadcastReceiver {
  
	protected static String TAG = "HFGame";
  
	/**
	 * When a successful checkin is announced, extract the unique ID of the place
	 * that's been checked in to, and pass this value to the CheckinNotification Service
	 * when you start it.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(D)Log.d(TAG, "NewCheckinReceiver.onReceive");
		
		String id = intent.getStringExtra(Config.PlacesConstants.EXTRA_KEY_ID);
		if (id != null) {
			Intent serviceIntent = new Intent(context, CheckinNotificationService.class);
			serviceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_ID, id);
			context.startService(serviceIntent);
		}
	}
}