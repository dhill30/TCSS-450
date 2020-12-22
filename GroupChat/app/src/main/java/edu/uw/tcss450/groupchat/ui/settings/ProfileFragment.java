package edu.uw.tcss450.groupchat.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentProfileBinding;
import edu.uw.tcss450.groupchat.model.ProfileViewModel;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;

/**
 * Fragment for user profile page.
 *
 * @version December 2020
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel mProfileModel;

    /**
     * Required empty public constructor
     */
    public ProfileFragment() {
        // do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserInfoViewModel userModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
        mProfileModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        mProfileModel.connect(userModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentProfileBinding binding = FragmentProfileBinding.bind(view);

        binding.buttonChangePassword.setOnClickListener(click ->
                Navigation.findNavController(view)
                        .navigate(ProfileFragmentDirections
                                .actionNavigationChangePasswordToChangePasswordFragment()));

        mProfileModel.addProfileObserver(getViewLifecycleOwner(), profile -> {
            binding.textUserName.setText(profile.getName());
            binding.textUserUsername.setText(profile.getUsername());
            binding.textUserEmail.setText(profile.getEmail());
            binding.profileWait.setVisibility(View.GONE);
        });
    }
}