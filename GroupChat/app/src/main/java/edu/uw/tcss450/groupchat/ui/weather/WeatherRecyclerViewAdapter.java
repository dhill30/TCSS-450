package edu.uw.tcss450.groupchat.ui.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentHourlyBinding;

/**
 * The class describes how each Hourly Weather object should look on the weather page
 * and mange the list of all Hourly Weather object.
 *
 * @version November 19, 2020
 */
public class WeatherRecyclerViewAdapter extends
        RecyclerView.Adapter<WeatherRecyclerViewAdapter.WeatherViewHolder> {

    private final List<WeatherHourly> mHourly;

    /**
     * Constructor to initialize the list of hourly weather.
     *
     * @param items List of WeatherHourly object.
     */
    public WeatherRecyclerViewAdapter( List<WeatherHourly> items) {
        this.mHourly = items;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WeatherRecyclerViewAdapter.WeatherViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_hourly, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        holder.setWeather(mHourly.get(position));

    }


    @Override
    public int getItemCount() {
        return mHourly.size();
    }

    /**
     * The class describe how each Contact should look on the page.
     *
     * @version November 5
     */
    public class WeatherViewHolder extends RecyclerView.ViewHolder {
        /** The current View object of page. */
        public final View mView;

        /** Binding for view object */
        public FragmentHourlyBinding binding;

        private WeatherHourly mHour;

        /**
         * Initialize the ViewHolder.
         *
         * @param view current view context for page
         */
        public WeatherViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentHourlyBinding.bind(view);
        }

        /**
         * Initialize Contact object and populate binding.
         *
         * @param h Contact object
         */
        void setWeather(final WeatherHourly h) {
            mHour = h;
            int ch = h.getHour();
            String hourlyText = "    " + (ch % 12 == 0 ? 12 : ch % 12) + (ch / 12 == 0 ? "AM" : "PM");
            binding.hourText.setText(hourlyText);
            binding.tempText.setText("      " + h.getTemp() + "°");
            setImage(mHour.getID(), binding.conditionImage);

        }

        /**
         * Method to set the icons for weather condition
         * @param icon, the image id
         * @param v, the image view for weather condition
         */
        public void setImage(String icon, ImageView v) {
            if (icon.contentEquals("01d")) {
                v.setImageResource(R.drawable.ic_1d);
            } else if (icon.contentEquals("02d")) {
                v.setImageResource(R.drawable.ic_2d);
            } else if (icon.contentEquals("03d")) {
                v.setImageResource(R.drawable.ic_3d);
            } else if (icon.contentEquals("04d")) {
                v.setImageResource(R.drawable.ic_4d);
            } else if (icon.contentEquals("09d")) {
                v.setImageResource(R.drawable.ic_9d);
            } else if (icon.contentEquals("10d")) {
                v.setImageResource(R.drawable.ic_10d);

            } else if (icon.contentEquals("11d")) {
                v.setImageResource(R.drawable.ic_11d);

            } else if (icon.contentEquals("13d")) {
                v.setImageResource(R.drawable.ic_13d);

            } else if (icon.contentEquals("50d")) {
                v.setImageResource(R.drawable.ic_50d);

            } else if (icon.contentEquals("01n")) {
                v.setImageResource(R.drawable.ic_1n);

            } else if (icon.contentEquals("02n")) {
                v.setImageResource(R.drawable.ic_2n);

            } else if (icon.contentEquals("03n")) {
                v.setImageResource(R.drawable.ic_3n);

            } else if (icon.contentEquals("04n")) {
                v.setImageResource(R.drawable.ic_4n);

            } else if (icon.contentEquals("09n")) {
                v.setImageResource(R.drawable.ic_9n);

            } else if (icon.contentEquals("10n")) {
                v.setImageResource(R.drawable.ic_10n);

            } else if (icon.contentEquals("11n")) {
                v.setImageResource(R.drawable.ic_11n);

            } else if (icon.contentEquals("13n")) {
                v.setImageResource(R.drawable.ic_13n);

            } else if (icon.contentEquals("50n")) {
                v.setImageResource(R.drawable.ic_50n);

            }
        }
    }
}
