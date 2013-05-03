package com.hyperfine.hfgame.UI.fragments;

//TODO Create a richer UI to display places Details. This should include images,
//TODO ratings, reviews, other people checked in here, etc.

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.hyperfine.hfgame.utils.Config;
import com.hyperfine.hfgame.R;
import com.hyperfine.hfgame.content_providers.PlaceDetailsContentProvider;
import com.hyperfine.hfgame.services.PlaceCheckinService;
import com.hyperfine.hfgame.services.PlaceDetailsUpdateService;

import static com.hyperfine.hfgame.utils.Config.D;
import static com.hyperfine.hfgame.utils.Config.E;


/**
* UI Fragment to display the details for a selected venue.
*/
public class PlaceDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
	
	public final static String TAG =  Config.unifiedLogs ? "HFGame" : "HFGame_UI";

	/**
	 * Factory that produces a new {@link PlaceDetailFragment} populated with
	 * details corresponding to the reference / ID of the venue passed in.
	 * @param reference Venue Reference
	 * @param id Venue Unique ID
	 * @return {@link PlaceDetailFragment}
	 */
	public static PlaceDetailFragment newInstance(String reference, String id) {
		if(D)Log.d(TAG, "PlaceDetailFragment.newInstance");
		
		PlaceDetailFragment f = new PlaceDetailFragment();

		// Supply reference and ID inputs as arguments.
		Bundle args = new Bundle();
		args.putString(Config.PlacesConstants.ARGUMENTS_KEY_REFERENCE, reference);
		args.putString(Config.PlacesConstants.ARGUMENTS_KEY_ID, id);
		f.setArguments(args);
 
		return f;
	}

	protected String placeReference = null;
	protected String placeId = null;

	protected Handler handler = new Handler();
	protected Activity activity;
	protected TextView nameTextView;
	protected TextView phoneTextView;
	protected TextView addressTextView;
	protected TextView ratingTextView;
	protected TextView urlTextView;
	protected Button checkinButton;
	protected TextView checkedInText;

	public PlaceDetailFragment() {
		super();
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		if(D)Log.d(TAG, "PlaceDetailFragment.onActivityCreated");
		
		super.onActivityCreated(savedInstanceState); 
		activity = getActivity();
     
		// Query the PlacesDetails Content Provider using a Loader to find
		// the details for the selected venue.
		if (placeId != null)
			getLoaderManager().initLoader(0, null, this);
 
		// Query the Shared Preferences to find the ID of the last venue checked in to.
		SharedPreferences sp = activity.getSharedPreferences(Config.PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
		String lastCheckin = sp.getString(Config.PlacesConstants.SP_KEY_LAST_CHECKIN_ID, null);
		if (lastCheckin != null )
			checkedIn(lastCheckin);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(D)Log.d(TAG, "PlaceDetailFragment.onCreateView");
		
		View view = inflater.inflate(R.layout.place_detail, container, false);
		nameTextView = (TextView)view.findViewById(R.id.detail_name);
		phoneTextView = (TextView)view.findViewById(R.id.detail_phone);
		addressTextView = (TextView)view.findViewById(R.id.detail_address);
		ratingTextView = (TextView)view.findViewById(R.id.detail_rating);
		urlTextView = (TextView)view.findViewById(R.id.detail_url);
		checkinButton = (Button)view.findViewById(R.id.checkin_button);
		checkedInText = (TextView)view.findViewById(R.id.detail_checkin_text);
 
		checkinButton.setOnClickListener(checkinButtonOnClickListener);
 
		if (getArguments() != null) {
			placeReference = getArguments().getString(Config.PlacesConstants.ARGUMENTS_KEY_REFERENCE);
			placeId = getArguments().getString(Config.PlacesConstants.ARGUMENTS_KEY_ID);
		}
		return view;
	}

	@Override
	public void onResume() {
		if(D)Log.d(TAG, "PlaceDetailFragment.onResume");
		
		super.onResume();
 
		// Always refresh the details on resume, but don't force
		// a refresh to minimize the network usage. Forced updates
		// are unnecessary as we force an update when a venue
		// is selected in the Place List Activity.
		if (placeReference != null && placeId != null)
			updatePlace(placeReference, placeId, false);
	}

	/**
	 * Start the {@link PlaceDetailsUpdateService} to refresh the details for the 
	 * selected venue.
	 * @param reference Reference
	 * @param id Unique Identifier
	 * @param forceUpdate Force an update
	 */
	protected void updatePlace(String reference, String id, boolean forceUpdate) {
		if (placeReference != null && placeId != null) {
			if(D)Log.d(TAG, String.format("PlaceDetailFragment.updatePlace: reference=%s, id=%s, forceUpdate=%b", reference, id, forceUpdate));
			// Start the PlaceDetailsUpdate Service to query the server for details
			// on the specified venue. A "forced update" will ignore the caching latency 
			// rules and query the server.
			Intent updateServiceIntent = new Intent(activity, PlaceDetailsUpdateService.class);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_REFERENCE, reference);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_ID, id);
			updateServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_FORCEREFRESH, forceUpdate);
			activity.startService(updateServiceIntent);
		}
	}

	/**
	 * {@inheritDoc}
	 * Query the {@link PlaceDetailsContentProvider} for the Phone, Address, Rating, Reference, and Url
	 * of the selected venue. 
	 * TODO Expand the projection to include any other details you are recording in the Place Detail Content Provider.
	 */
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(D)Log.d(TAG, "PlaceDetailFragment.onCreateLoader");
		
		String[] projection = new String[] {PlaceDetailsContentProvider.KEY_NAME, 
				PlaceDetailsContentProvider.KEY_PHONE, 
				PlaceDetailsContentProvider.KEY_ADDRESS, 
				PlaceDetailsContentProvider.KEY_RATING, 
				PlaceDetailsContentProvider.KEY_REFERENCE, 
				PlaceDetailsContentProvider.KEY_URL};
 
		String selection = PlaceDetailsContentProvider.KEY_ID + "='" + placeId + "'";
 
		return new CursorLoader(activity, PlaceDetailsContentProvider.CONTENT_URI, 
				projection, selection, null, null);
	}

	/**
	 * {@inheritDoc}
	 * When the Loader has completed, schedule an update of the Fragment UI on the main application thread.
	 */
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(D)Log.d(TAG, "PlaceDetailFragment.onLoadFinished");
		
		if (data.moveToFirst()) {
			final String name = data.getString(data.getColumnIndex(PlaceDetailsContentProvider.KEY_NAME));
			final String phone = data.getString(data.getColumnIndex(PlaceDetailsContentProvider.KEY_PHONE));
			final String address = data.getString(data.getColumnIndex(PlaceDetailsContentProvider.KEY_ADDRESS));
			final String rating = data.getString(data.getColumnIndex(PlaceDetailsContentProvider.KEY_RATING));
			final String url = data.getString(data.getColumnIndex(PlaceDetailsContentProvider.KEY_URL));

			// If we don't have a place reference passed in, we need to look it up and update our details
			// accordingly.
			if (placeReference == null) {
				placeReference = data.getString(data.getColumnIndex(PlaceDetailsContentProvider.KEY_REFERENCE));
				updatePlace(placeReference, placeId, true);
			}
   
			handler.post(new Runnable () {
				public void run() {
					if(D)Log.d(TAG, "PlaceDetailFragment.onLoadFinished.run");
					
					nameTextView.setText(name);
					phoneTextView.setText(phone);
					addressTextView.setText(address);
					ratingTextView.setText(rating);
					urlTextView.setText(url);
				}        
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void onLoaderReset(Loader<Cursor> loader) {
		if(D)Log.d(TAG, "PlaceDetailFragment.onLoaderReset");
		handler.post(new Runnable () {
			public void run() {
				if(D)Log.d(TAG, "PlaceDetailFragment.onLoaderReset.run");
				nameTextView.setText("");
				phoneTextView.setText("");
				addressTextView.setText("");
				ratingTextView.setText("");
				urlTextView.setText("");
			}
		});
	}

	/**
	 * When the Checkin Button is clicked start the {@link PlaceCheckinService} to checkin.
	 */
	protected OnClickListener checkinButtonOnClickListener = new OnClickListener() {
		public void onClick(View view) {
			if(D)Log.d(TAG, "PlaceDetailFragment.checkinButton.onClick");
			
			// TODO Pass in additional parameters to your checkin / rating / review service as appropriate
			// TODO In some cases you may prefer to open a new Activity with checkin details before initiating the Service.
			Intent checkinServiceIntent = new Intent(getActivity(), PlaceCheckinService.class);
			checkinServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_REFERENCE, placeReference);
			checkinServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_ID, placeId);
			checkinServiceIntent.putExtra(Config.PlacesConstants.EXTRA_KEY_TIME_STAMP, System.currentTimeMillis());
			getActivity().startService(checkinServiceIntent);
		}
	}; 

	/**
	 * Checks to see if the currently displayed venue is the last place checked in to.
	 * IF it is, it disables the checkin button and update the UI accordingly.
	 * @param id Checked-in place ID
	 */
	public void checkedIn(String id) {
		if(D)Log.d(TAG, "PlaceDetailFragment.checkedIn");
		
		if (placeId == null) {
			if(E)Log.e(TAG, "Place ID = null");
		}
		
		boolean checkedIn = id != null && placeId != null && placeId.equals(id);
		checkinButton.setEnabled(!checkedIn);
		checkedInText.setVisibility(checkedIn ? View.VISIBLE : View.INVISIBLE);
	}
}