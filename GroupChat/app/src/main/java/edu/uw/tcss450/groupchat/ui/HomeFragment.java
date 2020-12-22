package edu.uw.tcss450.groupchat.ui;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentHomeBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;
import edu.uw.tcss450.groupchat.model.weather.CurrentLocationViewModel;
import edu.uw.tcss450.groupchat.model.weather.WeatherCurrentViewModel;
import edu.uw.tcss450.groupchat.ui.chats.ChatDetailedRecyclerViewAdapter;
import edu.uw.tcss450.groupchat.ui.chats.ChatRoom;

/**
 * Fragment for Home page.
 *
 * @version December 21, 2020
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private WeatherCurrentViewModel mWeatherModel;

    private CurrentLocationViewModel mLocationModel;

    private ChatRoomViewModel mRoomModel;

    private UserInfoViewModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeatherModel = new ViewModelProvider(getActivity()).get(WeatherCurrentViewModel.class);
        mLocationModel = new ViewModelProvider(getActivity()).get(CurrentLocationViewModel.class);
        mRoomModel = new ViewModelProvider(getActivity()).get(ChatRoomViewModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);

        mRoomModel.connectRecent(mUserModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textWelcome.setText("Welcome to Group Chat\nSigned in as: " + mUserModel.getUsername());

        final RecyclerView rv = binding.listRootHome;
        rv.setAdapter(new ChatDetailedRecyclerViewAdapter(new HashMap<>(), getActivity()));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(divItemDecor);

        mLocationModel.addLocationObserver(getViewLifecycleOwner(), location -> {
            mWeatherModel.connect(location.getLatitude(), location.getLongitude());
            binding.homeWait.setVisibility(View.VISIBLE);
        });

        mWeatherModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                if (response.has("code")) {
                    Log.d("Weather", "Invalid location");
                } else {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject info = (JSONObject) weather.get(0);

                        binding.textCity.setText(response.getString("name"));
                        binding.textCondition.setText(info.getString("main"));
                        setImage(info, binding.imageCondition);
                        binding.textDegree.setText((int) main.getDouble("temp") + " °F");

                        binding.homeWait.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        });

        mRoomModel.addRecentObserver(getViewLifecycleOwner(), chats -> {
            if (chats.size() != 1) {
                rv.setAdapter(new ChatDetailedRecyclerViewAdapter(chats, getActivity()));
            } else if (!chats.keySet().toArray()[0].equals(new ChatRoom(0, "init"))) {
                rv.setAdapter(new ChatDetailedRecyclerViewAdapter(chats, getActivity()));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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