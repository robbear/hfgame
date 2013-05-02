package com.hyperfine.hfgame.SDK;

import org.json.JSONObject;

import android.util.Log;

import com.hyperfine.hfgame.utils.RESTHelper;
import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

public class UserAPI {

	public final static String TAG = "HFGame";

	public static void login(String userName, String password, RESTHelper.RESTHelperListener listener) {
		if(D)Log.d(TAG, String.format("UserAPI.login: userName=%s", userName));
		
		RESTHelper restHelper = new RESTHelper();
		
		String url = Config.restBaseUrl + "users/login";
		
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", userName);
			jsonObject.put("password", password);
			
			restHelper.restCallAsync(RESTHelper.HttpVerb.GET, url, jsonObject, listener);
			return;
		}
		catch (Exception e) {
			if(E)Log.e(TAG, "UserAPI.login", e);
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(E)Log.e(TAG, "UserAPI.login", e);
			e.printStackTrace();
		}
		
		if (listener != null) {
			listener.onRESTResponse(500, "internal error");
		}
	}
}
