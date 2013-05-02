package com.hyperfine.hfgame.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

public class RESTHelper {
	
	public final static String TAG = "HFGame";
	
	public final static int READ_TIMEOUT = 10000;
	public final static int CONNECT_TIMEOUT = 10000;
	
	private ArrayList<RESTHelperListener> m_restHelperListeners = new ArrayList<RESTHelperListener>();
	
	public interface RESTHelperListener {
		public void onRESTResponse(String stringResponse);
	}
	
	public void registerRESTHelperListener(RESTHelperListener listener) {
		if(D)Log.d(TAG, "RESTHelper.registerRESTHelperListener");
		
		if (!m_restHelperListeners.contains(listener)) {
			m_restHelperListeners.add(listener);
		}
		if(D)Log.d(TAG, String.format("RESTHelper.registerRESTHelperListener: now have %d listeners", m_restHelperListeners.size()));		
	}
	
	public void unregisterRESTHelperListener(RESTHelperListener listener) {
		if(D)Log.d(TAG, "RESTHelper.unregisterRESTHelperListener");
		
		if (m_restHelperListeners.contains(listener)) {
			m_restHelperListeners.remove(listener);
		}		
		if(D)Log.d(TAG, String.format("RESTHelper.unregisterRESTHelperListener: now have %d listeners", m_restHelperListeners.size()));
	}
	
	public enum HttpVerb {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	private class RESTCallTaskParams {
		public RESTCallTaskParams(HttpVerb verb, String url, JSONObject parameters) {
			m_verb = verb;
			m_url = url;
			m_parameters = parameters;
		}
		
		public HttpVerb m_verb;
		public String m_url;
		public JSONObject m_parameters;
	}
	
	private String CallHttpGet(RESTCallTaskParams rctp) {
		OutputStream os = null;
		InputStream is = null;
		BufferedReader br = null;
		HttpURLConnection conn = null;
		URL url;
		String response = "";
		int httpResult;

		if(D)Log.d(TAG, String.format("RESTHelper.CallHttpGet: url=%s, params=%s", rctp.m_url, rctp.m_parameters == null ? "{}" : rctp.m_parameters.toString()));

		try {
			StringBuilder sbQuery = new StringBuilder();
			
			JSONObject params = rctp.m_parameters;
			boolean isFirst = true;
			
			if (params != null && params.length() > 0) {
				Iterator<?> keys = params.keys();
				while (keys.hasNext()) {
					String key = (String)keys.next();
					String value = params.getString(key);
					
					sbQuery.append(isFirst ? "?" : "&");
					sbQuery.append(String.format("%s=%s", key, value));
				}
			}
			url = new URL(rctp.m_url + sbQuery.toString());
			
			conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			is = conn.getInputStream();
	
			httpResult = conn.getResponseCode();
			if(D)Log.d(TAG, String.format("CallHttpGet - httpResult = %d", httpResult));
	
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(is.available());
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			response = sb.toString();
		}
		catch (Exception e) {
			if(E)Log.e(TAG, "RESTHelper.CallHttpGet", e);
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(E)Log.e(TAG, "RESTHelper.CallHttpGet", e);
			e.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (Exception ex) {}
			}
			if (os != null) {
				try {
					os.close();
				}
				catch (Exception ex) {}
			}
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception ex) {}
			}
			if (conn != null) {					
				conn.disconnect();
			}
		}
		
		return response;		
	}
	
	private String CallHttpPost(RESTCallTaskParams rctp) {
		OutputStream os = null;
		InputStream is = null;
		BufferedReader br = null;
		HttpURLConnection conn = null;
		URL url;
		String response = "";
		int httpResult;
		
		if(D)Log.d(TAG, String.format("RESTHelper.CallHttpPost: url=%s, params=%s", rctp.m_url, rctp.m_parameters == null ? "{}" : rctp.m_parameters.toString()));
		
		try {
			String jsonParams = rctp.m_parameters == null ? "{}" : rctp.m_parameters.toString();
			
			url = new URL(rctp.m_url);
			
			conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			
			conn.setRequestMethod("POST");
			conn.setDoInput(true);	
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(jsonParams.getBytes().length);
			
			conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			
			conn.connect();
			
			os = new BufferedOutputStream(conn.getOutputStream());
			os.write(jsonParams.getBytes());
			os.flush();
			
			httpResult = conn.getResponseCode();
			if(D)Log.d(TAG, String.format("RESTHelper - httpResult = %d", httpResult));
			
			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(is.available());
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			response = sb.toString();
		}
		catch (Exception e) {
			if(E)Log.e(TAG, "RESTHelper.CallHttpPost", e);
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(E)Log.e(TAG, "RESTHelper.CallHttpPost", e);
			e.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (Exception ex) {}
			}
			if (os != null) {
				try {
					os.close();
				}
				catch (Exception ex) {}
			}
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception ex) {}
			}
			if (conn != null) {					
				conn.disconnect();
			}
		}
		
		return response;
	}

	private class RESTCallTask extends AsyncTask<Object, Void, String> {

		@Override
		protected String doInBackground(Object... params) {
			if(D)Log.d(TAG, "RESTHelper.RESTCallTask.doInBackground");
			
			RESTCallTaskParams rctp = (RESTCallTaskParams)params[0];
			
			String response = null;
			
			switch (rctp.m_verb){
				case GET:
					response = CallHttpGet(rctp);
					break;
				case PUT:
					break;
				case POST:
					response = CallHttpPost(rctp);
					break;
				case DELETE:
					break;
				default:
					break;
			}
			
			return response;
		}
		
		@Override
		protected void onPostExecute(String responseString) {
			if(D)Log.d(TAG, String.format("RESTHelper.RESTCallTask.onPostExecute: responseString=%s", responseString));
			
			// Clone the arraylist so that mods on the array list due to actions in the callback to
			// not result in a ConcurrentModificationException
			ArrayList<RESTHelperListener> restHelperListeners = new ArrayList<RESTHelperListener>(RESTHelper.this.m_restHelperListeners);
			
			for (RESTHelperListener rhl : restHelperListeners) {
				rhl.onRESTResponse(responseString);
			}
			
			return;
		}
	}
	
	public void restCallAsync(HttpVerb verb, String url, JSONObject parameters) {
		if(D)Log.d(TAG, String.format("RESTHelper.restCallAsync: verb=%s, url=%s, parameters=%s", verb.toString(), url, parameters == null ? "{}" : parameters.toString()));
		
		RESTCallTaskParams rctp = new RESTCallTaskParams(verb, url, parameters);
		
		try {
			new RESTCallTask().execute(rctp);
		}
		catch (Exception e) {
			if(E)Log.e(TAG, "RESTHelper.restCallAsync", e);
			e.printStackTrace();
		}
		catch (OutOfMemoryError e) {
			if(E)Log.e(TAG, "RESTHelper.restCallAsync", e);
			e.printStackTrace();			
		}
	}
}
