package com.hyperfine.slideshare;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

//
// SlideShareJSON example:
//
//  {
//      title: "Title text",
//      description: "Description text",
//      transitionEffect: 0,
//      slides: {
//          guidval1: { image: "http://foo.com/1.jpg", audio: "http://foo.com/1.3gp" },
//          guidval2: { image: "http://foo.com/2.jpg", audio: "http://foo.com/2.3gp" },
//          guidval3: { image: "http://foo.com/3.jpg", audio: "http://foo.com/3.3gp" }
//      },
//      order: [ guidval2, guidval3, guidval1 ]
//  }
//

public class SlideShareJSON extends JSONObject {
    public final static String TAG = "SlideShareJSON";

    public final static String KEY_TITLE = "title";
    public final static String KEY_DESCRIPTION = "description";
    public final static String KEY_TRANSITIONEFFECT = "transitionEffect";
    public final static String KEY_SLIDES = "slides";
    public final static String KEY_IMAGE = "image";
    public final static String KEY_AUDIO = "audio";
    public final static String KEY_ORDER = "order";

    public enum TransitionEffects {
        None;
    };
    // Cache TransitionEffects.values() for doing enum-to-int conversions
    public TransitionEffects[] TransitionEffectsValues = TransitionEffects.values();

    public SlideShareJSON() throws JSONException {
        if(D)Log.d(TAG, "SlideShareJSON.SlideShareJSON");

        put(KEY_TITLE, "Untitled");
        put(KEY_DESCRIPTION, "No description");
        put(KEY_TRANSITIONEFFECT, TransitionEffects.None.ordinal());

        JSONObject slides = new JSONObject();
        put(KEY_SLIDES, slides);

        JSONArray orderArray = new JSONArray();
        put(KEY_ORDER, orderArray);

        if(D)Log.d(TAG, String.format("SlideShareJSON: initial JSON = %s", this.toString()));
    }

    public SlideShareJSON(String json) throws JSONException {
        super(json);

        if(D)Log.d(TAG, String.format("SlideShareJSON.SlideShareJSON constructed from string: %s", json));
    }

    public void setTitle(String title) throws JSONException {
        put(KEY_TITLE, title);
    }

    public String getTitle() throws JSONException {
        return getString(KEY_TITLE);
    }

    public void setDescription(String description) throws JSONException {
        put(KEY_DESCRIPTION, description);
    }

    public String getDescription() throws JSONException {
        return getString(KEY_DESCRIPTION);
    }

    public void setTransitionEffect(TransitionEffects effect) throws JSONException {
        put(KEY_TRANSITIONEFFECT, effect.ordinal());
    }

    public TransitionEffects getTransitionEffect() throws JSONException {
        int effectOrdinal = getInt(KEY_TRANSITIONEFFECT);

        return TransitionEffectsValues[effectOrdinal];
    }

    public JSONObject getSlides() throws JSONException {
        return getJSONObject(KEY_SLIDES);
    }

    public void setSlides(JSONObject slides) throws JSONException {
        put(KEY_SLIDES, slides);
    }

    public JSONArray getOrder() throws JSONException {
        return getJSONArray(KEY_ORDER);
    }

    public void setOrder(JSONArray order) throws JSONException {
        put(KEY_ORDER, order);
    }

    public void upsertSlide(String uuidString, String imageUrl, String audioUrl) throws JSONException {
        if(D)Log.d(TAG, String.format("SlideShareJSON.upsertSlide: uuid=%s, imageUrl=%s, audioUrl=%s", uuidString, imageUrl, audioUrl));

        JSONObject slides = getSlides();
        JSONObject slide = null;

        if (slides.has(uuidString)) {
            slide = slides.getJSONObject(uuidString);
        }

        if (slide != null) {
            if(D)Log.d(TAG, String.format("SlideShareJSON.upsertSlide - found slide and updating for uuid=%s", uuidString));

            if (imageUrl != null) {
                slide.put(KEY_IMAGE, imageUrl);
            }

            if (audioUrl != null) {
                slide.put(KEY_AUDIO, audioUrl);
            }
        }
        else {
            if(D)Log.d(TAG, String.format("SlideShareJSON.upsertSlide - no slide found for %s, so creating new slide", uuidString));

            JSONArray orderArray = getOrder();

            JSONObject paths = new JSONObject();
            paths.put(KEY_IMAGE, imageUrl);
            paths.put(KEY_AUDIO, audioUrl);

            slides.put(uuidString, paths);
            orderArray.put(uuidString);
        }
    }

    public void removeSlide(String uuidString) throws JSONException {
        if(D)Log.d(TAG, String.format("SlideShareJSON.removeSlide: uuid=%s", uuidString));

        JSONObject slides = getSlides();
        JSONObject slide = null;

        if (slides.has(uuidString)) {
            if(D)Log.d(TAG, "SlideShareJSON.removeSlide - removing slide and order entry");

            JSONArray orderArray = getOrder();
            JSONArray newOrderArray = new JSONArray();

            // Remove the item from the "order" array by rebuilding the array
            // without the item.
            for (int i = 0; i < orderArray.length(); i++) {
                String item = orderArray.getString(i);
                if (!uuidString.equals(item)) {
                    newOrderArray.put(item);
                }
            }

            setOrder(newOrderArray);
            slides.remove(uuidString);
        }
        else {
            if(D)Log.d(TAG, "SlideShareJSON.removeSlide - no slide found. Bailing");
        }
    }

    public JSONObject getSlide(String uuidSlide) throws JSONException {
        if(D)Log.d(TAG, String.format("SlideShareJSON.getSlide: uuid=%s", uuidSlide));

        JSONObject slides = getSlides();
        JSONObject slide = null;

        if (slides.has(uuidSlide)) {
            return slides.getJSONObject(uuidSlide);
        }
        else {
            return null;
        }
    }

    public boolean save(Context context, String folder, String fileName) {
        if(D)Log.d(TAG, String.format("SlideShareJSON.save: folder=%s, fileName=%s", folder, fileName));

        return Utilities.saveStringToFile(context, this.toString(), folder, fileName);
    }

    public static SlideShareJSON load(Context context, String folder, String fileName) {
        if(D)Log.d(TAG, String.format("SlideShareJSON.load: folder=%s, fileName=%s", folder, fileName));

        SlideShareJSON ssj = null;
        String json = Utilities.loadStringFromFile(context, folder, fileName);

        if (json == null) {
            return null;
        }

        try {
            ssj = new SlideShareJSON(json);
        }
        catch (Exception e) {
            if(E)Log.e(TAG, "SlideShareJSON.load", e);
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {
            if(E)Log.e(TAG, "SlideShareJSON.load", e);
            e.printStackTrace();
        }

        return ssj;
    }
}
