package com.hyperfine.hfgame.UI.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.UI.PlaceActivity;
import com.hyperfine.hfgame.content_providers.PlacesContentProvider;
import com.hyperfine.hfgame.services.PlaceDetailsUpdateService;

import static com.hyperfine.hfgame.utils.Config.D;
//import static com.hyperfine.hfgame.utils.Config.E;

// TODO Update this UI to show a better list of available venues. This could include
// TODO pictures, direction, more detailed text, etc. You will likely want to define
// TODO your own List Item Layout. 

/**
 * UI Fragment to show a list of venues near to the users current location.
 */
public class PlaceListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_UI";
  
	protected Cursor cursor = null;
	protected SimpleCursorAdapter adapter;
	protected PlaceActivity activity;
  
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if(D)Log.d(TAG, "PlaceListFragment.onActivityCreated");
		
		super.onActivityCreated(savedInstanceState);
        
		activity = (PlaceActivity)getActivity();
    
		// Create a new SimpleCursorAdapter that displays the name of each nearby
		// venue and the current distance to it.
		adapter = new SimpleCursorAdapter(
            activity,
            android.R.layout.two_line_list_item,
            cursor,                                              
            new String[] {PlacesContentProvider.KEY_NAME, PlacesContentProvider.KEY_DISTANCE},           
            new int[] {android.R.id.text1, android.R.id.text2},
            0);
		// Allocate the adapter to the List displayed within this fragment.
		setListAdapter(adapter);
    
		// Populate the adapter / list using a Cursor Loader. 
		getLoaderManager().initLoader(0, null, this);
	}
  
	/**
	 * {@inheritDoc}
	 * When a venue is clicked, fetch the details from your server and display the detail page.
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long theid) {
		if(D)Log.d(TAG, "PlaceListFragment.onListItemClick");
		
		super.onListItemClick(l, v, position, theid);
    
		// Find the ID and Reference of the selected venue.
		// These are needed to perform a lookup in our cache and the Google Places API server respectively.
		Cursor c = adapter.getCursor();
		c.moveToPosition(position);
		String reference = c.getString(c.getColumnIndex(PlacesContentProvider.KEY_REFERENCE));
		String id = c.getString(c.getColumnIndex(PlacesContentProvider.KEY_ID));
    
		// Initiate a lookup of the venue details usign the PlacesDetailsUpdateService.
		// Because this is a user initiated action (rather than a prefetch) we request
		// that the Service force a refresh.
		Intent serviceIntent = new Intent(activity, PlaceDetailsUpdateService.class);
		serviceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_REFERENCE, reference);
		serviceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_ID, id);
		serviceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, true);        
		activity.startService(serviceIntent);
    
		// Request the parent Activity display the venue detail UI.
		activity.selectDetail(reference, id);
	}
     
	/**
	 * {@inheritDoc}
	 * This loader will return the ID, Reference, Name, and Distance of all the venues
	 * currently stored in the {@link PlacesContentProvider}.
	 */
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(D)Log.d(TAG, "PlaceListFragment.onCreateLoader");
		
		String[] projection = new String[] {PlacesContentProvider.KEY_ID,PlacesContentProvider.KEY_NAME, PlacesContentProvider.KEY_DISTANCE, PlacesContentProvider.KEY_REFERENCE};
    
		return new CursorLoader(activity, PlacesContentProvider.CONTENT_URI, 
				projection, null, null, null);
	}

	/**
	 * {@inheritDoc}
	 * When the loading has completed, assign the cursor to the adapter / list.
	 */
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(D)Log.d(TAG, "PlaceListFragment.onLoadFinished");
		
		adapter.swapCursor(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onLoaderReset(Loader<Cursor> loader) {
		if(D)Log.d(TAG, "PlaceListFragment.onLoaderReset");
		
		adapter.swapCursor(null);
	}
}