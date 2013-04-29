package com.hyperfine.hfgame;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;
import android.util.Log;

import static com.hyperfine.hfgame.Config.D;
import static com.hyperfine.hfgame.Config.E;

public class RESTHelper {
	
	public final static String TAG = "HFGame";
	
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

	private class RESTCallTask extends AsyncTask<Void, Void, String> {
		private String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
			if(D)Log.d(TAG, "RESTHelper.RESTCallTask.getASCIIContentFromEntity");
			
			InputStream stream = entity.getContent();
			StringBuffer buffer = new StringBuffer();
			
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = stream.read(b);
				if (n > 0) {
					buffer.append(new String(b, 0, n));
				}
			}
			
			return buffer.toString();
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			if(D)Log.d(TAG, "RESTHelper.RESTCallTask.doInBackground");
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet("http://hfapi.jit.su/test");
			
			String text = null;
			
			try {
				HttpResponse response = httpClient.execute(httpGet, localContext);
				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);
			}
			catch (Exception e) {
				if(E)Log.e(TAG, "RESTHelper.RESTCallTast.doInBackground", e);
				e.printStackTrace();
			}
			catch (OutOfMemoryError e) {
				if(E)Log.e(TAG, "RESTHelper.RESTCallTask.doInBackground", e);
				e.printStackTrace();				
			}			

			return text;
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
	
	public void restCallAsync() {
		if(D)Log.d(TAG, "RESTHelper.restCallAsync");
		
		try {
			new RESTCallTask().execute();
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
