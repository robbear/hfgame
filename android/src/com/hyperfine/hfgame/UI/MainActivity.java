//
// Copyright (c) 2013 Hyperfine Software Corp.
// All rights reserved
//

package com.hyperfine.hfgame.UI;

import com.hyperfine.hfgame.R;
import com.hyperfine.hfgame.R.layout;
import com.hyperfine.hfgame.R.menu;
import com.hyperfine.hfgame.services.HFGameService;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;

public class MainActivity extends Activity {
	
	public final static String TAG = "HFGame";
	
	public final static String INSTANCE_STATE_ORIENTATION_CHANGED = "orientation_changed";

	private HFGameService m_hfGameService;
	private boolean m_fOrientationChanged = false;
	private SharedPreferences m_prefs = null;
	private int m_orientation;
	
	public ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			if(D)Log.d(TAG, "MainActivity.onServiceConnected");
			
			m_hfGameService = ((HFGameService.HFGameServiceBinder)service).getService();
			
			// Register any HFGameService listeners here
		}
		
		public void onServiceDisconnected(ComponentName className) {
			if(D)Log.d(TAG, "MainActivity.onServiceDisconnected");
			
			m_hfGameService = null;
		}
	};
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if(D)Log.d(TAG, "MainActivity.onSaveInstanceState");
		
		int orientation = getResources().getConfiguration().orientation;
		m_fOrientationChanged = m_orientation != orientation;
		savedInstanceState.putBoolean(INSTANCE_STATE_ORIENTATION_CHANGED, m_fOrientationChanged);
		if(D)Log.d(TAG, String.format("MainActivity.onSaveInstanceState: orientation %s changed", m_fOrientationChanged ? "has" : "has not"));
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if(D)Log.d(TAG, "MainActivity.onCreate");
    	
        super.onCreate(savedInstanceState);
        
        m_prefs = getSharedPreferences(HFGameService.PREFS, Context.MODE_PRIVATE);
        
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState != null) {
        	m_fOrientationChanged = savedInstanceState.getBoolean(INSTANCE_STATE_ORIENTATION_CHANGED, false);
        	if(D)Log.d(TAG, String.format("MainActivity.onCreate: m_fOrientationChanged = %b", m_fOrientationChanged));
        }
        
    }
    
    @Override
    public void onStart() {
    	if(D)Log.d(TAG, "MainActivity.onStart");
    	
    	super.onStart();
    	
    	initializeHFGameService();
    }
    
    @Override
    public void onStop() {
    	if(D)Log.d(TAG, "MainActivity.onStop");
    	
    	super.onStop();
    	
    	uninitializeHFGameService();
    }
    
    @Override
    public void onResume() {
    	if(D)Log.d(TAG, "MainActivity.onResume");
    	
    	m_orientation = getResources().getConfiguration().orientation;
    	if(D)Log.d(TAG, String.format("MainActivity.onResume: orientation = %d", m_orientation));
    	
    	super.onResume();
    	
    	if (m_hfGameService != null) {
    		// Register any HFGameService listeners here
    	}
    }
    
    @Override
    public void onRestart() {
    	if(D)Log.d(TAG, "MainActivity.onRestart");
    	
    	super.onRestart();
    }
    
    @Override
    public void onPause() {
    	if(D)Log.d(TAG, "MainActivity.onPause");
    	
    	super.onPause();
    	
    	if (m_hfGameService != null) {
    		// Unregister any HFGameService listeners here
    	}
    }
    
    private void initializeHFGameService() {
    	if(D)Log.d(TAG, "MainActivity.initializeHFGameService");
    	
    	Intent service = new Intent(this, HFGameService.class);
    	
    	/* NEVER
    	if (m_fOrientationChanged) {
    		if(D)Log.d(TAG, "MainActivity.initializeHFGameService - calling startService due to orientation change");
    		startService(service);
    	}
    	else {
    		// Otherwise, make sure we kill the static background service
    		if(D)Log.d(TAG, "MainActivity.initializeHFGameService - calling stopService in order to simply bind and have the service stop when we exit the app.");
    		stopService(service);
    	}
    	*/
    	
    	m_fOrientationChanged = false;
    	
    	if(D)Log.d(TAG, "MainActivity.initializeHFGameService - calling bindService");
    	
    	// Regardless of orientation change, we're always going to need a bound HFGameService for this activity
    	bindService(service, m_connection, Context.BIND_AUTO_CREATE);
    }
    
    private void uninitializeHFGameService() {
    	if(D)Log.d(TAG, "MainActivity.uninitializeHFGameService");
    	
    	if (m_hfGameService != null) {
    		// Unregister any HFGameService listeners here
    	}
    	
    	if (m_hfGameService != null && m_connection != null && !m_fOrientationChanged) {
    		if(D)Log.d(TAG, "MainActivity.uninitializeHFGameService - calling unbindService");
    		unbindService(m_connection);
    	}
    	
    	if (!m_fOrientationChanged) {
    		if(D)Log.d(TAG, "MainActivity.unintializeHFGameService - calling stopService");
    		Intent service = new Intent(this, HFGameService.class);
    		stopService(service);
    	}
    	
    	m_hfGameService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if(D)Log.d(TAG, "MainActivity.onCreateOptionsMenu");
    	
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
