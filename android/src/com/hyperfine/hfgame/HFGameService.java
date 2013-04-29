//
// Copyright (c) 2013 Hyperfine Software Corp.
// All rights reserved
//

package com.hyperfine.hfgame;

import static com.hyperfine.hfgame.Config.D;
import android.app.Service;
//import static com.hyperfine.hfgame.Config.E;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class HFGameService extends Service {
	
	public final static String TAG = "HFGame";
	
	public final static String PREFS = "hfgame";
	public final static String PREFS_API_BASEURL = "prefs_hfgame_api_url";
	
	public final static String DEFAULT_API_BASEURL = "http://hfapi.jit.su/";
	
	@Override
	public void onCreate() {
		if(D)Log.d(TAG, "HFGameService.onCreate - starting HFGameService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(D)Log.d(TAG, String.format("HFGameService.onStartCommand: flags=%d, startId=%d", flags, startId));
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		if(D)Log.d(TAG, "HFGameService.onDestroy");
	}
	
	//
	// Binder methods
	//
	public class HFGameServiceBinder extends Binder {
		public HFGameService getService() {
			return HFGameService.this;
		}
	}
	
	private final IBinder binder = new HFGameServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		if(D)Log.d(TAG, "HFGameService.onBind");
		
		return binder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		if(D)Log.d(TAG, "HFGameService.onRebind");
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		if(D)Log.d(TAG, "HFGameService.onUnbind");
		
		// Don't use onRebind
		return false;
	}
}
