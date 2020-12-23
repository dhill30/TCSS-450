package edu.uw.tcss450.groupchat.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentProfileBinding;
import edu.uw.tcss450.groupchat.model.ProfileViewModel;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.utils.PasswordValidator;

import static android.app.Activity.RESULT_OK;
import static edu.uw.tcss450.groupchat.utils.PasswordValidator.checkExcludeWhiteSpace;
import static edu.uw.tcss450.groupchat.utils.PasswordValidator.checkPwdDoNotInclude;
import static edu.uw.tcss450.groupchat.utils.PasswordValidator.checkPwdLength;
import static edu.uw.tcss450.groupchat.utils.PasswordValidator.checkPwdNoLongerThan;
import static edu.uw.tcss450.groupchat.utils.PasswordValidator.checkPwdSpecialChar;

/**
 * Fragment for user profile page.
 *
 * @version December 2020
 */
public class ProfileFragment extends Fragment {

    private static final int MY_PERMISSIONS_STORAGE = 3124;

    private ProfileViewModel mProfileModel;

    private UserInfoViewModel mUserModel;

    private FragmentProfileBinding binding;

    private final PasswordValidator mNameValidator = checkPwdLength(1)
            .and(checkPwdNoLongerThan(16));

    private final PasswordValidator mUsernameValidator = checkPwdLength(1)
            .and(checkPwdDoNotInclude("+"))
            .and(checkPwdNoLongerThan(16))
            .and(checkExcludeWhiteSpace());

    private final PasswordValidator mEmailValidator = checkPwdLength(2)
            .and(checkExcludeWhiteSpace())
            .and(checkPwdNoLongerThan(32))
            .and(checkPwdSpecialChar("@"));

    private ProfileViewModel.Profile mProfile;

    /**
     * Required empty public constructor
     */
    public ProfileFragment() {
        // do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
        mProfileModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        mProfileModel.connect(mUserModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonChangeImage.setOnClickListener(click -> {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_STORAGE);
            } else {
                // the user has already allowed the use storage, start the storage access
                accessStorage();
            }
        });

        binding.buttonChangePassword.setOnClickListener(click ->
                Navigation.findNavController(view)
                        .navigate(ProfileFragmentDirections
                                .actionNavigationChangePasswordToChangePasswordFragment()));

        binding.buttonEditInfo.setOnClickListener(this::editProfileInfo);

        binding.buttonSave.setOnClickListener(this::attemptProfileUpdate);

        binding.buttonCancel.setOnClickListener(this::finishProfileUpdate);

        mProfileModel.addProfileObserver(getViewLifecycleOwner(), profile -> {
            mProfile = profile;
            binding.textUserName.setText(profile.getFirst() + " " + profile.getLast());
            binding.textUserUsername.setText(profile.getUsername());
            binding.textUserEmail.setText(profile.getEmail());
            if (!profile.getImage().isEmpty() && !profile.getImage().equals("null"))
                Glide.with(this.getActivity()).load(profile.getImage())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_black_24dp)
                        .into(binding.imageProfileIcon);
            binding.profileWait.setVisibility(View.GONE);
        });

        mProfileModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                if (response.has("code")) {
                    binding.profileWait.setVisibility(View.GONE);
                    try {
                        String msg = response.getJSONObject("data").getString("message");
                        if (msg.equals("Username already taken")) {
                            binding.editProfileUsername.setError(msg);
                        } else if (msg.equals("Email in use")) {
                            binding.editProfileEmail.setError(msg);
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                } else {
                    try {
                        String email = response.getString("email");
                        String user = response.getString("username");
                        String jwt = response.getString("token");
                        mUserModel.update(email, user, jwt);

                        SharedPreferences prefs =
                                getActivity().getSharedPreferences(
                                        getString(R.string.keys_shared_prefs),
                                        Context.MODE_PRIVATE);
                        prefs.edit().putString(getString(R.string.keys_prefs_jwt), jwt).apply();

                        mProfileModel.connect(mUserModel.getJwt());
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                        binding.profileWait.setVisibility(View.GONE);
                    }
                    finishProfileUpdate(getView());
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_STORAGE:
            {
                // if request is cancelled, the result arrays are empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, do the tasks needed
                    accessStorage();
                } else {
                    // permission denied, disable the functionality that depends on this
                    Log.d("PERMISSION DENIED", "Nothing to see or do here.");
                }
            }
        }
    }

    /**
     * For handle selected image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();
            try {
                InputStream iStream = getContext().getContentResolver().openInputStream(imageUri);
                byte[] inputData = getBytes(iStream);
                mProfileModel.uploadImage(inputData, mUserModel.getJwt());
                getActivity().findViewById(R.id.profile_wait).setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accessStorage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
    }

    private void editProfileInfo(View view) {
        binding.editProfileFirst.setError(null);
        binding.editProfileLast.setError(null);
        binding.editProfileUsername.setError(null);
        binding.editProfileEmail.setError(null);

        binding.editProfileFirst.setText(mProfile.getFirst());
        binding.editProfileLast.setText(mProfile.getLast());
        binding.editProfileUsername.setText(binding.textUserUsername.getText().toString());
        binding.editProfileEmail.setText(binding.textUserEmail.getText().toString());

        binding.textUserName.setVisibility(View.GONE);
        binding.textUserUsername.setVisibility(View.GONE);
        binding.textUserEmail.setVisibility(View.GONE);
        binding.buttonChangePassword.setVisibility(View.GONE);
        binding.buttonEditInfo.setVisibility(View.GONE);

        binding.editProfileFirst.setVisibility(View.VISIBLE);
        binding.editProfileLast.setVisibility(View.VISIBLE);
        binding.editProfileUsername.setVisibility(View.VISIBLE);
        binding.editProfileEmail.setVisibility(View.VISIBLE);
        binding.buttonCancel.setVisibility(View.VISIBLE);
        binding.buttonSave.setVisibility(View.VISIBLE);
    }

    private void attemptProfileUpdate(View view) {
        String first = binding.editProfileFirst.getText().toString();
        String last = binding.editProfileLast.getText().toString();
        String user = binding.editProfileUsername.getText().toString().trim();
        String email = binding.editProfileEmail.getText().toString().trim();

        binding.editProfileFirst.setText(first.isEmpty() ? mProfile.getFirst() : first);
        binding.editProfileLast.setText(last.isEmpty() ? mProfile.getLast() : last);
        binding.editProfileUsername.setText(user.isEmpty() ? mProfile.getUsername() : user);
        binding.editProfileEmail.setText(email.isEmpty() ? mProfile.getEmail() : email);

        if (checkProfile()) finishProfileUpdate(view);
        else validateName();
    }

    private void finishProfileUpdate(View view) {
        binding.editProfileFirst.setVisibility(View.GONE);
        binding.editProfileLast.setVisibility(View.GONE);
        binding.editProfileUsername.setVisibility(View.GONE);
        binding.editProfileEmail.setVisibility(View.GONE);
        binding.buttonCancel.setVisibility(View.GONE);
        binding.buttonSave.setVisibility(View.GONE);

        binding.textUserName.setVisibility(View.VISIBLE);
        binding.textUserUsername.setVisibility(View.VISIBLE);
        binding.textUserEmail.setVisibility(View.VISIBLE);
        binding.buttonChangePassword.setVisibility(View.VISIBLE);
        binding.buttonEditInfo.setVisibility(View.VISIBLE);
    }

    private boolean checkProfile() {
        if (!binding.editProfileFirst.getText().toString().equals(mProfile.getFirst())) return false;
        if (!binding.editProfileLast.getText().toString().equals(mProfile.getLast())) return false;
        if (!binding.editProfileUsername.getText().toString().equals(mProfile.getUsername())) return false;
        return binding.editProfileEmail.getText().toString().equals(mProfile.getEmail());
    }

    private void validateName() {
        mNameValidator.processResult(
                mNameValidator.apply(binding.editProfileFirst.getText().toString()),
                () -> mNameValidator.processResult(
                        mNameValidator.apply(binding.editProfileLast.getText().toString()),
                        this::validateUsername,
                        result -> binding.editProfileLast.setError("Invalid last name.")),
                result -> binding.editProfileFirst.setError("Invalid first name."));
    }

    private void validateUsername() {
        mUsernameValidator.processResult(
                mUsernameValidator.apply(binding.editProfileUsername.getText().toString().trim()),
                this::validateEmail,
                result -> binding.editProfileUsername.setError("Invalid username."));
    }

    private void validateEmail() {
        mEmailValidator.processResult(
                mEmailValidator.apply(binding.editProfileEmail.getText().toString().trim()),
                () -> {
                    String first = binding.editProfileFirst.getText().toString();
                    String last = binding.editProfileLast.getText().toString();
                    String user = binding.editProfileUsername.getText().toString().trim();
                    String email = binding.editProfileEmail.getText().toString().trim();
                    mProfileModel.connectUpdate(first, last, user, email, mUserModel.getJwt());
                    binding.profileWait.setVisibility(View.VISIBLE);
                },
                result -> binding.editProfileEmail.setError("Invalid email address."));
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}