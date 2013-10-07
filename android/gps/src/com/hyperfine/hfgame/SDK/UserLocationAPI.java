package com.hyperfine.hfgame.SDK;

import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

import com.hyperfine.hfgame.utils.RESTHelper;
import com.hyperfine.hfgame.utils.Config;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

public class UserLocationAPI {

	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_SDK";

	@SuppressLint("SimpleDateFormat") 
	public static void createLocation(
			String userId, double longitude, double latitude, double altitude, 
			double accuracy, long utcTimeInMillisecondsSince1970, RESTHelper.RESTHelperListener listener) {
		
		SimpleDateFormat iso8601DateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String isoDateString = iso8601DateTimeFormat.format(utcTimeInMillisecondsSince1970);

		if(D)Log.d(TAG, String.format(
				"UserLocationAPI.createLocation: userId=%s, longitude=%f, latitude=%f, altitude=%f, accuracy=%f, date=%s",
				userId, longitude, latitude, altitude, accuracy, isoDateString));
		
		RESTHelper restHelper = new RESTHelper();
		
		String url = Config.restBaseUrl + "userlocations/createlocation";
		
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userId", userId);
			
			JSONArray coordinates = new JSONArray();
			coordinates.put(0, longitude);
			coordinates.put(1, latitude);		
			jsonObject.put("coordinates", coordinates);
			
			jsonObject.put("altitude", altitude);
			jsonObject.put("accuracy", accuracy);
			
			jsonObject.put("date", isoDateString);
			
			restHelper.restCallAsync(RESTHelper.HttpVerb.POST, url, jsonObject, listener);
			return;
		}
		catch (Exception e) {
			if(E)Log.e(TAG, "UserLocationAPI.createLocation", e);
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(E)Log.e(TAG, "UserLocationAPI.CreateLocation", e);
			e.printStackTrace();
		}
		
		if (listener != null) {
			listener.onRESTResponse(500, "internal error");
		}
	}
}
