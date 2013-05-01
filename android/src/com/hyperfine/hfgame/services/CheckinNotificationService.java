package com.hyperfine.hfgame.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.R;
import com.hyperfine.hfgame.UI.PlaceActivity;
import com.hyperfine.hfgame.content_providers.PlaceDetailsContentProvider;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;


/**
 * Service that handles background checkin notifications.
 * This Service will be started by the {@link NewCheckinReceiver}
 * when the Application isn't visible and trigger a Notification
 * telling the user that they have been checked in to a venue.
 * This typically happens if an earlier checkin has failed 
 * (due to lack of connectivity, server error, etc.).
 * 
 * If your app lets users post reviews / ratings / etc. This 
 * Service can be used to notify them once they have been successfully
 * posted.
 * 
 * TODO Update the Notification to display a richer payload.
 * TODO Create a variation of this Notification for Honeycomb+ devices. 
 */
public class CheckinNotificationService extends IntentService {
  
	protected static String TAG = "HFGame";
  
	protected ContentResolver contentResolver;
	protected NotificationManager notificationManager;
	protected String[] projection;
  
	public CheckinNotificationService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		if(D)Log.d(TAG, "CheckinNotificationService.onCreate");
		
		super.onCreate();
		contentResolver = getContentResolver();
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    
		projection = new String[] {PlaceDetailsContentProvider.KEY_ID, PlaceDetailsContentProvider.KEY_NAME};
	}
	
	@Override
	public void onDestroy() {
		if(D)Log.d(TAG, "CheckinNotificationService.onDestroy");
		
		super.onDestroy();
	}

	/**
	 * {@inheritDoc}
	 * Extract the name of the venue based on the ID specified in the broadcast Checkin Intent
	 * and use it to display a Notification. 
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if(D)Log.d(TAG, "CheckinNotificationService.onHandleIntent");
		
		String id = intent.getStringExtra(Config.PlacesConstants.EXTRA_KEY_ID);
    
		// Query the PlaceDetailsContentProvider for the specified venue.
		Uri uri = Uri.withAppendedPath(PlaceDetailsContentProvider.CONTENT_URI, id);
		Cursor cursor = contentResolver.query(uri, projection, null, null, null);
    
		if (cursor.moveToFirst()) {
			// Construct a Pending Intent for the Notification. This will ensure that when
			// the notification is clicked, the application will open and the venue we've
			// checked in to will be displayed.
			Intent contentIntent = new Intent(this, PlaceActivity.class);
			contentIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_ID, id);
			PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      
			// Construct the notification.
			String checkinText = getResources().getText(R.string.checkin_text).toString();
			String placeName = cursor.getString(cursor.getColumnIndex(PlaceDetailsContentProvider.KEY_NAME));
			String tickerText = checkinText + placeName;
			Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
			// BUGBUG
			notification.setLatestEventInfo(this, checkinText, placeName, contentPendingIntent);
      
			// Trigger the notification.
			notificationManager.notify(Config.PlacesConstants.CHECKIN_NOTIFICATION, notification);
		}
	}
}