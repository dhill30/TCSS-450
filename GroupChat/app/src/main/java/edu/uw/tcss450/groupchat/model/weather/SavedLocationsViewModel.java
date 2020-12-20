package edu.uw.tcss450.groupchat.model.weather;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.ui.weather.SavedLocation;

/**
 * This view model holds lists of SavedLocation objects for interacting with the user's favorited
 * and searched locations.
 *
 * @version December 19, 2020
 */
public class SavedLocationsViewModel extends AndroidViewModel {

    private MutableLiveData<JSONObject> mResponse;

    private MutableLiveData<List<SavedLocation>> mLocations;

    private MutableLiveData<List<SavedLocation>> mFavorites;

    /**
     * Default constructor for this view model.
     * @param application reference to the current application
     */
    public SavedLocationsViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>(new JSONObject());
        mLocations = new MutableLiveData<>(new ArrayList<>());
        mFavorites = new MutableLiveData<>(new ArrayList<>());
    }

    /**
     * Adds an observer to the response object.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Adds an observer to the saved and searched locations.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addLocationsObserver(@NonNull LifecycleOwner owner,
                                     @NonNull Observer<? super List<SavedLocation>> observer) {
        mLocations.observe(owner, observer);
    }

    /**
     * Returns whether or not the passed location is a favorite of the user.
     * @param location location to check for
     * @return true if the location is a favorite one
     */
    public boolean isFavorite(final SavedLocation location) {
        return mFavorites.getValue().contains(location);
    }

    /**
     * Adds a new location to the list of saved and searched locations.
     * @param location location to be added
     */
    public void addLocation(final SavedLocation location) {
        if (!mLocations.getValue().contains(location)) {
            mLocations.getValue().add(new SavedLocation(location));
        }
        mLocations.setValue(mLocations.getValue());
    }

    /**
     * Clears the searched locations from the list.
     */
    public void clearSearched() {
        mLocations.setValue(mFavorites.getValue());
    }

    /**
     * Makes a request to the web service to get the list of the user's favorite locations.
     * @param jwt the user's signed JWT
     */
    public void connect(final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "locations";

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleSuccess,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    /**
     * Makes a request to the web service to save a location as one of the user's favorites.
     * @param jwt the user's signed JWT
     * @param name the nickname of the location
     * @param lat the latitude of the location
     * @param lon the longitude of the location
     */
    public void connectSaveLocation(final String jwt,
                                    final String name,
                                    final double lat,
                                    final double lon) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "locations";

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("lat", lat);
            body.put("long", lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                mResponse::setValue,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    /**
     * Makes a request to the web service to remove a location from the user's favorites.
     * @param jwt teh user's signed JWT
     * @param lat the latitude of the location
     * @param lon the longitude of the location
     */
    public void connectRemoveLocation(final String jwt, final double lat, final double lon) {
        mFavorites.getValue().remove(new SavedLocation("", lat, lon));

        String url = getApplication().getResources().getString(R.string.base_url)
                + "locations?lat=" + lat + "&long=" + lon;

        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                mResponse::setValue,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    private void handleSuccess(final JSONObject result) {
        List<SavedLocation> list = new ArrayList<>();
        try {
            if (result.has("locations")) {
                JSONArray locations = result.getJSONArray("locations");

                for (int i = 0; i < locations.length(); i++) {
                    JSONObject jsonLocation = locations.getJSONObject(i);
                    SavedLocation location = new SavedLocation(
                            jsonLocation.getString("nickname"),
                            jsonLocation.getDouble("lat"),
                            jsonLocation.getDouble("long"));
                    mLocations.getValue().remove(location);
                    mLocations.getValue().add(i, location);
                    list.add(location);
                }
            } else {
                Log.e("ERROR", "No locations array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
        mLocations.setValue(mLocations.getValue());
        mFavorites.setValue(list);
    }

    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            try {
                mResponse.setValue(new JSONObject("{" +
                        "error:\"" + error.getMessage() +
                        "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
        else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            try {
                mResponse.setValue(new JSONObject("{" +
                        "code:" + error.networkResponse.statusCode +
                        ", data:" + data +
                        "}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
    }
}
