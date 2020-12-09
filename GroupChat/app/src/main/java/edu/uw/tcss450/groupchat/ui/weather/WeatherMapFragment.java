package edu.uw.tcss450.groupchat.ui.weather;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentWeatherMapBinding;
import edu.uw.tcss450.groupchat.model.weather.LocationViewModel;
import edu.uw.tcss450.groupchat.model.weather.WeatherSearchViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherMapFragment extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private WeatherSearchViewModel mWeatherModel;

    private LocationViewModel mModel;

    private GoogleMap mMap;

    private Marker mMarker;

    public WeatherMapFragment() {
        // Required empty public constructor
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

        mModel = new ViewModelProvider(getActivity()).get(LocationViewModel.class);

        mWeatherModel = new ViewModelProvider(getActivity()).get(WeatherSearchViewModel.class);

        binding.buttonWeather.setOnClickListener(button -> {
            if (mMarker != null) {
                LatLng latLng = mMarker.getPosition();
                mWeatherModel.connect(latLng.latitude, latLng.longitude);
            }
        });

        binding.buttonReset.setOnClickListener(button -> {
            if (mMarker != null) {
                mMarker.remove();
            }
            Location location = mModel.getCurrentLocation();
            mWeatherModel.connect(location.getLatitude(), location.getLongitude());
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        // add this fragment as the OnMapReadyCallback -> see onMapReady()
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationViewModel model = new ViewModelProvider(getActivity()).get(LocationViewModel.class);
        model.addLocationObserver(getViewLifecycleOwner(), location -> {
            if (location != null) {
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.setMyLocationEnabled(true);

                final LatLng c = new LatLng(location.getLatitude(), location.getLongitude());
                // Zoom levels are from 2.0f (zoomed out) to 21.0f (zoomed in)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(c, 15.0f));
            }
        });

        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("LAT/LONG", latLng.toString());

        if (mMarker != null) {
            mMarker.remove();
        }

        mMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("New Marker"));

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        latLng, mMap.getCameraPosition().zoom));
    }
}