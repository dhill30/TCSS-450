package edu.uw.tcss450.groupchat.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentSettingsBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;

/**
 * Fragment for application settings page.
 *
 * @version December 2020
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private UserInfoViewModel mUserViewModel;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserViewModel = (new ViewModelProvider(getActivity())).get(UserInfoViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_prefs_theme))) {
            int theme = prefs.getInt(getString(R.string.keys_prefs_theme), -1);

            switch (theme) {
                case 1:
                    binding.settingsColorIg.setChecked(true);
                    mUserViewModel.setTheme(R.style.Theme_IndigoGreen);
                    break;
                case 2:
                    binding.settingsColorGo.setChecked(true);
                    mUserViewModel.setTheme(R.style.Theme_GreyOrange);
                    break;
                case 0:
                default:
                    binding.settingsColorPg.setChecked(true);
                    mUserViewModel.setTheme(R.style.Theme_PurpleGold);
                    break;
            }
        } else {
            binding.settingsColorPg.setChecked(true);
        }

        if (prefs.contains(getString(R.string.keys_prefs_mode))) {
            int mode = prefs.getInt(getString(R.string.keys_prefs_mode), -1);

            switch (mode) {
                case 1:
                    binding.settingsModeDark.setChecked(true);
                    mUserViewModel.setMode(1);
                    break;
                case 0:
                default:
                    binding.settingsModeLight.setChecked(true);
                    mUserViewModel.setMode(0);
                    break;
            }
        } else {
            binding.settingsModeLight.setChecked(true);
        }
    }
}