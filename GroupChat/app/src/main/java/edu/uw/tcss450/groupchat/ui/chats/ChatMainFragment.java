package edu.uw.tcss450.groupchat.ui.chats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.util.ArrayList;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentChatMainBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;

/**
 * Fragment for Home Page of the chats.
 * Displays list of chat rooms a user is member of.
 *
 * @version November 5
 */
public class ChatMainFragment extends Fragment implements View.OnClickListener {

    private ChatRoomViewModel mModel;

    private UserInfoViewModel mUserModel;

    private FragmentChatMainBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mModel = provider.get(ChatRoomViewModel.class);
        mUserModel = provider.get(UserInfoViewModel.class);

        mModel.connect(mUserModel.getJwt());
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
        rv.setAdapter(new ChatRoomRecyclerViewAdapter(new ArrayList<>(), getActivity()));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(divItemDecor);

        binding.swipeContainer.setOnRefreshListener(() ->
                mModel.connect(mUserModel.getJwt()));

        mModel.addResponseObserver(getViewLifecycleOwner(), response ->
                mModel.connect(mUserModel.getJwt()));

        mModel.addRoomsObserver(getViewLifecycleOwner(), rooms -> {
            if (rooms.size() != 1) {
                rv.setAdapter(new ChatRoomRecyclerViewAdapter(rooms, getActivity()));
                binding.swipeContainer.setRefreshing(false);
                binding.chatWait.setVisibility(View.GONE);
            } else if (!rooms.get(0).equals(new ChatRoom(0, "init", ""))) {
                rv.setAdapter(new ChatRoomRecyclerViewAdapter(rooms, getActivity()));
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
                mModel.connectCreate(mUserModel.getJwt(), chatName.getText().toString());

                mModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                    if (response.length() > 0) {
                        if (response.has("code")) {
                            try {
                                chatName.setError("Error: "
                                        + response.getJSONObject("data").getString("message"));
                            } catch (JSONException e) {
                                Log.e("JSON Parse Error", e.getMessage());
                            }
                        } else {
                            mModel.connect(mUserModel.getJwt());
                            Snackbar snack = Snackbar.make(v, "You created "
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
    }
}