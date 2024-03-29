package com.hyperfine.hfgame.receivers;

import com.hyperfine.hfgame.utils.Config;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;

/**
 * The manifest Receiver is used to detect changes in battery state. 
 * When the system broadcasts a "Battery Low" warning we turn off
 * the passive location updates to conserve battery when the app is
 * in the background. 
 * 
 * When the system broadcasts "Battery OK" to indicate the battery
 * has returned to an okay state, the passive location updates are 
 * resumed.
 */
public class PowerStateChangedReceiver extends BroadcastReceiver {

	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_Receivers";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(D)Log.d(TAG, "PowerStateChangedReceiver.onReceive");
		
		boolean batteryLow = intent.getAction().equals(Intent.ACTION_BATTERY_LOW);
		
		if(D)Log.d(TAG, String.format("... battery is%s low", batteryLow ? "" : " NOT"));
 
		PackageManager pm = context.getPackageManager();
		ComponentName passiveLocationReceiver = new ComponentName(context, PassiveLocationChangedReceiver.class);
    
		// Disable the passive location update receiver when the battery state is low.
		// Disabling the Receiver will prevent the app from initiating the background
		// downloads of nearby locations.
		pm.setComponentEnabledSetting(passiveLocationReceiver,
				batteryLow ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 
				PackageManager.DONT_KILL_APP);
	}
}