package edu.uw.tcss450.groupchat.ui.weather;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private final List<Weather> mHourly;

    private boolean mMetric;

    /**
     * Constructor to initialize the list of hourly weather.
     *
     * @param items List of WeatherHourly object.
     */
    public WeatherRecyclerViewAdapter(List<Weather> items) {
        mHourly = items;
        mMetric = false;
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
     * Updates weather forecast temps to use either fahrenheit or celsius.
     * @param metric whether or not to display temps in metric
     */
    public void updateTemps(final boolean metric) {
        mMetric = metric;
        notifyDataSetChanged();
    }

    /**
     * The class describe how each Contact should look on the page.
     *
     * @version November 5
     */
    public class WeatherViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public FragmentHourlyBinding binding;

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
         * Initialize Weather object and populate binding.
         *
         * @param weather the Weather object
         */
        void setWeather(final Weather weather) {
            SpannableString time = new SpannableString(weather.getTime());
            if (weather.equals(mHourly.get(0))) {
                time = new SpannableString("Now");
                time.setSpan(new RelativeSizeSpan(1.2f), 0, 3, 0);
            } else {
                if (time.length() == 3) time.setSpan(new RelativeSizeSpan(1.2f), 0, 1, 0);
                else time.setSpan(new RelativeSizeSpan(1.2f), 0, 2, 0);
            }
            binding.textHourlyName.setText(time);
            binding.imageHourlyCondition.setImageResource(weather.getIcon());
            binding.textHourlyTemp.setText(weather.getTemp(mMetric));
        }
    }
}
