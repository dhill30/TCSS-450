package edu.uw.tcss450.groupchat.ui.weather;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentWeatherMainBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.weather.CurrentLocationViewModel;
import edu.uw.tcss450.groupchat.model.weather.SavedLocationsViewModel;
import edu.uw.tcss450.groupchat.model.weather.WeatherSearchDailyViewModel;
import edu.uw.tcss450.groupchat.model.weather.WeatherSearchViewModel;

/**
 * Fragment for main page of weather.
 *
 * @version December 2020
 */
public class WeatherMainFragment extends Fragment {

    private CurrentLocationViewModel mLocationModel;

    private SavedLocationsViewModel mSavesModel;

    private WeatherSearchViewModel mWeatherModel;

    private WeatherSearchDailyViewModel mDailyModel;

    private UserInfoViewModel mUserModel;

    private FragmentWeatherMainBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mLocationModel = provider.get(CurrentLocationViewModel.class);
        mSavesModel = provider.get(SavedLocationsViewModel.class);
        mWeatherModel = provider.get(WeatherSearchViewModel.class);
        mDailyModel = provider.get(WeatherSearchDailyViewModel.class);
        mUserModel = provider.get(UserInfoViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWeatherMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WeatherMainFragmentArgs args = WeatherMainFragmentArgs.fromBundle(getArguments());
        if (args.getLocation() != null) {
            LatLng latLng = args.getLocation();
            mWeatherModel.setLocation(new SavedLocation(
                    args.getLocationName(), latLng.latitude, latLng.longitude));
        }

        binding.buttonRefresh.setOnClickListener(button -> {
            SavedLocation location = mWeatherModel.getLocation();
            mWeatherModel.connect(location.getLatitude(), location.getLongitude());
            binding.weatherWait.setVisibility(View.VISIBLE);
        });

        mLocationModel.addLocationObserver(getViewLifecycleOwner(), location -> {
            Geocoder geocoder = new Geocoder(getContext());
            List<Address> results = null;
            try {
                results = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                Log.e("ERROR", "Geocoder error on location");
                e.printStackTrace();
            }
            String name = results.get(0).getAddressLine(0);

            if (!mWeatherModel.isInitialized()) {
                mWeatherModel.initialize(new SavedLocation(
                        name, location.getLatitude(), location.getLongitude()));
                mWeatherModel.connect(location.getLatitude(), location.getLongitude());
                mSavesModel.connect(mUserModel.getJwt());
            } else {
                mWeatherModel.setCurrent(location);
            }
        });

        AtomicBoolean celsius = new AtomicBoolean(false);

        mWeatherModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                if (response.has("code")) {
                    Log.d("Weather", "Invalid location");
                } else {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        JSONObject coord = response.getJSONObject("coord");
                        JSONObject wind = response.getJSONObject("wind");
                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject info = (JSONObject) weather.get(0);


                        binding.textCity.setText(response.getString("name"));
                        binding.textDayTime.setText(new SimpleDateFormat("EEEE h:mm a",
                                Locale.getDefault()).format(new Date()));
                        binding.textCondition.setText(info.getString("main"));
                        setImage(info, binding.imageCondition);
                        binding.textDegree.setText(String.valueOf((int) main.getDouble("temp")));
                        binding.textHumidity.setText("Humidity: "
                                + (int) main.getDouble("humidity") + "%");
                        binding.textWind.setText("Wind: "
                                + (int) wind.getDouble("speed") + " mph");

                        mDailyModel.connect(coord.getDouble("lat"),
                                coord.getDouble("lon"));

                        binding.buttonCelsius.setOnClickListener(v -> {
                            if (!celsius.get()) {
                                try {
                                    int temp = (int) (main.getDouble("temp") - 32) * 5 / 9;
                                    binding.textDegree.setText(String.valueOf(temp));
                                    binding.buttonCelsius.setTextColor(
                                            binding.buttonFahrenheit.getCurrentTextColor());
                                    binding.buttonFahrenheit.setTextColor(Color.GRAY);
                                    celsius.set(true);
                                    mDailyModel.connect(coord.getDouble("lat"),
                                            coord.getDouble("lon"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("JSON Parse Error", e.getMessage());
                                }
                            }
                        });

                        binding.buttonFahrenheit.setOnClickListener(v -> {
                            if (celsius.get()) {
                                try {
                                    int temp = (int) main.getDouble("temp");
                                    binding.textDegree.setText(String.valueOf(temp));
                                    binding.buttonFahrenheit.setTextColor(
                                            binding.buttonCelsius.getCurrentTextColor());
                                    binding.buttonCelsius.setTextColor(Color.GRAY);
                                    celsius.set(false);
                                    mDailyModel.connect(coord.getDouble("lat"),
                                            coord.getDouble("lon"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("JSON Parse Error", e.getMessage());
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        });

        mDailyModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                if (response.has("code")) {
                    Log.d("Weather", "Invalid location");
                } else {
                    try {
                        JSONArray daily = response.getJSONArray("daily");
                        JSONArray hourly = response.getJSONArray("hourly");
                        getDaily(daily, celsius.get());
                        getHourly(hourly);
                        binding.weatherWait.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        onPrepareSpinner(menu.findItem(R.id.action_spinner), menu.findItem(R.id.action_favorite));
        menu.findItem(R.id.action_spinner).setVisible(true);
        menu.findItem(R.id.action_favorite).setVisible(true);
        menu.findItem(R.id.action_clear_searched).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) favoriteLocation();
        if (item.getItemId() == R.id.action_clear_searched) clearLocations();
        return super.onOptionsItemSelected(item);
    }

    private void getDaily(JSONArray daily, boolean celsius) throws JSONException {
        for (int i = 1; i <= 7; i++) {
            JSONObject day = (JSONObject) daily.get(i);
            JSONObject temp = day.getJSONObject("temp");
            JSONArray weather = day.getJSONArray("weather");
            JSONObject info = (JSONObject) weather.get(0);

            String max, min, temps;
            if (celsius) {
                max = String.valueOf((int) (temp.getDouble("max") - 32) * 5 / 9);
                min = String.valueOf((int) (temp.getDouble("min") - 32) * 5 / 9);
                temps = max + "°C / " + min + "°C";
            } else {
                max = String.valueOf((int) temp.getDouble("max"));
                min = String.valueOf((int) temp.getDouble("min"));
                temps = max + "°F / " + min + "°F";
            }

            switch (i) {
                case 1:
                    setImage(info, binding.imageOne);
                    binding.textWeatherOne.setText(temps);
                    binding.textDayOne.setText(getDayName(i));
                    break;
                case 2:
                    setImage(info, binding.imageTwo);
                    binding.textWeatherTwo.setText(temps);
                    binding.textDayTwo.setText(getDayName(i));
                    break;
                case 3:
                    setImage(info, binding.imageThree);
                    binding.textWeatherThree.setText(temps);
                    binding.textDayThree.setText(getDayName(i));
                    break;
                case 4:
                    setImage(info, binding.imageFour);
                    binding.textWeatherFour.setText(temps);
                    binding.textDayFour.setText(getDayName(i));
                    break;
                case 5:
                    setImage(info, binding.imageFive);
                    binding.textWeatherFive.setText(temps);
                    binding.textDayFive.setText(getDayName(i));
                    break;
                case 6:
                    setImage(info, binding.imageSix);
                    binding.textWeatherSix.setText(temps);
                    binding.textDaySix.setText(getDayName(i));
                    break;
                case 7:
                    setImage(info, binding.imageSeven);
                    binding.textWeatherSeven.setText(temps);
                    binding.textDaySeven.setText(getDayName(i));
                    break;
                default:
                    Log.d("DAILY WEATHER ERROR", "Could not set daily weather");
                    break;
            }
        }
    }

    private void getHourly(JSONArray hourly) throws JSONException {
        List<WeatherHourly> mHourly = new ArrayList<>();
        for (int i = 1; i < 25; i++) {
            JSONObject hour = (JSONObject) hourly.get(i);
            JSONArray weather = hour.getJSONArray("weather");
            JSONObject info = (JSONObject) weather.get(0);

            Calendar calendar = Calendar.getInstance();
            int time = calendar.get(Calendar.HOUR_OF_DAY) + i;
            if (time >= 24) {
                time -= 24;
            }
            String temp = String.valueOf((int) hour.getDouble("temp"));
            String icon = info.getString("icon");

            mHourly.add(new WeatherHourly(time, icon, temp));
        }

        RecyclerView recyclerView = binding.hourlyView;
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new WeatherRecyclerViewAdapter(mHourly));
    }

    private void onPrepareSpinner(MenuItem spinnerItem, MenuItem favoriteItem) {
        Spinner spinner = (Spinner) spinnerItem.getActionView();

        mSavesModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                if (response.has("code")) {
                    binding.weatherWait.setVisibility(View.GONE);
                    try {
                        Log.e("Web Service Error",
                                response.getJSONObject("data").getString("message"));
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                } else {
                    binding.weatherWait.setVisibility(View.GONE);
                    try {
                        if (response.getString("message")
                                .equals("Location saved successfully!")) {
                            favoriteItem.setIcon(R.drawable.ic_weather_star_filled_24dp);
                        } else {
                            favoriteItem.setIcon(R.drawable.ic_weather_star_empty_24dp);
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                    mSavesModel.connect(mUserModel.getJwt());
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        });

        mSavesModel.addLocationsObserver(getViewLifecycleOwner(), locations -> {
            SavedLocation location = mWeatherModel.getLocation();

            List<SavedLocation> items = new ArrayList<>();
            items.add(mWeatherModel.getCurrent());
            items.addAll(locations);
            if (!items.contains(location)) items.add(location);
            items.add(new SavedLocation("Get new location...", 0, 0));

            ArrayAdapter<SavedLocation> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            mWeatherModel.addLocationObserver(getViewLifecycleOwner(), saved -> {
                int pos = adapter.getPosition(saved);
                spinner.setSelection(pos);
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView text = (TextView) parent.getChildAt(0);
                    if (text != null) text.setTextColor(Color.WHITE);

                    SavedLocation newLocation = new SavedLocation((SavedLocation) parent.getSelectedItem());
                    if (position == parent.getCount() - 1) {
                        Navigation.findNavController(getView())
                                .navigate(WeatherMainFragmentDirections
                                        .actionNavigationWeatherToWeatherMapFragment());
                        spinnerItem.collapseActionView();
                        return;
                    } else if (position == 0) {
                        Geocoder geocoder = new Geocoder(getContext());
                        List<Address> results = null;
                        try {
                            results = geocoder.getFromLocation(
                                    newLocation.getLatitude(), newLocation.getLongitude(), 1);
                        } catch (IOException e) {
                            Log.e("ERROR", "Geocoder error on location");
                            e.printStackTrace();
                        }
                        if (results != null) {
                            newLocation.setName(results.get(0).getAddressLine(0));
                        }
                    }
                    mWeatherModel.setLocation(newLocation);
                    mWeatherModel.connect(newLocation.getLatitude(), newLocation.getLongitude());
                    if (mSavesModel.isFavorite(newLocation)) {
                        favoriteItem.setIcon(R.drawable.ic_weather_star_filled_24dp);
                    } else {
                        favoriteItem.setIcon(R.drawable.ic_weather_star_empty_24dp);
                    }
                    binding.weatherWait.setVisibility(View.VISIBLE);
                    spinnerItem.collapseActionView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // do nothing
                }
            });
        });
    }

    private void favoriteLocation() {
        SavedLocation location = mWeatherModel.getLocation();
        if (mSavesModel.isFavorite(location)) {
            mSavesModel.connectRemoveLocation(mUserModel.getJwt(),
                    location.getLatitude(),
                    location.getLongitude());
        } else {
            mSavesModel.connectSaveLocation(mUserModel.getJwt(),
                    location.getName(),
                    location.getLatitude(),
                    location.getLongitude());
        }
        binding.weatherWait.setVisibility(View.VISIBLE);
    }

    private void clearLocations() {
        mWeatherModel.setLocation(mWeatherModel.getCurrent());
        mSavesModel.clearSearched();
    }

    private String getDayName(int forecast) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) + forecast;
        if (day > 7) day -= 7;

        switch (day) {
            case 1:
                return "SUNDAY";
            case 2:
                return "MONDAY";
            case 3:
                return "TUESDAY";
            case 4:
                return "WEDNESDAY";
            case 5:
                return "THURSDAY";
            case 6:
                return "FRIDAY";
            case 7:
                return "SATURDAY";
        }
        return null;
    }

    /**
     * Method to set the icons for weather condition
     *
     * @param info, the JSONObject that contain the weather info
     * @param view, the image view for weather condition
     */
    private void setImage(JSONObject info, ImageView view) throws JSONException {
        String icon = info.getString("icon");

        switch (icon) {
            case "01d":
                view.setImageResource(R.drawable.ic_1d);
                break;
            case "02d":
                view.setImageResource(R.drawable.ic_2d);
                break;
            case "03d":
                view.setImageResource(R.drawable.ic_3d);
                break;
            case "04d":
                view.setImageResource(R.drawable.ic_4d);
                break;
            case "09d":
                view.setImageResource(R.drawable.ic_9d);
                break;
            case "10d":
                view.setImageResource(R.drawable.ic_10d);
                break;
            case "11d":
                view.setImageResource(R.drawable.ic_11d);
                break;
            case "13d":
                view.setImageResource(R.drawable.ic_13d);
                break;
            case "50d":
                view.setImageResource(R.drawable.ic_50d);
                break;
            case "01n":
                view.setImageResource(R.drawable.ic_1n);
                break;
            case "02n":
                view.setImageResource(R.drawable.ic_2n);
                break;
            case "03n":
                view.setImageResource(R.drawable.ic_3n);
                break;
            case "04n":
                view.setImageResource(R.drawable.ic_4n);
                break;
            case "09n":
                view.setImageResource(R.drawable.ic_9n);
                break;
            case "10n":
                view.setImageResource(R.drawable.ic_10n);
                break;
            case "11n":
                view.setImageResource(R.drawable.ic_11n);
                break;
            case "13n":
                view.setImageResource(R.drawable.ic_13n);
                break;
            case "50n":
                view.setImageResource(R.drawable.ic_50n);
                break;
            default:
                Log.d("ICON ERROR", "Could not get weather icon");
                break;
        }
    }
}