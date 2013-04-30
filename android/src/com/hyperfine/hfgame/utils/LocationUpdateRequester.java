package com.hyperfine.hfgame.utils;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * Provides support for initiating active and passive location updates 
 * optimized for the Gingerbread release. Includes use of the Passive Location Provider.
 * 
 * Uses broadcast Intents to notify the app of location changes.
 */
public class LocationUpdateRequester {
	public final static String TAG = "HFGame";
	
	protected LocationManager m_locationManager;

	public LocationUpdateRequester(LocationManager locationManager) {
		m_locationManager = locationManager;
	}

	/**
	 * Request passive location updates.
	 * These updates will be triggered by locations received by 3rd party apps that have requested location updates.
	 * The minimum time and distance for passive updates will typically be longer than for active updates. The trick
	 * is to balance the difference to minimize battery drain by maximize freshness.
	 * @param minTime Minimum time that should elapse between location update broadcasts.
	 * @param minDistance Minimum distance that should have been moved between location update broadcasts.
	 * @param pendingIntent The Pending Intent to broadcast to notify the app of passive location changes.
	 */
	public void requestPassiveLocationUpdates(long minTime, long minDistance, PendingIntent pendingIntent) {
		if(D)Log.d(TAG, "LocationUpdateRequester.requestPassiveLocationUpdates");
		
		// Froyo introduced the Passive Location Provider, which receives updates whenever a 3rd party app 
	    // receives location updates.
	    m_locationManager.requestLocationUpdates(
	    		LocationManager.PASSIVE_PROVIDER, Config.PlacesConstants.MAX_TIME, Config.PlacesConstants.MAX_DISTANCE, pendingIntent);    
	}

	/**
	 * Request active location updates. 
	 * These updates will be triggered by a direct request from the Location Manager.
	 * @param minTime Minimum time that should elapse between location update broadcasts.
	 * @param minDistance Minimum distance that should have been moved between location update broadcasts.
	 * @param criteria Criteria that define the Location Provider to use to detect the Location.
	 * @param pendingIntent The Pending Intent to broadcast to notify the app of active location changes.
	 */
	public void requestLocationUpdates(long minTime, long minDistance, Criteria criteria, PendingIntent pendingIntent) {
		if(D)Log.d(TAG, "LocationUpdateRequester.requestLocationUpdates");
		
		// Gingerbread and above supports a location update request that accepts criteria directly.
		// Note that we aren't monitoring this provider to check if it becomes disabled - this is handled by the calling Activity.
		m_locationManager.requestLocationUpdates(minTime, minDistance, criteria, pendingIntent);
	}
}
