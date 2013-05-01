//
// Copyright (c) 2013 Hyperfine Software Corp.
// All rights reserved
//

package com.hyperfine.hfgame.utils;

import android.app.AlarmManager;

public class Config
{
    // Build string to appear in Information page
    public static final String buildString = "g000000000000";
    
    // Developer mode - ship with this set to false
    public static final boolean isDeveloperMode = true;

    // Is this an Amazon release?
    public static final boolean isAmazon = false;
    
    // Error logs - ship with this set to false
    public static final boolean E = true;
	
    // Debug logs - ship with this set to false
    public static final boolean D = true;
	
    // Verbose logs - ship with this set to false
    public static final boolean V = true;
    
    //
    // PlacesConstants
    //
    public class PlacesConstants {
    	  
    	/**
    	 * TODO **P1**  You must put your Google Places API key here.
    	 * You can get your API key from: 
    	 * {@link http://code.google.com/apis/maps/documentation/places/#Limits}
    	 */
    	private static final String MY_API_KEY = "AIzaSyAht-LIFnld9wtevEfyrOziMlGujBJ5NkU";
    	  
    	public static final String PLACES_API_KEY = "&key=" + MY_API_KEY;
    	  
    	/**
    	 * You'll need to modify these values to suit your own app.
    	 */
    	public static final boolean DEVELOPER_MODE = Config.isDeveloperMode;
    	  
    	// TODO Point these at your data sources.
    	public static final String PLACES_LIST_BASE_URI = "https://maps.googleapis.com/maps/api/place/search/xml?sensor=true";
    	public static final String PLACES_DETAIL_BASE_URI = "https://maps.googleapis.com/maps/api/place/details/xml?sensor=true&reference=";
    	public static final String PLACES_CHECKIN_URI = "https://maps.googleapis.com/maps/api/place/check-in/xml?sensor=true";
    	public static final String PLACES_CHECKIN_OK_STATUS = "OK";
    	  
    	/**
    	 * These values control the user experience of your app. You should
    	 * modify them to provide the best experience based on how your
    	 * app will actually be used.
    	 * TODO Update these values for your app.
    	 */
    	// The default search radius when searching for places nearby.
    	public static final int DEFAULT_RADIUS = 5000;/*150;*/
    	// The maximum distance the user should travel between location updates. 
    	public static final int MAX_DISTANCE = DEFAULT_RADIUS/2;
    	// The maximum time that should pass before the user gets a location update.
    	public static final long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    	 
    	// You will generally want passive location updates to occur less frequently
    	// than active updates. You need to balance location freshness with battery life.
    	// The location update distance for passive updates.
    	public static final int PASSIVE_MAX_DISTANCE = MAX_DISTANCE;
    	// The location update time for passive updates
    	public static final long PASSIVE_MAX_TIME = MAX_TIME;
    	// Use the GPS (fine location provider) when the Activity is visible?
    	public static final boolean USE_GPS_WHEN_ACTIVITY_VISIBLE = true;
    	//When the user exits via the back button, do you want to disable
    	// passive background updates.
    	public static final boolean DISABLE_PASSIVE_LOCATION_WHEN_USER_EXIT = false;
    	  
    	// Maximum latency before you force a cached detail page to be updated.
    	public static final long MAX_DETAILS_UPDATE_LATENCY = AlarmManager.INTERVAL_DAY;
    	  
    	// Pre-fetching place details is useful but potentially expensive. The following
    	// values lets you disable pre-fetching when on mobile data or low battery conditions.
    	// Only pre-fetch on WIFI?
    	public static final boolean PREFETCH_ON_WIFI_ONLY = false;
    	// Disable prefetching when battery is low?
    	public static final boolean DISABLE_PREFETCH_ON_LOW_BATTERY = true;
    	  
    	// How long to wait before retrying failed checkins.
    	public static final long CHECKIN_RETRY_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    	  
    	// The maximum number of locations to prefetch for each update.
    	public static final int PREFETCH_LIMIT = 5;
    	 
    	  
    	/**
    	 * These values are constants used for intents, extras, and shared preferences.
    	 * You shouldn't need to modify them.
    	 */
    	public static final String SHARED_PREFERENCE_FILE = "SHARED_PREFERENCE_FILE";
    	public static final String SP_KEY_FOLLOW_LOCATION_CHANGES = "SP_KEY_FOLLOW_LOCATION_CHANGES";
    	public static final String SP_KEY_LAST_LIST_UPDATE_TIME = "SP_KEY_LAST_LIST_UPDATE_TIME";
    	public static final String SP_KEY_LAST_LIST_UPDATE_LAT = "SP_KEY_LAST_LIST_UPDATE_LAT";
    	public static final String SP_KEY_LAST_LIST_UPDATE_LNG = "SP_KEY_LAST_LIST_UPDATE_LNG";
    	public static final String SP_KEY_LAST_CHECKIN_ID = "SP_KEY_LAST_CHECKIN_ID";
    	public static final String SP_KEY_LAST_CHECKIN_TIMESTAMP = "SP_KEY_LAST_CHECKIN_TIMESTAMP";
    	public static final String SP_KEY_RUN_ONCE = "SP_KEY_RUN_ONCE";
    	  
    	public static final String EXTRA_KEY_REFERENCE = "reference";
    	public static final String EXTRA_KEY_ID = "id";
    	public static final String EXTRA_KEY_LOCATION = "location";
    	public static final String EXTRA_KEY_RADIUS = "radius";
    	public static final String EXTRA_KEY_TIME_STAMP = "time_stamp";
    	public static final String EXTRA_KEY_FORCEREFRESH = "force_refresh";
    	public static final String EXTRA_KEY_IN_BACKGROUND = "EXTRA_KEY_IN_BACKGROUND";
    	 
    	public static final String ARGUMENTS_KEY_REFERENCE = "reference";
    	public static final String ARGUMENTS_KEY_ID = "id";
    	  
    	public static final String NEW_CHECKIN_ACTION = "com.hyperfine.places.NEW_CHECKIN_ACTION";
    	public static final String RETRY_QUEUED_CHECKINS_ACTION = "com.hyperfine.places.retry_queued_checkins";
    	public static final String ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED = "com.hyperfine.places.active_location_update_provider_disabled";
    	    	  
    	public static final String CONSTRUCTED_LOCATION_PROVIDER = "CONSTRUCTED_LOCATION_PROVIDER";
    	  
    	public static final int CHECKIN_NOTIFICATION = 0;
   	}    
}
