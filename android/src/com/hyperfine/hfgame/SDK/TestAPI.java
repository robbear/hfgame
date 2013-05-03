package com.hyperfine.hfgame.SDK;

import android.util.Log;

import com.hyperfine.hfgame.utils.RESTHelper;
import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;

public class TestAPI {
	
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_SDK";
	
	public static void test(RESTHelper.RESTHelperListener listener) {
		if(D)Log.d(TAG, "TestAPI.test");
		
		RESTHelper restHelper = new RESTHelper();
		
		String url = Config.restBaseUrl + "test";
		restHelper.restCallAsync(RESTHelper.HttpVerb.GET, url, null, listener);
	}
}
