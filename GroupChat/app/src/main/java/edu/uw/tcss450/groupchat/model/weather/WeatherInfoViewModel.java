package edu.uw.tcss450.groupchat.model.weather;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.ui.weather.SavedLocation;
import edu.uw.tcss450.groupchat.ui.weather.WeatherInfo;

/**
 * This ViewModel holds data for the weather page's weather forecast.
 *
 * @version December 31, 2020
 */
public class WeatherInfoViewModel extends AndroidViewModel {

    private MutableLiveData<WeatherInfo> mWeatherInfo;

    private MutableLiveData<SavedLocation> mLocation;

    private boolean init = false;

    private SavedLocation mCurrent;

    /**
     * Default constructor for this view model.
     * @param application reference to the current application
     */
    public WeatherInfoViewModel(@NonNull Application application) {
        super(application);
        mWeatherInfo = new MutableLiveData<>();
        mLocation = new MutableLiveData<>();
    }

    /**
     * Initializes this view model with the initial location.
     * @param location the initial location for this view model
     */
    public void initialize(final SavedLocation location) {
        mLocation.setValue(new SavedLocation(location));

        DecimalFormat df = new DecimalFormat("#0.00");
        String name = "Current Location (" + df.format(location.getLatitude())
                + ", " + df.format(location.getLongitude()) + ")";
        mCurrent = new SavedLocation(name, location.getLatitude(), location.getLongitude());

        init = true;
    }

    /**
     * Adds an observer to the weather object.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addWeatherObserver(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<? super WeatherInfo> observer) {
        mWeatherInfo.observe(owner, observer);
    }

    /**
     * Adds an observer to the location object.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addLocationObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super SavedLocation> observer) {
        mLocation.observe(owner, observer);
    }

    /**
     * Returns whether or not this view model has been initialized.
     * @return true if initialized
     */
    public boolean initialized() {
        return init;
    }

    /**
     * Returns the current saved location object.
     * @return current saved location
     */
    public SavedLocation getCurrent() {
        return new SavedLocation(mCurrent);
    }

    /**
     * Returns the saved location object.
     * @return saved location
     */
    public SavedLocation getLocation() {
        return new SavedLocation(mLocation.getValue());
    }

    /**
     * Updates the current saved location.
     * @param location new current saved location
     */
    public void setCurrent(final Location location) {
        DecimalFormat df = new DecimalFormat("#0.00");
        String name = "Current Location (" + df.format(location.getLatitude())
                + ", " + df.format(location.getLongitude()) + ")";
        mCurrent = new SavedLocation(name, location.getLatitude(), location.getLongitude());
    }

    /**
     * Updates the saved location.
     * @param location new saved location
     */
    public void setLocation(final SavedLocation location) {
        mLocation.setValue(new SavedLocation(location));
    }

    /**
     * Makes a request to the web service to get the weather information
     * @param lat the latitude for getting weather info
     * @param lon the longitude for getting weather info
     */
    public void connect(final double lat, final double lon) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "weather?lat=" +  lat + "&lon=" + lon;

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleSuccess,
                this::handleError);

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    private void handleSuccess(final JSONObject result) {
        try {
            if (result.has("current")) {
                JSONObject current = result.getJSONObject("current");
                JSONArray hourly = result.getJSONArray("hourly");
                JSONArray daily = result.getJSONArray("daily");
                String timezone = result.getString("timezone");
                double lat = result.getDouble("lat");
                double lon = result.getDouble("lon");

                Geocoder geocoder = new Geocoder(getApplication().getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> adds = geocoder.getFromLocation(lat, lon, 1);
                    if (adds.size() == 0) {
                        DecimalFormat df = new DecimalFormat("#0.00");
                        String name = df.format(lat) + ", " + df.format(lon);
                        mWeatherInfo.setValue(new WeatherInfo(current, hourly, daily, timezone, name));
                    } else {
                        if (adds.get(0).getLocality() != null) {
                            mWeatherInfo.setValue(new WeatherInfo(current, hourly, daily, timezone, adds.get(0).getLocality()));
                        } else if (adds.get(0).getAdminArea() != null) {
                            mWeatherInfo.setValue(new WeatherInfo(current, hourly, daily, timezone, adds.get(0).getAdminArea()));
                        } else if (adds.get(0).getCountryName() != null) {
                            mWeatherInfo.setValue(new WeatherInfo(current, hourly, daily, timezone, adds.get(0).getCountryName()));
                        } else if (adds.get(0).getAddressLine(0) != null){
                            mWeatherInfo.setValue(new WeatherInfo(current, hourly, daily, timezone, adds.get(0).getAddressLine(0)));
                        } else {
                            DecimalFormat df = new DecimalFormat("#0.00");
                            String name = df.format(lat) + ", " + df.format(lon);
                            mWeatherInfo.setValue(new WeatherInfo(current, hourly, daily, timezone, name));
                        }
                    }
                } catch (IOException e) {
                    Log.e("Geocoder Error", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Log.e("ERROR", "No weather information");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
    }

    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            Log.e("NETWORK ERROR", error.getMessage());
        }
        else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            Log.e("CLIENT ERROR",
                    error.networkResponse.statusCode + " " + data);
        }
    }
}
