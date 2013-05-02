package com.hyperfine.hfgame.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import com.hyperfine.hfgame.content_providers.PlaceDetailsContentProvider;
import com.hyperfine.hfgame.content_providers.PlacesContentProvider;
import com.hyperfine.hfgame.receivers.ConnectivityChangedReceiver;
import com.hyperfine.hfgame.receivers.LocationChangedReceiver;
import com.hyperfine.hfgame.receivers.PassiveLocationChangedReceiver;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

/**
 * Service that requests a list of nearby locations from the underlying web service.
 * TODO Update the URL and XML parsing to correspond with your underlying web service.
 */
public class PlacesUpdateService extends IntentService {  

	protected static String TAG = "HFGame";
  
	protected ContentResolver contentResolver;
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
		contentResolver = getContentResolver();
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
		int radius = Config.PlacesConstants.DEFAULT_RADIUS;
    
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(Config.PlacesConstants.EXTRA_KEY_LOCATION)) {
			location = (Location)(extras.get(Config.PlacesConstants.EXTRA_KEY_LOCATION));
			radius = extras.getInt(Config.PlacesConstants.EXTRA_KEY_RADIUS, Config.PlacesConstants.DEFAULT_RADIUS);
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
				if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - refreshing places and calling UserLocationAPI");
				storeUserLocation(location);
				
				// Refresh the prefetch count for each new location.
				prefetchCount = 0;
				// Remove the old locations
				removeOldLocations(location, radius);
				// Hit the server for new venues for the current location.
				refreshPlaces(location, radius);
			}
			else {
				if(D)Log.d(TAG, "Place List is fresh: Not refreshing");
			}
      
			// Retry any queued checkins.
			Intent checkinServiceIntent = new Intent(this, PlaceCheckinService.class);
			startService(checkinServiceIntent);
		}
		
		if(D)Log.d(TAG, "PlacesUpdateService.onHandleIntent - Place List Download Service Complete");
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

		UserLocationAPI.createLocation(
				userId, location.getLongitude(), location.getLatitude(), 
				location.getAltitude(), (double)location.getAccuracy(), location.getTime(), 
				new RESTHelperListener() {
					public void onRESTResponse(int httpResult, String responseString) {
						if(D)Log.d(TAG, String.format("PlacesUpdateService.UserLocationAPI.createLocation: httpResult=%d, response=%s", httpResult, responseString));
					}					
				});
	}
  
	/**
	 * Polls the underlying service to return a list of places within the specified
	 * radius of the specified Location. 
	 * @param location Location
	 * @param radius Radius
	 */
	protected void refreshPlaces(Location location, int radius) {   
		// Log to see if we'll be prefetching the details page of each new place.
		if (mobileData) {
			if(D)Log.d(TAG, "Not prefetching due to being on mobile");
		} 
		else if (lowBattery) {
			if(D)Log.d(TAG, "Not prefetching due to low battery");
		}

		long currentTime = System.currentTimeMillis();
		URL url;
    
		try {
			// TODO Replace this with a URI to your own service.
			String locationStr = location.getLatitude() + "," + location.getLongitude();
			String baseURI = Config.PlacesConstants.PLACES_LIST_BASE_URI;
			String placesFeed = baseURI + "&location=" + locationStr + "&radius=" + radius + Config.PlacesConstants.PLACES_API_KEY;
			url = new URL(placesFeed);
           
			// Open the connection
			URLConnection connection = url.openConnection();
			HttpsURLConnection httpConnection = (HttpsURLConnection)connection; 
			int responseCode = httpConnection.getResponseCode(); 

			if (responseCode == HttpURLConnection.HTTP_OK) { 
				// Use the XML Pull Parser to extract each nearby location.
				// TODO Replace the XML parsing to extract your own place list.
				InputStream in = httpConnection.getInputStream();     
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();

				xpp.setInput(in, null);
				int eventType = xpp.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("result")) {
						eventType = xpp.next();
						String id = "";
						String name = "";
						String vicinity = "";
						String types = "";
						String locationLat = "";
						String locationLng = "";
						String viewport = "";
						String icon = "";
						String reference = "";
						
						while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals("result"))) {
							if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("name"))
								name = xpp.nextText();
							else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("vicinity"))
								vicinity = xpp.nextText();
							else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("type"))
								types = types == "" ? xpp.nextText() : types + " " + xpp.nextText();
								else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("lat"))
									locationLat = xpp.nextText();
								else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("lng"))
									locationLng = xpp.nextText();
								else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("icon"))
									icon = xpp.nextText();
								else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("reference"))
									reference = xpp.nextText();
								else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("id"))
									id = xpp.nextText();
							eventType = xpp.next();
						}
						Location placeLocation = new Location(Config.PlacesConstants.CONSTRUCTED_LOCATION_PROVIDER);
						placeLocation.setLatitude(Double.valueOf(locationLat));
						placeLocation.setLongitude(Double.valueOf(locationLng));
           
						// Add each new place to the Places Content Provider
						addPlace(location, id, name, vicinity, types, placeLocation, viewport, icon, reference, currentTime);
					}
					eventType = xpp.next();
				}
        
				// Remove places from the PlacesContentProviderlist that aren't from this updte.
				String where = PlaceDetailsContentProvider.KEY_LAST_UPDATE_TIME + " < " + currentTime; 
				contentResolver.delete(PlacesContentProvider.CONTENT_URI, where, null);
       
				// Save the last update time and place to the Shared Preferences.
				prefsEditor.putLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, (long) location.getLatitude());
				prefsEditor.putLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, (long) location.getLongitude());
				prefsEditor.putLong(Config.PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, System.currentTimeMillis());      
				prefsEditor.commit();
			}
			else {
				if(E)Log.e(TAG, responseCode + ": " + httpConnection.getResponseMessage());
			}
      
		} 
		catch (MalformedURLException e) {
			if(E)Log.e(TAG, e.getMessage());
		} 
		catch (IOException e) {
			if(E)Log.e(TAG, e.getMessage());
		} 
		catch (XmlPullParserException e) {
			if(E)Log.e(TAG, e.getMessage());
		}
		finally {
		}
  }
  
	/**
	 * Adds the new place to the {@link PlacesContentProvider} using the values passed in.
	 * TODO Update this method to accept and persist the place information your service provides.
	 * @param currentLocation Current location
	 * @param id Unique identifier
	 * @param name Name
	 * @param vicinity Vicinity
	 * @param types Types
	 * @param location Location
	 * @param viewport Viewport
	 * @param icon Icon
	 * @param reference Reference
	 * @param currentTime Current time
	 * @return Successfully added
	 */
	protected boolean addPlace(Location currentLocation, String id, String name, String vicinity, String types, Location location, String viewport, String icon, String reference, long currentTime) {
		// Contruct the Content Values
		ContentValues values = new ContentValues();
		values.put(PlacesContentProvider.KEY_ID, id);  
		values.put(PlacesContentProvider.KEY_NAME, name);
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		values.put(PlacesContentProvider.KEY_LOCATION_LAT, lat);
		values.put(PlacesContentProvider.KEY_LOCATION_LNG, lng);
		values.put(PlacesContentProvider.KEY_VICINITY, vicinity);
		values.put(PlacesContentProvider.KEY_TYPES, types);
		values.put(PlacesContentProvider.KEY_VIEWPORT, viewport);
		values.put(PlacesContentProvider.KEY_ICON, icon);
		values.put(PlacesContentProvider.KEY_REFERENCE, reference);
		values.put(PlacesContentProvider.KEY_LAST_UPDATE_TIME, currentTime);

		// Calculate the distance between the current location and the venue's location
		float distance = 0;
		if (currentLocation != null && location != null)
			distance = currentLocation.distanceTo(location);
		values.put(PlacesContentProvider.KEY_DISTANCE, distance);  
    
		// Update or add the new place to the PlacesContentProvider
		String where = PlacesContentProvider.KEY_ID + " = '" + id + "'";
		boolean result = false;
		try {
			if (contentResolver.update(PlacesContentProvider.CONTENT_URI, values, where, null) == 0) {
				if (contentResolver.insert(PlacesContentProvider.CONTENT_URI, values) != null) {
					result = true;
				}
			}
			else
			{
				result = true;
			}
		}
		catch (Exception ex) { 
			if(E)Log.e("PLACES", "Adding " + name + " failed.");
		}
    
		// If we haven't yet reached our prefetching limit, and we're either
		// on WiFi or don't have a WiFi-only prefetching restriction, and we
		// either don't have low batter or don't have a low battery prefetching 
		// restriction, then prefetch the details for this newly added place.
		if ((prefetchCount < Config.PlacesConstants.PREFETCH_LIMIT) &&
				(!Config.PlacesConstants.PREFETCH_ON_WIFI_ONLY || !mobileData) &&
				(!Config.PlacesConstants.DISABLE_PREFETCH_ON_LOW_BATTERY || !lowBattery)) {
			prefetchCount++;
      
			// Start the PlaceDetailsUpdateService to prefetch the details for this place.
			// As we're prefetching, don't force the refresh if we already have data.
			Intent updateServiceIntent = new Intent(this, PlaceDetailsUpdateService.class);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_REFERENCE, reference);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_ID, id);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, false);
			startService(updateServiceIntent);
		}
    
		return result;
	}
  
	/**
	 * Remove stale place detail records unless we've set the persistent cache flag to true.
	 * 	This is typically the case where a place has actually been viewed rather than prefetched. 
   	 * @param location Location
   	 * @param radius Radius
   	 */
	protected void removeOldLocations(Location location, int radius) {
		// Stale Detail Pages
		long minTime = System.currentTimeMillis()-Config.PlacesConstants.MAX_DETAILS_UPDATE_LATENCY;
		String where = PlaceDetailsContentProvider.KEY_LAST_UPDATE_TIME + " < " + minTime + " AND " +
                   PlaceDetailsContentProvider.KEY_FORCE_CACHE + " = 0";
		contentResolver.delete(PlaceDetailsContentProvider.CONTENT_URI, where, null);
	}
}