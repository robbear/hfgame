package com.hyperfine.hfgame;

import android.app.Application;
import android.util.Log;

import com.hyperfine.hfgame.utils.AppStrictMode;
import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;

public class PlacesApplication extends Application {
	
	public final static String TAG = "HFGame";
	
	private String m_userId = null;
  
	// TODO Insert your Google Places API into MY_API_KEY in PlacesConstants.java
	// TODO Insert your Backup Manager API into res/values/strings.xml : backup_manager_key
  
	@Override
	public final void onCreate() {
		if(D)Log.d(TAG, "PlacesApplication.onCreate");

		super.onCreate();
    
		if (Config.PlacesConstants.DEVELOPER_MODE) {
			AppStrictMode strictMode = new AppStrictMode();
			if (strictMode != null) {
				if(D)Log.d(TAG, "PlacesApplication.onCreate - enabling strict mode");
				strictMode.enableStrictMode();
			}
		}
	}
	
	public String getUserId() {
		return m_userId;
	}
	
	public void setUserId(String userId) {
		m_userId = userId;
	}
}