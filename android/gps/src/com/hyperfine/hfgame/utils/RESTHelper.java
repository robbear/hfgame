package com.hyperfine.hfgame.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;

public class RESTHelper {
	
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_Utils";
	
	public final static int READ_TIMEOUT = 10000;
	public final static int CONNECT_TIMEOUT = 10000;
	
	public interface RESTHelperListener {
		public void onRESTResponse(int httpResult, String responseString);
	}
	
	public enum HttpVerb {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	private class RESTCallTaskParams {
		public RESTCallTaskParams(HttpVerb verb, String url, JSONObject parameters, RESTHelperListener listener) {
			m_verb = verb;
			m_url = url;
			m_parameters = parameters;
			m_listener = listener;
			m_responseString = "{ result: \"error\" }";
			m_httpResult = 500;
		}
		
		public HttpVerb m_verb;
		public String m_url;
		public JSONObject m_parameters;
		public RESTHelperListener m_listener;
		public String m_responseString;
		public int m_httpResult;
	}
	
	private void CallHttpGet(RESTCallTaskParams rctp) {
		OutputStream os = null;
		InputStream is = null;
		BufferedReader br = null;
		HttpURLConnection conn = null;
		URL url;

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
					isFirst = false;
				}
			}
			url = new URL(rctp.m_url + sbQuery.toString());
			
			conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			is = conn.getInputStream();
	
			rctp.m_httpResult = conn.getResponseCode();
			if(D)Log.d(TAG, String.format("CallHttpGet - httpResult = %d", rctp.m_httpResult));
	
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(is.available());
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			rctp.m_responseString = sb.toString();
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
	}
	
	private void CallHttpPost(RESTCallTaskParams rctp) {
		OutputStream os = null;
		InputStream is = null;
		BufferedReader br = null;
		HttpURLConnection conn = null;
		URL url;
		
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
			
			rctp.m_httpResult = conn.getResponseCode();
			if(D)Log.d(TAG, String.format("RESTHelper - httpResult = %d", rctp.m_httpResult));
			
			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(is.available());
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			rctp.m_responseString = sb.toString();
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
	}

	private class RESTCallTask extends AsyncTask<Object, Void, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			if(D)Log.d(TAG, "RESTHelper.RESTCallTask.doInBackground");
			
			RESTCallTaskParams rctp = (RESTCallTaskParams)params[0];
					
			switch (rctp.m_verb){
				case GET:
					CallHttpGet(rctp);
					break;
				case PUT:
					break;
				case POST:
					CallHttpPost(rctp);
					break;
				case DELETE:
					break;
				default:
					break;
			}
			
			return rctp;
		}
		
		@Override
		protected void onPostExecute(Object obj) {
			RESTCallTaskParams rctp = (RESTCallTaskParams)obj;
			
			if(D)Log.d(TAG, String.format("RESTHelper.RESTCallTask.onPostExecute: responseString=%s", rctp.m_responseString));
			
			if (rctp.m_listener != null) {
				rctp.m_listener.onRESTResponse(rctp.m_httpResult, rctp.m_responseString);
			}
			
			return;
		}
	}
	
	public void restCallAsync(HttpVerb verb, String url, JSONObject parameters, RESTHelperListener listener) {
		if(D)Log.d(TAG, String.format("RESTHelper.restCallAsync: verb=%s, url=%s, parameters=%s", verb.toString(), url, parameters == null ? "{}" : parameters.toString()));
		
		RESTCallTaskParams rctp = new RESTCallTaskParams(verb, url, parameters, listener);
		
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
