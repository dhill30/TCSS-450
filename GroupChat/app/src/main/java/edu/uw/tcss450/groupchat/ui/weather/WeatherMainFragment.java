package edu.uw.tcss450.groupchat.ui.weather;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentWeatherMainBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.weather.CurrentLocationViewModel;
import edu.uw.tcss450.groupchat.model.weather.SavedLocationsViewModel;
import edu.uw.tcss450.groupchat.model.weather.WeatherInfoViewModel;

/**
 * Fragment for main page of weather.
 *
 * @version December 2020
 */
public class WeatherMainFragment extends Fragment {

    private CurrentLocationViewModel mLocationModel;

    private SavedLocationsViewModel mSavesModel;

    private WeatherInfoViewModel mWeatherModel;

    private UserInfoViewModel mUserModel;

    private FragmentWeatherMainBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mLocationModel = provider.get(CurrentLocationViewModel.class);
        mSavesModel = provider.get(SavedLocationsViewModel.class);
        mWeatherModel = provider.get(WeatherInfoViewModel.class);
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
                Log.e("Geocoder Error", e.getMessage());
                e.printStackTrace();
            }
            String name = results.get(0).getAddressLine(0);

            if (!mWeatherModel.initialized()) {
                mWeatherModel.initialize(new SavedLocation(
                        name, location.getLatitude(), location.getLongitude()));
                mWeatherModel.connect(location.getLatitude(), location.getLongitude());
                mSavesModel.connect(mUserModel.getJwt());
                binding.weatherWait.setVisibility(View.VISIBLE);
            } else {
                mWeatherModel.setCurrent(location);
            }
        });

        AtomicBoolean celsius = new AtomicBoolean(false);

        mWeatherModel.addWeatherObserver(getViewLifecycleOwner(), info -> {
            Weather current = info.getCurrent();

            binding.textCity.setText(info.getName());
            binding.textDayTime.setText(current.getTime());
            binding.textCondition.setText(current.getMain());
            binding.imageCondition.setImageResource(current.getIcon());
            binding.textDegree.setText(current.getTemp(false));
            binding.textHumidity.setText("Humidity: " + current.getHumidity() + "%");
            binding.textWind.setText("Wind: " + current.getWindSpeed(false) + " mph");

            final RecyclerView recyclerView = binding.hourlyView;
            recyclerView.setLayoutManager(new LinearLayoutManager(
                    recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(new WeatherRecyclerViewAdapter(info.getHourly()));

            loadDailyWeather(info.getDaily(), celsius.get());

            binding.buttonCelsius.setOnClickListener(click -> {
                if (!celsius.get()) {
                    binding.textDegree.setText(current.getTemp(true));
                    ((WeatherRecyclerViewAdapter) recyclerView.getAdapter()).updateTemps(true);
                    loadDailyWeather(info.getDaily(), true);
                    binding.buttonCelsius.setTextColor(
                            binding.buttonFahrenheit.getCurrentTextColor());
                    binding.buttonFahrenheit.setTextColor(Color.GRAY);
                    celsius.set(true);
                }
            });

            binding.buttonFahrenheit.setOnClickListener(click -> {
                if (celsius.get()) {
                    binding.textDegree.setText(current.getTemp(false));
                    ((WeatherRecyclerViewAdapter) recyclerView.getAdapter()).updateTemps(false);
                    loadDailyWeather(info.getDaily(), false);
                    binding.buttonFahrenheit.setTextColor(
                            binding.buttonCelsius.getCurrentTextColor());
                    binding.buttonCelsius.setTextColor(Color.GRAY);
                    celsius.set(false);
                }
            });

            binding.weatherWait.setVisibility(View.GONE);
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        onPrepareSpinner(menu.findItem(R.id.action_spinner), menu.findItem(R.id.action_favorite));
        menu.findItem(R.id.action_spinner).setVisible(true);
        menu.findItem(R.id.action_favorite).setVisible(true);
        menu.findItem(R.id.action_clear_searched).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) favoriteLocation();
        if (item.getItemId() == R.id.action_clear_searched) clearLocations();
        return super.onOptionsItemSelected(item);
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
                    R.layout.spinner_item, items);
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
    }

    private void clearLocations() {
        mWeatherModel.setLocation(mWeatherModel.getCurrent());
        mSavesModel.clearSearched();
    }

    private void loadDailyWeather(final List<Weather> daily, final boolean metric) {
        binding.textDayOne.setText(daily.get(0).getTime());
        binding.imageOne.setImageResource(daily.get(0).getIcon());
        binding.textWeatherOne.setText(daily.get(0).getTemp(metric));

        binding.textDayTwo.setText(daily.get(1).getTime());
        binding.imageTwo.setImageResource(daily.get(1).getIcon());
        binding.textWeatherTwo.setText(daily.get(1).getTemp(metric));

        binding.textDayThree.setText(daily.get(2).getTime());
        binding.imageThree.setImageResource(daily.get(2).getIcon());
        binding.textWeatherThree.setText(daily.get(2).getTemp(metric));

        binding.textDayFour.setText(daily.get(3).getTime());
        binding.imageFour.setImageResource(daily.get(3).getIcon());
        binding.textWeatherFour.setText(daily.get(3).getTemp(metric));

        binding.textDayFive.setText(daily.get(4).getTime());
        binding.imageFive.setImageResource(daily.get(4).getIcon());
        binding.textWeatherFive.setText(daily.get(4).getTemp(metric));

        binding.textDaySix.setText(daily.get(5).getTime());
        binding.imageSix.setImageResource(daily.get(5).getIcon());
        binding.textWeatherSix.setText(daily.get(5).getTemp(metric));

        binding.textDaySeven.setText(daily.get(6).getTime());
        binding.imageSeven.setImageResource(daily.get(6).getIcon());
        binding.textWeatherSeven.setText(daily.get(6).getTemp(metric));
    }
}