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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import edu.uw.tcss450.groupchat.databinding.FragmentHomeBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;
import edu.uw.tcss450.groupchat.model.weather.CurrentLocationViewModel;
import edu.uw.tcss450.groupchat.model.weather.WeatherHomeViewModel;
import edu.uw.tcss450.groupchat.ui.chats.ChatDetailedRecyclerViewAdapter;
import edu.uw.tcss450.groupchat.ui.chats.ChatRoom;

/**
 * Fragment for Home page.
 *
 * @version December 21, 2020
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private WeatherHomeViewModel mWeatherModel;

    private CurrentLocationViewModel mLocationModel;

    private ChatRoomViewModel mRoomModel;

    private UserInfoViewModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeatherModel = new ViewModelProvider(getActivity()).get(WeatherHomeViewModel.class);
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

        mWeatherModel.addWeatherObserver(getViewLifecycleOwner(), weather -> {
            binding.textCity.setText(mWeatherModel.getName());
            binding.textCondition.setText(weather.getMain());
            binding.imageCondition.setImageResource(weather.getIcon());
            binding.textDegree.setText(weather.getTemp(false) + "°F");
            binding.homeWait.setVisibility(View.GONE);
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
}