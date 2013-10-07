package com.hyperfine.hfgame.UI;

import java.text.SimpleDateFormat;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.PlacesApplication;
import com.hyperfine.hfgame.R;
import com.hyperfine.hfgame.SDK.UserAPI;
import com.hyperfine.hfgame.receivers.LocationChangedReceiver;
import com.hyperfine.hfgame.receivers.PassiveLocationChangedReceiver;
import com.hyperfine.hfgame.services.PlacesUpdateService;
import com.hyperfine.hfgame.utils.LastLocationFinder;
import com.hyperfine.hfgame.utils.LocationUpdateRequester;
import com.hyperfine.hfgame.utils.RESTHelper.RESTHelperListener;
import com.hyperfine.hfgame.utils.SharedPreferenceSaver;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

/**
 * Main application Activity. Used for all application UI, including pre/post Honeycomb 
 * and tablet / phone variations using different layouts. The {@link PlaceDetailFragment},
 * {@link PlaceListFragment}, and {@link CheckinFragment} are all managed from within this Activity.
 * 
 * Because the list, detail, and checkin functionality is encapsulated within their respective
 * Fragments, this Activity serves as a container for each fragment, and the coordinator of their
 * interaction. This Activity also manages the menu, listens for checkins, and manages location updates when the 
 * Application is active.
 * 
 * TODO Update the menu and functionality to support your enhanced UI
 * TODO and service-specific features and requirements
 */
public class PlaceActivity extends FragmentActivity {

	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_UI";

	// TODO (RETO) Add "refreshing" icons when stuff is blank or refreshing.

	protected PackageManager m_packageManager;
	protected NotificationManager m_notificationManager;
	protected LocationManager m_locationManager;
  
	// protected boolean followLocationChanges = true;
	protected SharedPreferences m_prefs;
	protected Editor m_prefsEditor;
	protected SharedPreferenceSaver m_sharedPreferenceSaver;

	protected Criteria m_criteria;
	protected LastLocationFinder m_lastLocationFinder;
	protected LocationUpdateRequester m_locationUpdateRequester;
	protected PendingIntent m_locationListenerPendingIntent;
	protected PendingIntent m_locationListenerPassivePendingIntent;
 
	protected TextView m_numCallsText;
	protected TextView m_numSavedText;
	protected TextView m_longitudeText;
	protected TextView m_latitudeText;
	protected TextView m_altitudeText;
	protected TextView m_accuracyText;
	protected TextView m_dateText;
	
	protected double m_currentLongitude = 0.0;
	protected double m_currentLatitude = 0.0;
	protected double m_currentAltitude = 0.0;
	protected float m_currentAccuracy = 0;
	protected long m_currentTime = 0;
	
	protected LocationUpdateReceiver m_locationUpdateReceiver;
	protected LocationSavedReceiver m_locationSavedReceiver;
	
	// For API testing...
	private String m_userId = null;
	private String m_userName = "test1@test.com";
	private String m_password = "password";

  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(D)Log.d(TAG, "PlaceActivity.onCreate");
			
		super.onCreate(savedInstanceState);
    
		// Inflate the layout
		setContentView(R.layout.main);
		
		m_numCallsText = (TextView)findViewById(R.id.main_numcalls_text);
		m_numSavedText = (TextView)findViewById(R.id.main_saved_text);
		m_longitudeText = (TextView)findViewById(R.id.main_longitude_text);
		m_latitudeText = (TextView)findViewById(R.id.main_latitude_text);
		m_altitudeText = (TextView)findViewById(R.id.main_altitude_text);
		m_accuracyText = (TextView)findViewById(R.id.main_accuracy_text);
		m_dateText = (TextView)findViewById(R.id.main_date_text);
    
		// Get references to the managers
		m_packageManager = getPackageManager();
		m_notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		m_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    
		// Get a reference to the Shared Preferences and a Shared Preference Editor.
		m_prefs = getSharedPreferences(Config.PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
		m_prefsEditor = m_prefs.edit();  
    
		// Instantiate a SharedPreferenceSaver class based on the available platform version.
		// This will be used to save shared preferences
		m_sharedPreferenceSaver = new SharedPreferenceSaver(this);
           
		// Save that we've been run once.
		m_prefsEditor.putBoolean(Config.PlacesConstants.SP_KEY_RUN_ONCE, true);
		m_sharedPreferenceSaver.savePreferences(m_prefsEditor, false);
    
		// Specify the Criteria to use when requesting location updates while the application is Active
		m_criteria = new Criteria();
		if (Config.PlacesConstants.USE_GPS_WHEN_ACTIVITY_VISIBLE) {
			m_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		}
		else {
			m_criteria.setPowerRequirement(Criteria.POWER_LOW);
		}
    
		// Setup the location update Pending Intents
		Intent activeIntent = new Intent(this, LocationChangedReceiver.class);
		m_locationListenerPendingIntent = PendingIntent.getBroadcast(this, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent passiveIntent = new Intent(this, PassiveLocationChangedReceiver.class);
		m_locationListenerPassivePendingIntent = PendingIntent.getBroadcast(this, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Instantiate a LastLocationFinder class.
		// This will be used to find the last known location when the application starts.
		m_lastLocationFinder = new LastLocationFinder(this);
		m_lastLocationFinder.setChangedLocationListener(oneShotLastLocationUpdateListener);
    
		// Instantiate a Location Update Requester class based on the available platform version.
		// This will be used to request location updates.
		m_locationUpdateRequester = new LocationUpdateRequester(m_locationManager);
        
		PlacesApplication app = (PlacesApplication)getApplication();
		m_userId = app.getUserId();
		if (m_userId == null) {
			if(D)Log.d(TAG, "PlaceActivity.onCreate - m_userId is null, so logging in");
			UserAPI.login(m_userName, m_password, new RESTHelperListener() {
				public void onRESTResponse(int httpResult, String responseString) {
					if(D)Log.d(TAG, String.format("PlaceActivity.UserAPI.login: httpResult=%d, response=%s", httpResult, responseString));
					if (httpResult == 200) {
						try {
							JSONObject json = new JSONObject(responseString);
							m_userId = json.getString("id");
							PlacesApplication app = (PlacesApplication)getApplication();
							app.setUserId(m_userId);
							
							if(D)Log.d(TAG, String.format("PlaceActivity.UserAPI.login: m_userId is now set to %s", m_userId));
						}
						catch (Exception e) {
							if(E)Log.e(TAG, "PlaceActivity.UserAPI.login", e);
							e.printStackTrace();
						}
						catch (OutOfMemoryError e) {
							if(E)Log.e(TAG, "PlaceActivity.UserAPI.login", e);
							e.printStackTrace();
						}
					}
				}			
			});
		}

		m_locationUpdateReceiver = new LocationUpdateReceiver();
		m_locationSavedReceiver = new LocationSavedReceiver();
	}

	@Override
	protected void onResume() {
		if(D)Log.d(TAG, "PlaceActivity.onResume");
		
		super.onResume();
    		
		updateLocationMetricsUI();
    
		// Get the last known location (and optionally request location updates) and
		// update the place list.
		boolean followLocationChanges = m_prefs.getBoolean(Config.PlacesConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, true);
		getLocationAndUpdatePlaces(followLocationChanges);
		
		// Register the broadcast receiver for location retrieved and stored notifications
		IntentFilter filter = new IntentFilter(PlacesUpdateService.LOCATION_RETRIEVED_BROADCAST);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(m_locationUpdateReceiver, filter);
		registerReceiver(m_locationSavedReceiver, filter);
	}
  
	@Override
	protected void onPause() {
		if(D)Log.d(TAG, "PlaceActivity.onPause");
		
		// Stop listening for location updates when the Activity is inactive.
		disableLocationUpdates();
		
		unregisterReceiver(m_locationUpdateReceiver);
		unregisterReceiver(m_locationSavedReceiver);
    
		super.onPause();
	}

	/**
	 * Find the last known location (using a {@link LastLocationFinder}) and updates the
	 * place list accordingly.
	 * @param updateWhenLocationChanges Request location updates
	 */
	protected void getLocationAndUpdatePlaces(boolean updateWhenLocationChanges) {
		
		if(D)Log.d(TAG, String.format("PlaceActivity.getLocationAndUpdatePlaces - updateWhenLocationChanges=%b", updateWhenLocationChanges));
		
		// This isn't directly affecting the UI, so put it on a worker thread.
		AsyncTask<Void, Void, Void> findLastLocationTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				
				if(D)Log.d(TAG, "PlaceActivity.findLastLocationTask.doInBackground");
				
				// Find the last known location, specifying a required accuracy of within the min distance between updates
				// and a required latency of the minimum time required between updates.
				Location lastKnownLocation = m_lastLocationFinder.getLastBestLocation(Config.PlacesConstants.MAX_DISTANCE, 
						System.currentTimeMillis()-Config.PlacesConstants.MAX_TIME);
        
				// Update the place list based on the last known location within a defined radius.
				// Note that this is *not* a forced update. The Place List Service has settings to
				// determine how frequently the underlying web service should be pinged. This function  
				// is called everytime the Activity becomes active, so we don't want to flood the server
				// unless the location has changed or a minimum latency or distance has been covered.
				// TODO Modify the search radius based on user settings?
				updatePlaces(lastKnownLocation, Config.PlacesConstants.DEFAULT_RADIUS, false);
				return null;
			}
		};
		findLastLocationTask.execute();
    
		// If we have requested location updates, turn them on here.
		toggleUpdatesWhenLocationChanges(updateWhenLocationChanges);
	}

	/**
	 * Choose if we should receive location updates. 
	 * @param updateWhenLocationChanges Request location updates
	 */
	protected void toggleUpdatesWhenLocationChanges(boolean updateWhenLocationChanges) {
		if(D)Log.d(TAG, String.format("PlaceActivity.toggleUpdatesWhenLocationChanges: updateWhenLocationChanges=%b", updateWhenLocationChanges));
		
		// Save the location update status in shared preferences
		m_prefsEditor.putBoolean(Config.PlacesConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, updateWhenLocationChanges);
		m_sharedPreferenceSaver.savePreferences(m_prefsEditor, true);

		// Start or stop listening for location changes
		if (updateWhenLocationChanges)
			requestLocationUpdates();
		else 
			disableLocationUpdates();
	}
  
	/**
	 * Start listening for location updates.
	 */
	protected void requestLocationUpdates() {
		if(D)Log.d(TAG, "PlaceActivity.requestLocationUpdates");
		
		// Normal updates while activity is visible.
		m_locationUpdateRequester.requestLocationUpdates(Config.PlacesConstants.MAX_TIME, Config.PlacesConstants.MAX_DISTANCE, m_criteria, m_locationListenerPendingIntent);

		// Passive location updates from 3rd party apps when the Activity isn't visible.
		m_locationUpdateRequester.requestPassiveLocationUpdates(Config.PlacesConstants.PASSIVE_MAX_TIME, Config.PlacesConstants.PASSIVE_MAX_DISTANCE, m_locationListenerPassivePendingIntent);
    
		// Register a receiver that listens for when the provider I'm using has been disabled. 
		IntentFilter intentFilter = new IntentFilter(Config.PlacesConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
		registerReceiver(locProviderDisabledReceiver, intentFilter);

		// Register a receiver that listens for when a better provider than I'm using becomes available.
		String bestProvider = m_locationManager.getBestProvider(m_criteria, false);
		String bestAvailableProvider = m_locationManager.getBestProvider(m_criteria, true);
		if (bestProvider != null && !bestProvider.equals(bestAvailableProvider)) {
			m_locationManager.requestLocationUpdates(bestProvider, 0, 0, bestInactiveLocationProviderListener, getMainLooper());
		}
	}
  
	/**
	 * Stop listening for location updates
	 */
	@SuppressWarnings("unused")
	protected void disableLocationUpdates() {
		if(D)Log.d(TAG, "PlaceActivity.disableLocationUpdates");
		
		unregisterReceiver(locProviderDisabledReceiver);
		m_locationManager.removeUpdates(m_locationListenerPendingIntent);
		m_locationManager.removeUpdates(bestInactiveLocationProviderListener);
		if (isFinishing())
			m_lastLocationFinder.cancel();
		if (Config.PlacesConstants.DISABLE_PASSIVE_LOCATION_WHEN_USER_EXIT && isFinishing())
			m_locationManager.removeUpdates(m_locationListenerPassivePendingIntent);
	}
  
	/**
	 * One-off location listener that receives updates from the {@link LastLocationFinder}.
	 * This is triggered where the last known location is outside the bounds of our maximum
	 * distance and latency.
	 */
	protected LocationListener oneShotLastLocationUpdateListener = new LocationListener() {
		public void onLocationChanged(Location l) {
			if(D)Log.d(TAG, String.format(
					"PlaceActivity.oneShotLastLocationUpdateListener.onLocationChanged: long=%f, lat=%f, alt=%f, acc=%f",
					l.getLongitude(), l.getLatitude(), l.getAltitude(), l.getAccuracy()));
			
			updatePlaces(l, Config.PlacesConstants.DEFAULT_RADIUS, true);
		}
   
		public void onProviderDisabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
	};
  
	/**
	 * If the best Location Provider (usually GPS) is not available when we request location
	 * updates, this listener will be notified if / when it becomes available. It calls 
	 * requestLocationUpdates to re-register the location listeners using the better Location
	 * Provider.
	 */
	protected LocationListener bestInactiveLocationProviderListener = new LocationListener() {		
		public void onLocationChanged(Location l) {
			if(D)Log.d(TAG, String.format(
					"PlaceActivity.bestInactiveLocationProviderListener.onLocationChanged: long=%f, lat=%f, alt=%f, acc=%f",
					l.getLongitude(), l.getLatitude(), l.getAltitude(), l.getAccuracy()));			
		}
		public void onProviderDisabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {
			if(D)Log.d(TAG, "PlaceActivity.bestInactiveLocationProviderListener.onProviderEnabled");
			
			// Re-register the location listeners using the better Location Provider.
			requestLocationUpdates();
		}
	};
  
	/**
	 * If the Location Provider we're using to receive location updates is disabled while the 
	 * app is running, this Receiver will be notified, allowing us to re-register our Location
	 * Receivers using the best available Location Provider is still available.
	 */
	protected BroadcastReceiver locProviderDisabledReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(D)Log.d(TAG, "PlaceActivity.locProviderDisabledReceiver.onReceive");
			
			boolean providerDisabled = !intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
			// Re-register the location listeners using the best available Location Provider.
			if (providerDisabled)
				requestLocationUpdates();
		}
	};
  
	/**
	 * Update the list of nearby places centered on the specified Location, within the specified radius.
	 * This will start the {@link PlacesUpdateService} that will poll the underlying web service.
	 * @param location Location
	 * @param radius Radius (meters)
	 * @param forceRefresh Force Refresh
	 */
	protected void updatePlaces(Location location, int radius, boolean forceRefresh) {
		if (location != null) {
			if(D)Log.d(TAG, "PlaceActivity.updatePlaces - Updating place list.");
			// Start the PlacesUpdateService. Note that we use an action rather than specifying the 
			// class directly. That's because we have different variations of the Service for different
			// platform versions.
			Intent updateServiceIntent = new Intent(this, PlacesUpdateService.class);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_LOCATION, location);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_RADIUS, radius);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, forceRefresh);
			startService(updateServiceIntent);
		}
		else {
			if(D)Log.d(TAG, "PlaceActivity.updatePlaces - Updating place list for: No Previous Location Found");
		}
	}
	
	@SuppressLint("SimpleDateFormat") 
	protected void updateLocationMetricsUI() {
		if(D)Log.d(TAG, "PlaceActivity.updateLocationMetricsUI");
		
		m_numCallsText.setText(String.valueOf(((PlacesApplication)getApplication()).getNumUserLocationCalls()));
		m_numSavedText.setText(String.valueOf(((PlacesApplication)getApplication()).getNumUserLocationSaved()));
		m_longitudeText.setText(String.valueOf(m_currentLongitude));
		m_latitudeText.setText(String.valueOf(m_currentLatitude));
		m_altitudeText.setText(String.valueOf(m_currentAltitude));
		m_accuracyText.setText(String.valueOf(m_currentAccuracy));
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = df.format(m_currentTime);
		m_dateText.setText(dateString);
	}
	
	public class LocationUpdateReceiver extends BroadcastReceiver {
		
		@SuppressLint("SimpleDateFormat") @Override
		public void onReceive(Context context, Intent intent) {
			m_currentLongitude = intent.getDoubleExtra("longitude", 0.0);
			m_currentLatitude = intent.getDoubleExtra("latitude", 0.0);
			m_currentAltitude = intent.getDoubleExtra("altitude", 0.0);
			m_currentAccuracy = intent.getFloatExtra("accuracy", 0);
			m_currentTime = intent.getLongExtra("date", 0);
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = df.format(m_currentTime);
			
			if(D)Log.d(TAG, String.format(
					"PlaceActivity.LocationUpdateReceiver.onReceive: lng=%f, lat=%f, alt=%f, acc=%f, time=%s",
					m_currentLongitude, m_currentLatitude, m_currentAltitude, m_currentAccuracy, dateString));
			
			PlacesApplication app = (PlacesApplication)getApplication();
			int nCalls = app.getNumUserLocationCalls();
			nCalls++;
			app.setNumUserLocationCalls(nCalls);

			updateLocationMetricsUI();
		}		
	}
	
	public class LocationSavedReceiver extends BroadcastReceiver {
		
		public void onReceive(Context context, Intent intent) {
			PlacesApplication app = (PlacesApplication)getApplication();
			int nCalls = app.getNumUserLocationSaved();
			nCalls++;
			app.setNumUserLocationSaved(nCalls);
			
			if(D)Log.d(TAG, String.format("PlaceActivity.LocationSavedReceiver.onReceive - numSaved = %d", nCalls));
			
			m_numSavedText.setText(String.valueOf(((PlacesApplication)getApplication()).getNumUserLocationSaved()));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(D)Log.d(TAG, "PlaceActivity.onCreateOptionsMenu");
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
  
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(D)Log.d(TAG, "PlaceActivity.onOptionsItemSelected");
		
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			// Request the last known location.
			Location lastKnownLocation = m_lastLocationFinder.getLastBestLocation(Config.PlacesConstants.MAX_DISTANCE, 
    	            System.currentTimeMillis()-Config.PlacesConstants.MAX_TIME);
			// Force an update
			updatePlaces(lastKnownLocation, Config.PlacesConstants.DEFAULT_RADIUS, true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}