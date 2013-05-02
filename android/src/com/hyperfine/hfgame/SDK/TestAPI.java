package com.hyperfine.hfgame.SDK;

import android.util.Log;

import com.hyperfine.hfgame.utils.RESTHelper;
import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;

public class TestAPI {
	
	public final static String TAG = "HFGame";
	
	public static void Test(RESTHelper.RESTHelperListener listener) {
		if(D)Log.d(TAG, "TestAPI.Test");
		
		RESTHelper restHelper = new RESTHelper();
		
		String url = Config.restBaseUrl + "test";
		restHelper.restCallAsync(RESTHelper.HttpVerb.GET, url, null, listener);
	}
}
