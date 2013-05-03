package com.hyperfine.hfgame.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.hyperfine.hfgame.services.PlacesUpdateService;
import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.utils.LastLocationFinder;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred while this application isn't visible.
 * 
 * Where possible, this is triggered by a Passive Location listener.
 */
public class PassiveLocationChangedReceiver extends BroadcastReceiver {
  
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_Receivers";
  
	/**
	 * When a new location is received, extract it from the Intent and use
	 * it to start the Service used to update the list of nearby places.
	 * 
	 * This is the Passive receiver, used to receive Location updates from 
	 * third party apps when the Activity is not visible. 
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(D)Log.d(TAG, "PassiveLocationChangedReceiver.onReceive");
		
		String key = LocationManager.KEY_LOCATION_CHANGED;
		Location location = null;
    
		if (intent.hasExtra(key)) {
			// This update came from Passive provider, so we can extract the location
			// directly.
			if(D)Log.d(TAG, "PassiveLocationChangedReceiver.onReceive - update is from Passive provider");
			location = (Location)intent.getExtras().get(key);      
		}
		else {
			// This update came from a recurring alarm. We need to determine if there
			// has been a more recent Location received than the last location we used.
			if(D)Log.d(TAG, "PassiveLocationChangedReceiver.onReceive - update is from a recurring alarm");
      
			// Get the best last location detected from the providers.
			LastLocationFinder lastLocationFinder = new LastLocationFinder(context);
			location = lastLocationFinder.getLastBestLocation(Config.PlacesConstants.MAX_DISTANCE, System.currentTimeMillis()-Config.PlacesConstants.MAX_TIME);
			SharedPreferences prefs = context.getSharedPreferences(Config.PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
      
			// Get the last location we used to get a listing.
			long lastTime = prefs.getLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
			long lastLat = prefs.getLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
			long lastLng = prefs.getLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
			Location lastLocation = new Location(Config.PlacesConstants.CONSTRUCTED_LOCATION_PROVIDER);
			lastLocation.setLatitude(lastLat);
			lastLocation.setLongitude(lastLng);

			// Check if the last location detected from the providers is either too soon, or too close to the last
			// value we used. If it is within those thresholds we set the location to null to prevent the update
			// Service being run unnecessarily (and spending battery on data transfers).
			if ((lastTime > System.currentTimeMillis()-Config.PlacesConstants.MAX_TIME) ||
					(lastLocation.distanceTo(location) < Config.PlacesConstants.MAX_DISTANCE)) {
				location = null;
			}
		}
    
		// Start the Service used to find nearby points of interest based on the last detected location.
		if (location != null) {
			if(D)Log.d(TAG, "PassiveLocationChangedReceiver.onReceive - Passively updating place list.");
			if(D)Log.d(TAG, String.format(
					"...passive location: accuracy: %f, long: %f, lat: %f, alt: %f", 
					location.getAccuracy(), location.getLongitude(), location.getLatitude(), location.getAltitude()));
			Intent updateServiceIntent = new Intent(context, PlacesUpdateService.class);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_LOCATION, location);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_RADIUS, Config.PlacesConstants.DEFAULT_RADIUS);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, false);
			context.startService(updateServiceIntent);   
		}
	}
}