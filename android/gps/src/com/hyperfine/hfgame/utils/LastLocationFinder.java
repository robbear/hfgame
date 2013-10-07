package com.hyperfine.hfgame.utils;

import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;

/**
 * Optimized implementation of Last Location Finder for devices running Gingerbread  
 * and above.
 * 
 * This class lets you find the "best" (most accurate and timely) previously 
 * detected location using whatever providers are available. 
 * 
 * Where a timely / accurate previous location is not detected it will
 * return the newest location (where one exists) and setup a one-shot 
 * location update to find the current location.
 */
public class LastLocationFinder {
  
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_Utils";

	protected static String SINGLE_LOCATION_UPDATE_ACTION = "com.hyperfine.hfgame.SINGLE_LOCATION_UPDATE_ACTION";
	  
	protected PendingIntent m_singleUpatePI;
	protected LocationListener m_locationListener;
	protected LocationManager m_locationManager;
	protected Context m_context;
	protected Criteria m_criteria;
  
	/**
	 * Construct a new Last Location Finder.
	 * @param context Context
	 */
	public LastLocationFinder(Context context) {
		if(D)Log.d(TAG, "LastLocationFinder.constructor");
		
		m_context = context;
		m_locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
		// Coarse accuracy is specified here to get the fastest possible result.
		// The calling Activity will likely (or have already) request ongoing
		// updates using the Fine location provider.
		m_criteria = new Criteria();
		m_criteria.setAccuracy(Criteria.ACCURACY_LOW);
		
		// Construct the Pending Intent that will be broadcast by the one-shot
		// location update.
		Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);  
		m_singleUpatePI = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
  
	/**
	 * Returns the most accurate and timely previously detected location.
	 * Where the last result is beyond the specified maximum distance or 
	 * latency a one-off location update is returned via the {@link LocationListener}
	 * specified in {@link setChangedLocationListener}.
	 * @param minDistance Minimum distance before we require a location update.
	 * @param minTime Minimum time required between location updates.
	 * @return The most accurate and / or timely previously detected location.
	 */
	public Location getLastBestLocation(int minDistance, long minTime) {
		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
	    long bestTime = Long.MIN_VALUE;
	    
	    if(D)Log.d(TAG, "LastLocationFinder.getLastBestLocation");
	    
	    // Iterate through all the providers on the system, keeping
	    // note of the most accurate result within the acceptable time limit.
	    // If no result is found within maxTime, return the newest Location.
	    List<String> matchingProviders = m_locationManager.getAllProviders();
	    for (String provider: matchingProviders) {
	    	Location location = m_locationManager.getLastKnownLocation(provider);
	    	if (location != null) {
	    		float accuracy = location.getAccuracy();
	    		long time = location.getTime();
	        
	    		if ((time > minTime && accuracy < bestAccuracy)) {
	    			bestResult = location;
	    			bestAccuracy = accuracy;
	    			bestTime = time;
	    		}
	    		else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
	    			bestResult = location;
	    			bestTime = time;
	    		}
	    	}
	    }
	    
	    // If the best result is beyond the allowed time limit, or the accuracy of the
	    // best result is wider than the acceptable maximum distance, request a single update.
	    // This check simply implements the same conditions we set when requesting regular
	    // location updates every [minTime] and [minDistance]. 
	    if (m_locationListener != null && (bestTime < minTime || bestAccuracy > minDistance)) { 
	    	IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
	    	m_context.registerReceiver(singleUpdateReceiver, locIntentFilter);      
	    	m_locationManager.requestSingleUpdate(m_criteria, m_singleUpatePI);
	    }
	    
	    return bestResult;
	}
  
	/**
	 * This {@link BroadcastReceiver} listens for a single location
	 * update before unregistering itself.
	 * The one-shot location update is returned via the {@link LocationListener}
	 * specified in {@link setChangedLocationListener}.
	 */
	protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(D)Log.d(TAG, "LastLocationFinder.BroadcastReceiver.onReceive");
			
			context.unregisterReceiver(singleUpdateReceiver);
      
			String key = LocationManager.KEY_LOCATION_CHANGED;
			Location location = (Location)intent.getExtras().get(key);
      
			if (m_locationListener != null && location != null)
				m_locationListener.onLocationChanged(location);
      
			m_locationManager.removeUpdates(m_singleUpatePI);
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public void setChangedLocationListener(LocationListener l) {
		m_locationListener = l;
	}

	/**
	 * {@inheritDoc}
	 */
	public void cancel() {
		if(D)Log.d(TAG, "LastLocationFinder.cancel");
		
		m_locationManager.removeUpdates(m_singleUpatePI);
	}
}
