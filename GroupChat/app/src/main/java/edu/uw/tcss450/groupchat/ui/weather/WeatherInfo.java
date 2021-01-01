package edu.uw.tcss450.groupchat.ui.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data for the entire weather forecast, hourly and daily.
 *
 * @version December 31, 2020
 */
public class WeatherInfo {

    private final Weather mCurrent;
    private final List<Weather> mHourly;
    private final List<Weather> mDaily;

    private final String mName;
    private final String mTimezone;

    /**
     * Constructor for this class, initializes the entire object.
     * @param current the current weather information
     * @param hourly the hourly weather forecast
     * @param daily the daily weather forecast
     * @param timezone the forecast timezone
     * @param name the location name
     * @throws JSONException an error when creating the weather data
     */
    public WeatherInfo(JSONObject current, JSONArray hourly, JSONArray daily, String timezone, String name) throws JSONException {
        mName = name;
        mTimezone = timezone;
        mCurrent = createCurrentFromJson(current);
        mHourly = createHourlyFromJson(hourly);
        mDaily = createDailyFromJson(daily);
    }

    /**
     * Returns the name of the weather forecast location.
     * @return name of location
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the current weather information.
     * @return current weather info
     */
    public Weather getCurrent() {
        return mCurrent;
    }

    /**
     * Returns the hourly forecast information
     * @return hourly forecast info
     */
    public List<Weather> getHourly() {
        return mHourly;
    }

    /**
     * Returns the daily forecast information
     * @return daily forecast info
     */
    public List<Weather> getDaily() {
        return mDaily;
    }

    private Weather createCurrentFromJson(final JSONObject current) throws JSONException {
        final JSONArray weather = current.getJSONArray("weather");
        return new Weather("current",
                current.getLong("dt"),
                mTimezone,
                weather.getJSONObject(0).getString("main"),
                weather.getJSONObject(0).getString("icon"),
                current.getDouble("temp"),
                current.getInt("humidity"),
                current.getDouble("wind_speed"));
    }

    private List<Weather> createHourlyFromJson(final JSONArray hourly) throws JSONException {
        List<Weather> forecast = new ArrayList<>();
        for (int i = 1; i < 25; i++) {
            final JSONObject hour = hourly.getJSONObject(i);
            final JSONArray weather = hour.getJSONArray("weather");
            forecast.add(new Weather("hourly",
                    hour.getLong("dt"),
                    mTimezone,
                    weather.getJSONObject(0).getString("main"),
                    weather.getJSONObject(0).getString("icon"),
                    hour.getDouble("temp"),
                    hour.getInt("humidity"),
                    hour.getDouble("wind_speed")));
        }
        return forecast;
    }

    private List<Weather> createDailyFromJson(final JSONArray daily) throws JSONException {
        List<Weather> forecast = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            final JSONObject day = daily.getJSONObject(i);
            final JSONArray weather = day.getJSONArray("weather");
            final JSONObject temp = day.getJSONObject("temp");
            Weather dayWeather = new Weather("daily",
                    day.getLong("dt"),
                    mTimezone,
                    weather.getJSONObject(0).getString("main"),
                    weather.getJSONObject(0).getString("icon"),
                    temp.getDouble("day"),
                    day.getInt("humidity"),
                    day.getDouble("wind_speed"));
            dayWeather.setTemp(temp.getDouble("min"), temp.getDouble("max"));
            forecast.add(dayWeather);
        }
        return forecast;
    }
}
