package edu.uw.tcss450.groupchat.model.weather;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {

    private MutableLiveData<Location> mLocation;

    public LocationViewModel() {
        mLocation = new MutableLiveData<>();
    }

    public void addLocationObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super Location> observer) {
        mLocation.observe(owner, observer);
    }

    public void setLocation(final Location location) {
        if (mLocation.getValue() == null
                || location.getLatitude() != mLocation.getValue().getLatitude()
                || location.getLongitude() != mLocation.getValue().getLongitude()) {
            mLocation.setValue(location);
        }
    }

    public Location getCurrentLocation() {
        return new Location(mLocation.getValue());
    }

}
