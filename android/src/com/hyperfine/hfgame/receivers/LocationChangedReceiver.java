package com.hyperfine.hfgame.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.hyperfine.hfgame.services.PlacesUpdateService;
import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred. This is used instead of a LocationListener
 * within an Activity is our only action is to start a service.
 */
public class LocationChangedReceiver extends BroadcastReceiver {
  
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_Receivers";
  
	/**
	 * When a new location is received, extract it from the Intent and use
	 * it to start the Service used to update the list of nearby places.
	 * 
	 * This is the Active receiver, used to receive Location updates when 
	 * the Activity is visible. 
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(D)Log.d(TAG, "LocationChangedReceiver.onReceive");
		
		String locationKey = LocationManager.KEY_LOCATION_CHANGED;
		String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
		if (intent.hasExtra(providerEnabledKey)) {
			if (!intent.getBooleanExtra(providerEnabledKey, true)) {
				Intent providerDisabledIntent = new Intent(Config.PlacesConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
				context.sendBroadcast(providerDisabledIntent);    
			}
		}
		if (intent.hasExtra(locationKey)) {
			Location location = (Location)intent.getExtras().get(locationKey);
			if(D)Log.d(TAG, "LocationChangedReceiver.onReceive - Actively Updating place list");
			if(D)Log.d(TAG, String.format(
					"... location: accuracy: %f, long: %f, lat: %f, alt: %f", 
					location.getAccuracy(), location.getLongitude(), location.getLatitude(), location.getAltitude()));
			Intent updateServiceIntent = new Intent(context, PlacesUpdateService.class);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_LOCATION, location);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_RADIUS, Config.PlacesConstants.DEFAULT_RADIUS);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, true);
			context.startService(updateServiceIntent);
		}
	}
}