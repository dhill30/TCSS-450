package edu.uw.tcss450.groupchat.ui.weather;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentWeatherMapBinding;
import edu.uw.tcss450.groupchat.model.weather.CurrentLocationViewModel;
import edu.uw.tcss450.groupchat.model.weather.SavedLocationsViewModel;

/**
 * Fragment for Google Maps service of weather.
 *
 * @version December 2020
 */
public class WeatherMapFragment extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private SavedLocationsViewModel mLocationsModel;

    private GoogleMap mMap;

    private Marker mMarker;

    private MenuItem mSearch;

    /**
     * Default public constructor.
     */
    public WeatherMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationsModel = new ViewModelProvider(getActivity()).get(SavedLocationsViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentWeatherMapBinding binding = FragmentWeatherMapBinding.bind(getView());

        binding.buttonAddLocation.setOnClickListener(this::getWeather);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        // add this fragment as the OnMapReadyCallback -> see onMapReady()
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        mSearch = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final Geocoder geocoder = new Geocoder(getContext());
                List<Address> results = null;
                try {
                    results = geocoder.getFromLocationName(query, 1);
                } catch (IOException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Invalid Location");
                    builder.setPositiveButton("OK", (dlg, i) -> dlg.dismiss());

                    final AlertDialog dialog = builder.show();
                    TextView message = dialog.findViewById(android.R.id.message);
                    message.setGravity(Gravity.CENTER);
                    dialog.show();
                }
                if (results.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Invalid Location");
                    builder.setPositiveButton("OK", (dlg, i) -> dlg.dismiss());

                    final AlertDialog dialog = builder.show();
                    TextView message = dialog.findViewById(android.R.id.message);
                    message.setGravity(Gravity.CENTER);
                    dialog.show();
                } else {
                    Address loc = results.get(0);
                    final LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                    if (mMarker != null) mMarker.remove();
                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(results.get(0).getAddressLine(0)));
                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    latLng, mMap.getCameraPosition().zoom));
                    mMarker.showInfoWindow();

                    searchView.setIconified(true);
                    mSearch.collapseActionView();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearch.setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        AtomicBoolean init = new AtomicBoolean(false);
        CurrentLocationViewModel model = new ViewModelProvider(getActivity()).get(CurrentLocationViewModel.class);
        model.addLocationObserver(getViewLifecycleOwner(), location -> {
            if (location != null) {
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.setMyLocationEnabled(true);

                if (!init.get()) {
                    final LatLng c = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(c, 15.0f));
                    init.set(true);
                }
            }
        });

        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("LAT/LONG", latLng.toString());

        final Geocoder geocoder = new Geocoder(getContext());
        String title = "";
        try {
            List<Address> adds = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (adds.size() == 0) {
                DecimalFormat df = new DecimalFormat("#0.00");
                title = "Unknown (" + df.format(latLng.latitude) + ", " + df.format(latLng.longitude) + ")";
            } else if (adds.get(0).getAddressLine(0) != null) {
                title = adds.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e("Geocoder Error", e.getMessage());
            e.printStackTrace();
        }

        if (mMarker != null) mMarker.remove();

        mMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title));

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        latLng, mMap.getCameraPosition().zoom));

        mMarker.showInfoWindow();
    }

    private void getWeather(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (mMarker == null) {
            Log.d("WEATHER", "No location selected on map");

            builder.setMessage("You have not selected a location!");

            builder.setPositiveButton("OK", (dlg, i) -> dlg.dismiss());
        } else {
            Log.d("WEATHER", mMarker.getPosition().toString());

            LatLng latLng = mMarker.getPosition();

            builder.setMessage("You are about to get weather for:\n" + mMarker.getTitle());

            builder.setPositiveButton("OK", (dlg, i) -> {
                ((SearchView) mSearch.getActionView()).setIconified(true);
                mSearch.collapseActionView();
                mLocationsModel.addLocation(new SavedLocation(
                        mMarker.getTitle(), latLng.latitude, latLng.longitude));
                WeatherMapFragmentDirections.ActionWeatherMapFragmentToNavigationWeather directions =
                        WeatherMapFragmentDirections.actionWeatherMapFragmentToNavigationWeather();
                directions.setLocationName(mMarker.getTitle());
                directions.setLocation(mMarker.getPosition());
                Navigation.findNavController(getView()).navigate(directions);
                dlg.dismiss();
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());
        }

        final AlertDialog dialog = builder.show();
        TextView message = dialog.findViewById(android.R.id.message);
        message.setGravity(Gravity.CENTER);
        dialog.show();
    }
}