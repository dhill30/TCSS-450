package edu.uw.tcss450.groupchat.ui.chats;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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
import edu.uw.tcss450.groupchat.databinding.FragmentChatMainBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatMembersViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;
import edu.uw.tcss450.groupchat.utils.PasswordValidator;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment for Home Page of the chats.
 * Displays list of chat rooms a user is member of.
 *
 * @version November 5
 */
public class ChatMainFragment extends Fragment implements View.OnClickListener {

    private static final int MY_PERMISSIONS_STORAGE = 3124;

    private FragmentChatMainBinding binding;

    private UserInfoViewModel mUserModel;

    private ChatRoomViewModel mRoomsModel;

    private ChatMembersViewModel mMembersModel;

    private ChatRoom mSelectedRoom;

    private PasswordValidator mChatNameValidator = PasswordValidator.checkPwdLength(1);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mRoomsModel = provider.get(ChatRoomViewModel.class);
        mUserModel = provider.get(UserInfoViewModel.class);
        mMembersModel = provider.get(ChatMembersViewModel.class);

        mRoomsModel.connect(mUserModel.getJwt());
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentChatMainBinding.bind(getView());
        binding.swipeContainer.setRefreshing(true);
        binding.chatWait.setVisibility(View.VISIBLE);

        final RecyclerView rv = binding.listRoot;
        rv.setAdapter(new ChatRoomRecyclerViewAdapter(new ArrayList<>(), this));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(divItemDecor);

        binding.swipeContainer.setOnRefreshListener(() ->
                mRoomsModel.connect(mUserModel.getJwt()));

        mRoomsModel.addResponseObserver(getViewLifecycleOwner(), response ->
                mRoomsModel.connect(mUserModel.getJwt()));

        mRoomsModel.addRoomsObserver(getViewLifecycleOwner(), rooms -> {
            if (rooms.size() != 1) {
                rv.setAdapter(new ChatRoomRecyclerViewAdapter(rooms, this));
                binding.swipeContainer.setRefreshing(false);
                binding.chatWait.setVisibility(View.GONE);
            } else if (!rooms.get(0).equals(new ChatRoom(0, "init", "", false))) {
                rv.setAdapter(new ChatRoomRecyclerViewAdapter(rooms, this));
                binding.swipeContainer.setRefreshing(false);
                binding.chatWait.setVisibility(View.GONE);
            }
        });

        binding.buttonStartChatRoom.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v == binding.buttonStartChatRoom){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Create New Chat Room");

            EditText chatName = new EditText(getContext());
            builder.setView(chatName);

            builder.setPositiveButton("Create", (dlg, i) -> {
                // do nothing because it's going to be overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String newName = chatName.getText().toString().trim();
                mChatNameValidator.processResult(
                        mChatNameValidator.apply(newName),
                        () -> {
                            mRoomsModel.connectCreate(mUserModel.getJwt(), newName);

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
                                        Snackbar snack = Snackbar.make(v, "You created "
                                                + newName, Snackbar.LENGTH_LONG);
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
                        },
                        error -> {
                            chatName.setError("Chat name must not be empty.");
                            binding.chatWait.setVisibility(View.GONE);
                        }
                );
            });
        }
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
                mRoomsModel.uploadImage(binding, mSelectedRoom, inputData, mUserModel.getJwt());
                getActivity().findViewById(R.id.chat_wait).setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSelectedRoom(final ChatRoom room) {
        mSelectedRoom = room;
    }

    public void destroyChat() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Destroy Chat Room");

        builder.setMessage("Destroying chat room '" + mSelectedRoom.getName() + "'");

        builder.setPositiveButton("Confirm", (dlg, i) -> {});

        builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

        final android.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
            mMembersModel.connectDeleteChat(mSelectedRoom.getId(), mUserModel.getJwt());
            Snackbar snack = Snackbar.make(getView(),
                    "Deleted the room '" + mSelectedRoom.getName() + "'",
                    Snackbar.LENGTH_LONG);
            snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                    .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snack.setAnchorView(getActivity().findViewById(R.id.nav_view));
            snack.show();
            dialog.dismiss();
        });
    }

    public void updateName() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update Chat Room Name");

        EditText chatName = new EditText(getContext());
        builder.setView(chatName);

        builder.setPositiveButton("Update", (dlg, i) -> {});

        builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

        final android.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(click -> {
            String newName = chatName.getText().toString().trim();
            mChatNameValidator.processResult(
                    mChatNameValidator.apply(newName),
                    () -> {
                        mRoomsModel.connectName(mSelectedRoom, newName, mUserModel.getJwt());

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
                                            .setLabel(newName);
                                    Snackbar snack = Snackbar.make(click, "You updated "
                                            + newName, Snackbar.LENGTH_LONG);
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
                    },
                    error -> {
                        chatName.setError("Chat name must not be empty.");
                        binding.chatWait.setVisibility(View.GONE);
                    }
            );
        });
    }

    public void updateImage() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_STORAGE);
        } else {
            accessStorage();
        }
    }

    private void accessStorage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
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