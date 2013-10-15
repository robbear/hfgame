package com.hyperfine.slideshare;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

//
// SlideShowJSON example:
//
//  {
//      title: "Title text",
//      description: "Description text",
//      transitionEffect: 0,
//      slides: {
//          { guidval1: { image: "http://foo.com/1.jpg", audio: "http://foo.com/1.3gp" },
//          { guidval2: { image: "http://foo.com/2.jpg", audio: "http://foo.com/2.3gp" },
//          { guidval3: { image: "http://foo.com/3.jpg", audio: "http://foo.com/3.3gp" }
//      },
//      order: [ guidval2, guidval3, guidval1 ]
//  }
//

public class SlideShowJSON extends JSONObject {
    public final static String TAG = "SlideShowJSON";

    public enum TransitionEffects {
        None;
    };
    // Cache TransitionEffects.values() for doing enum-to-int conversions
    public TransitionEffects[] TransitionEffectsValues = TransitionEffects.values();

    public SlideShowJSON() throws JSONException {
        if(D)Log.d(TAG, "SlideShowJSON.SlideShowJSON");

        // BUGBUG - TODO: need resource strings
        put("title", "Untitled");
        put("description", "No description");
        put("transitionEffect", TransitionEffects.None.ordinal());

        JSONObject slides = new JSONObject();
        put("slides", slides);

        JSONArray orderArray = new JSONArray();
        put("order", orderArray);

        if(D)Log.d(TAG, String.format("SlideShowJSON: initial JSON = %s", this.toString()));
    }

    public void setTitle(String title) throws JSONException {
        put("title", title);
    }

    public String getTitle() throws JSONException {
        return getString("title");
    }

    public void setDescription(String description) throws JSONException {
        put("description", description);
    }

    public String getDescription() throws JSONException {
        return getString("description");
    }

    public void setTransitionEffect(TransitionEffects effect) throws JSONException {
        put("transitionEffect", effect.ordinal());
    }

    public TransitionEffects getTransitionEffect() throws JSONException {
        int effectOrdinal = getInt("transitionEffect");

        return TransitionEffectsValues[effectOrdinal];
    }

    public JSONObject getSlides() throws JSONException {
        return getJSONObject("slides");
    }

    public void setSlides(JSONObject slides) throws JSONException {
        put("slides", slides);
    }

    public JSONArray getOrder() throws JSONException {
        return getJSONArray("order");
    }

    public void setOrder(JSONArray order) throws JSONException {
        put("order", order);
    }
}
