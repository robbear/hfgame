package com.hyperfine.hfgame.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;

import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.utils.LocationUpdateRequester;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * This Receiver class is designed to listen for system boot.
 * 
 * If the app has been run at least once, the passive location
 * updates should be enabled after a reboot.
 */
public class BootReceiver extends BroadcastReceiver {
	public final static String TAG = "HFGame";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(D)Log.d(TAG, "BootReceiver.onReceive");
		
		SharedPreferences prefs = context.getSharedPreferences(Config.PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
		boolean runOnce = prefs.getBoolean(Config.PlacesConstants.SP_KEY_RUN_ONCE, false);
  	
		if (runOnce) {
			if(D)Log.d(TAG, "BootReceiver.onReceive - we've been run once, so check to see if we should follow locations");
			LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
  		
			// Instantiate a Location Update Requester class based on the available platform version.
			// This will be used to request location updates.
			LocationUpdateRequester locationUpdateRequester = new LocationUpdateRequester(locationManager);
   
			// Check the Shared Preferences to see if we are updating location changes.
			boolean followLocationChanges = prefs.getBoolean(Config.PlacesConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, true);
      
			if (followLocationChanges) {
				if(D)Log.d(TAG, "BootReceiver.onReceive - yes, we will ask for passive location updates from third party apps when we're not visible");
				
				// Passive location updates from 3rd party apps when the Activity isn't visible.
				Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
				PendingIntent locationListenerPassivePendingIntent = PendingIntent.getActivity(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				locationUpdateRequester.requestPassiveLocationUpdates(Config.PlacesConstants.PASSIVE_MAX_TIME, Config.PlacesConstants.PASSIVE_MAX_DISTANCE, locationListenerPassivePendingIntent);
			}
		}
	}
}