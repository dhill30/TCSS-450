package edu.uw.tcss450.groupchat.model.weather;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
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
import edu.uw.tcss450.groupchat.ui.weather.Weather;
import edu.uw.tcss450.groupchat.ui.weather.WeatherInfo;

/**
 * This ViewModel holds data for the home page's weather information.
 *
 * @version December 31, 2020
 */
public class WeatherHomeViewModel extends AndroidViewModel {

    private MutableLiveData<Weather> mWeather;

    private String mName;

    /**
     * Default constructor for this view model.
     * @param application reference to the current application
     */
    public WeatherHomeViewModel(@NonNull Application application) {
        super(application);
        mWeather = new MutableLiveData<>();
    }

    /**
     * Adds an observer to the weather object.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addWeatherObserver(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<? super Weather> observer) {
        mWeather.observe(owner, observer);
    }

    /**
     * Returns the name of the weather location
     * @return name of location
     */
    public String getName() {
        return mName;
    }

    /**
     * Makes a request to the web service to get the weather information
     * @param lat the latitude for getting weather info
     * @param lon the longitude for getting weather info
     */
    public void connect(final double lat, final double lon) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "weather?lat=" + lat + "&lon=" + lon;

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
                JSONArray weather = current.getJSONArray("weather");
                String timezone = result.getString("timezone");
                double lat = result.getDouble("lat");
                double lon = result.getDouble("lon");

                Geocoder geocoder = new Geocoder(getApplication().getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> adds = geocoder.getFromLocation(lat, lon, 1);
                    if (adds.get(0).getLocality() != null) {
                        mName = adds.get(0).getLocality();
                    } else if (adds.get(0).getAdminArea() != null) {
                        mName = adds.get(0).getAdminArea();
                    } else if (adds.get(0).getCountryName() != null) {
                        mName = adds.get(0).getCountryName();
                    } else if (adds.get(0).getAddressLine(0) != null){
                        mName = adds.get(0).getAddressLine(0);
                    } else {
                        DecimalFormat df = new DecimalFormat("#0.00");
                        mName = df.format(lat) + ", " + df.format(lon);
                    }
                } catch (IOException ex) {
                    Log.e("Geocoder Error", ex.getMessage());
                    ex.printStackTrace();
                }

                mWeather.setValue(new Weather("current",
                        current.getLong("dt"),
                        timezone,
                        weather.getJSONObject(0).getString("main"),
                        weather.getJSONObject(0).getString("icon"),
                        current.getDouble("temp"),
                        current.getInt("humidity"),
                        current.getDouble("wind_speed")));
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
