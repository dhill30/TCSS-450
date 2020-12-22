package edu.uw.tcss450.groupchat.ui.settings;

import android.Manifest;
import android.content.Intent;
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
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentProfileBinding;
import edu.uw.tcss450.groupchat.model.ProfileViewModel;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment for user profile page.
 *
 * @version December 2020
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel mProfileModel;

    private static final int MY_PERMISSIONS_STORAGE = 3124;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                mProfileModel.uploadImage(inputData,
                        (new ViewModelProvider(getActivity())).get(UserInfoViewModel.class).getJwt(),
                        this, this.getParentFragmentManager());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        mProfileModel.addProfileObserver(getViewLifecycleOwner(), profile -> {
            binding.textUserName.setText(profile.getName());
            binding.textUserUsername.setText(profile.getUsername());
            binding.textUserEmail.setText(profile.getEmail());
            if (!profile.getImage().isEmpty() && !profile.getImage().equals("null"))
                Glide.with(this.getActivity()).load(profile.getImage())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_black_24dp)
                        .into(binding.imageProfileIcon);
            binding.profileWait.setVisibility(View.GONE);
        });
    }

    private void accessStorage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
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