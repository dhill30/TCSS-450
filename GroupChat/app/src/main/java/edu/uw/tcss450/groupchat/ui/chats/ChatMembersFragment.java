package edu.uw.tcss450.groupchat.ui.chats;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentChatMembersBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatMembersViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatMembersFragment extends Fragment {

    private static final int MY_PERMISSIONS_STORAGE = 3124;

    private FragmentChatMembersBinding binding;

    private UserInfoViewModel mUserModel;

    private ChatMembersViewModel mMembersModel;

    private ChatRoomViewModel mRoomsModel;

    private ChatMembersFragmentArgs mArgs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
        mMembersModel = new ViewModelProvider(getActivity()).get(ChatMembersViewModel.class);
        mRoomsModel = new ViewModelProvider(getActivity()).get(ChatRoomViewModel.class);

        mArgs = ChatMembersFragmentArgs.fromBundle(getArguments());
        mMembersModel.connect(mArgs.getRoom().getId(), mUserModel.getJwt());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.swipeContainer.setRefreshing(true);

        final RecyclerView recyclerView = binding.listRoot;
        recyclerView.setAdapter(new ChatMembersRecyclerViewAdapter(getActivity(), new ArrayList<>(), mArgs));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divItemDecor);

        binding.swipeContainer.setOnRefreshListener(() ->
                mMembersModel.connect(mArgs.getRoom().getId(), mUserModel.getJwt()));

        mMembersModel.addMembersObserver(mArgs.getRoom().getId(), getViewLifecycleOwner(), members -> {
            recyclerView.setAdapter(new ChatMembersRecyclerViewAdapter(getActivity(), members, mArgs));
            binding.swipeContainer.setRefreshing(false);
            binding.membersWait.setVisibility(View.GONE);
        });

        mMembersModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                if (response.has("code")) {
                    try {
                        Log.e("Web Service Error",
                                response.getJSONObject("data").getString("message"));
                    } catch (JSONException e) {
                        Log.e("JSON Parse Error", e.getMessage());
                    }
                } else if (response.has("removed")) {
                    mMembersModel.connect(mArgs.getRoom().getId(), mUserModel.getJwt());
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if (mArgs.getAdmin()) {
            menu.findItem(R.id.action_chat_name).setVisible(true);
            menu.findItem(R.id.action_chat_image).setVisible(true);
            menu.findItem(R.id.action_chat_destroy).setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_chat_name) updateName();
        if (item.getItemId() == R.id.action_chat_image) updateImage();
        if (item.getItemId() == R.id.action_chat_destroy) destroyChat();
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream iStream = getContext().getContentResolver().openInputStream(imageUri);
                byte[] inputData = getBytes(iStream);
                mRoomsModel.uploadImage(binding, mArgs.getRoom(), inputData, mUserModel.getJwt());
                getActivity().findViewById(R.id.members_wait).setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accessStorage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
    }

    private void destroyChat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Destroy Chat Room");

        builder.setMessage("Destroying chat room '" + mArgs.getRoom().getName() + "'");

        builder.setPositiveButton("Confirm", (dlg, i) -> {});

        builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
            mMembersModel.connectDeleteChat(mArgs.getRoom().getId(), mUserModel.getJwt());
            Snackbar snack = Snackbar.make(getView(),
                    "Deleted the room '" + mArgs.getRoom().getName() + "'",
                    Snackbar.LENGTH_LONG);
            snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                    .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snack.setAnchorView(getActivity().findViewById(R.id.nav_view));
            snack.show();
            dialog.dismiss();
        });
    }

    private void updateName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update Chat Room Name");

        EditText chatName = new EditText(getContext());
        builder.setView(chatName);

        builder.setPositiveButton("Update", (dlg, i) -> {});

        builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(click -> {
            mRoomsModel.connectName(mArgs.getRoom(), chatName.getText().toString(), mUserModel.getJwt());

            mRoomsModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                if (response.length() > 0) {
                    if (response.has("code")) {
                        try {
                            chatName.setError("Error: "
                                    + response.getJSONObject("data").getString("message"));
                        } catch (JSONException e) {
                            Log.e("JSON Parse Error", e.getMessage());
                        }
                    } else {
                        mRoomsModel.connect(mUserModel.getJwt());
                        Navigation.findNavController(getView())
                                .getGraph()
                                .findNode(R.id.chatDisplayFragment)
                                .setLabel(chatName.getText().toString());
                        Snackbar snack = Snackbar.make(click, "You updated "
                                + chatName.getText().toString(), Snackbar.LENGTH_LONG);
                        snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                                .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        snack.setAnchorView(getActivity().findViewById(R.id.nav_view));
                        snack.show();
                        dialog.dismiss();
                    }
                } else {
                    Log.d("JSON Response", "No Response");
                }
            });
        });
    }

    private void updateImage() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_STORAGE);
        } else {
            accessStorage();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}