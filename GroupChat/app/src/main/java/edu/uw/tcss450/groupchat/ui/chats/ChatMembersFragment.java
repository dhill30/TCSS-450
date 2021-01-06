package edu.uw.tcss450.groupchat.ui.chats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import java.util.ArrayList;

import edu.uw.tcss450.groupchat.databinding.FragmentChatMembersBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatMembersViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;

/**
 * Fragment for displaying chat room members and admin controls.
 *
 * @version January, 2021
 */
public class ChatMembersFragment extends Fragment {

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
}