package com.hyperfine.hfgame.services;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.hyperfine.hfgame.PlacesApplication;
import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.utils.RESTHelper.RESTHelperListener;
import com.hyperfine.hfgame.SDK.UserLocationAPI;
import com.hyperfine.hfgame.receivers.ConnectivityChangedReceiver;
import com.hyperfine.hfgame.receivers.LocationChangedReceiver;
import com.hyperfine.hfgame.receivers.PassiveLocationChangedReceiver;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;

/**
 * Service that requests a list of nearby locations from the underlying web service.
 * TODO Update the URL and XML parsing to correspond with your underlying web service.
 */
public class PlacesUpdateService extends IntentService {  

	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_Services";
	
	public final static String LOCATION_STORED_BROADCAST = "com.hyperfine.hfgame.intent.action.LOCATION_STORED";
  
	protected SharedPreferences prefs;
	protected Editor prefsEditor;
	protected ConnectivityManager cm;
	protected boolean lowBattery = false;
	protected boolean mobileData = false;
	protected int prefetchCount = 0;
 
	public PlacesUpdateService() {
		super(TAG);
		setIntentRedeliveryMode(false);
	}
  
	/**
	 * Set the Intent Redelivery mode to true to ensure the Service starts "Sticky"
	 * Defaults to "true" on legacy devices.
	 */
	protected void setIntentRedeliveryMode(boolean enable) {}

	/**
	 * Returns battery status. True if less than 10% remaining.
	 * @param battery Battery Intent
	 * @return Battery is low
	 */
	protected boolean getIsLowBattery(Intent battery) {
		float pctLevel = (float)battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 1) / 
				battery.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
		return pctLevel < 0.15;
	}
  
	@Override
	public void onCreate() {
		super.onCreate();
		cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		prefs = getSharedPreferences(Config.PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
	}
	
	@Override
	public void onDestroy() {
		if(D)Log.d(TAG, "PlacesUpdateService.onDestroy");
		
		super.onDestroy();
	}

	/**
	 * {@inheritDoc}
	 * Checks the battery and connectivity state before removing stale venues
	 * and initiating a server poll for new venues around the specified 
	 * location within the given radius.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// Check if we're running in the foreground, if not, check if
		// we have permission to do background updates.
		// BUGBUG!!
		boolean backgroundAllowed = cm.getBackgroundDataSetting();
		boolean inBackground = prefs.getBoolean(Config.PlacesConstants.EXTRA_KEY_IN_BACKGROUND, true);
  	
		if (!backgroundAllowed && inBackground) return;
	
		// Extract the location and radius around which to conduct our search.
		Location location = new Location(Config.PlacesConstants.CONSTRUCTED_LOCATION_PROVIDER);
		//int radius = Config.PlacesConstants.DEFAULT_RADIUS;
    
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(Config.PlacesConstants.EXTRA_KEY_LOCATION)) {
			location = (Location)(extras.get(Config.PlacesConstants.EXTRA_KEY_LOCATION));
			//radius = extras.getInt(Config.PlacesConstants.EXTRA_KEY_RADIUS, Config.PlacesConstants.DEFAULT_RADIUS);
		}
    
		// Check if we're in a low battery situation.
		IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent battery = registerReceiver(null, batIntentFilter);
		lowBattery = getIsLowBattery(battery);
    
		// Check if we're connected to a data network, and if so - if it's a mobile network.
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		mobileData = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

		// If we're not connected, enable the connectivity receiver and disable the location receiver.
		// There's no point trying to poll the server for updates if we're not connected, and the 
		// connectivity receiver will turn the location-based updates back on once we have a connection.
		if (!isConnected) {
			if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - we are not currently connected");
			
			PackageManager pm = getPackageManager();
      
			ComponentName connectivityReceiver = new ComponentName(this, ConnectivityChangedReceiver.class);
			ComponentName locationReceiver = new ComponentName(this, LocationChangedReceiver.class);
			ComponentName passiveLocationReceiver = new ComponentName(this, PassiveLocationChangedReceiver.class);

			pm.setComponentEnabledSetting(connectivityReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP);
            
			pm.setComponentEnabledSetting(locationReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
					PackageManager.DONT_KILL_APP);
      
			pm.setComponentEnabledSetting(passiveLocationReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
					PackageManager.DONT_KILL_APP);
		}
		else {
			if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - we're connected");
			if(D)Log.d(TAG, String.format(
					"--- and our location is: long: %f, lat: %f, altitude: %f, accuracy: %f",
					location.getLongitude(), location.getLatitude(), location.getAltitude(), location.getAccuracy()));
			
			// If we are connected check to see if this is a forced update (typically triggered
			// when the location has changed).
			boolean doUpdate = intent.getBooleanExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, false);
      
			// If it's not a forced update (for example from the Activity being restarted) then
			// check to see if we've moved far enough, or there's been a long enough delay since
			// the last update and if so, enforce a new update.
			if (!doUpdate) {
				if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - not a forced update");
				
				// Retrieve the last update time and place.
				long lastTime = prefs.getLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
				long lastLat = prefs.getLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
				long lastLng = prefs.getLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
				Location lastLocation = new Location(Config.PlacesConstants.CONSTRUCTED_LOCATION_PROVIDER);
				lastLocation.setLatitude(lastLat);
				lastLocation.setLongitude(lastLng);
        
				// If update time and distance bounds have been passed, do an update.
				if ((lastTime < System.currentTimeMillis()-Config.PlacesConstants.MAX_TIME) ||
						(lastLocation.distanceTo(location) > Config.PlacesConstants.MAX_DISTANCE))
					doUpdate = true;
			}
      
			if (doUpdate) {
				if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - calling storeUserLocation.");
				storeUserLocation(location);
			}
			else {
				if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - not calling storeUserLocation.");
			}
		}
	}
	
	/**
	 * Stores the user location in the database via the UserLocationAPI
	 * @param location Location
	 */
	protected void storeUserLocation(Location location) {
		if(D)Log.d(TAG, "PlacesUpdateService.storeUserLocation");
		
		PlacesApplication app = (PlacesApplication)getApplication();
		String userId = app.getUserId();
		if (userId == null) {
			if(D)Log.d(TAG, "PlacesUpdateService.storeUserLocation - no userId yet, so bailing.");
			return;			
		}
		
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		double altitude = location.getAltitude();
		float accuracy = location.getAccuracy();
		long timeVal = location.getTime();
		
		// Broadcast the result
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PlacesUpdateService.LOCATION_STORED_BROADCAST);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra("longitude", longitude);
		broadcastIntent.putExtra("latitude", latitude);
		broadcastIntent.putExtra("altitude", altitude);
		broadcastIntent.putExtra("accuracy", accuracy);
		broadcastIntent.putExtra("date", timeVal);
		
		if(D)Log.d(TAG, "PlacesUpdateService.storeUserLocation - broadcasting LOCATION_STORED notification");
		sendBroadcast(broadcastIntent);
		
		UserLocationAPI.createLocation(
				userId, longitude, latitude, altitude, (double)accuracy, timeVal, 
				new RESTHelperListener() {
					public void onRESTResponse(int httpResult, String responseString) {
						if(D)Log.d(TAG, String.format("PlacesUpdateService.UserLocationAPI.createLocation: httpResult=%d, response=%s", httpResult, responseString));
						PlacesApplication app = (PlacesApplication)getApplication();
						int nCalls = app.getNumUserLocationCalls();
						nCalls++;
						app.setNumUserLocationCalls(nCalls);
					}					
				});
	}
}