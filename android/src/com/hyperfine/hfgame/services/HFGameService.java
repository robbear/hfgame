//
// Copyright (c) 2013 Hyperfine Software Corp.
// All rights reserved
//

package com.hyperfine.hfgame.services;

import com.hyperfine.hfgame.utils.RESTHelper;
import com.hyperfine.hfgame.utils.RESTHelper.RESTHelperListener;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

public class HFGameService extends Service implements RESTHelperListener {
	
	public final static String TAG = "HFGame";
	
	public final static String PREFS = "hfgame";
	public final static String PREFS_API_BASEURL = "prefs_hfgame_api_url";
	
	public final static String DEFAULT_API_BASEURL = "http://hfapi.jit.su/";
	
	private RESTHelper m_restHelper = null;
	private boolean m_fServiceDestroyed = false;
	
	@Override
	public void onCreate() {
		if(D)Log.d(TAG, "HFGameService.onCreate - starting HFGameService");
		
		m_fServiceDestroyed = false;
		m_restHelper = new RESTHelper();
		startTestIntervalAsync();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(D)Log.d(TAG, String.format("HFGameService.onStartCommand: flags=%d, startId=%d", flags, startId));
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		if(D)Log.d(TAG, "HFGameService.onDestroy");
		
		m_fServiceDestroyed = true;
	}
	
	//
	// REST call timer
	//
	private class TestIntervalTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if(D)Log.d(TAG, "HFGameService.TestIntervalTask.doInBackground");
			
			if (m_fServiceDestroyed) {
				if(D)Log.d(TAG, "HFGameService.TestIntervalTask.doInBackground - service destroyed. Bailing.");
				return null;
			}
			
			try {
				Thread.sleep(15000);
			}
			catch (Exception e) {
				if(E)Log.e(TAG, "HFGameService.TestIntervalTask.doInBackground", e);
				e.printStackTrace();
			}
			catch (OutOfMemoryError e) {
				if(E)Log.e(TAG, "HFGameService.TestIntervalTask.doInBackground", e);
				e.printStackTrace();				
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void p) {
			if(D)Log.d(TAG, "HFGameService.TestIntervalTask.onPostExecute");
			
			if (m_fServiceDestroyed) {
				if(D)Log.d(TAG, "HFGameService.TestIntervalTask.onPostExecute - service destroyed. Bailing.");
				return;
			}
			
			// Ensure the last one was removed
			m_restHelper.unregisterRESTHelperListener(HFGameService.this);
			m_restHelper.registerRESTHelperListener(HFGameService.this);
			m_restHelper.restCallAsync();
		}
	}
	
	private void startTestIntervalAsync() {
		if(D)Log.d(TAG, "HFGameService.startTestIntervalAsync");
		
		try {
			new TestIntervalTask().execute();
		}
		catch (Exception e) {
			if(E)Log.e(TAG, "HFGameService.startTestInterval", e);
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(E)Log.e(TAG, "HFGameService.startTestInterval", e);
			e.printStackTrace();			
		}
	}
	
	//
	// RESTHelperListener methods
	//
	
	public void onRESTResponse(String stringResponse) {
		if(D)Log.d(TAG, String.format("HFGameService.onRESTResponse: %s", stringResponse));
		
		m_restHelper.unregisterRESTHelperListener(this);
		
		if (m_fServiceDestroyed) {
			if(D)Log.d(TAG, "HFGameService.OnRESTResponse - service destroyed. Bailing.");
			return;
		}
		
		startTestIntervalAsync();
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
