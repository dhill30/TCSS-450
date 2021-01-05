package edu.uw.tcss450.groupchat.ui.chats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentChatCardBinding;
import edu.uw.tcss450.groupchat.model.chats.ChatNotificationsViewModel;

/**
 * The class describes how each chat room should look on the home page and manage the list of
 * chat rooms.
 *
 * @version November 27 2020
 */
public class ChatRoomRecyclerViewAdapter extends
        RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.RoomViewHolder> {

    private List<ChatRoom> mRooms;

    private FragmentActivity mActivity;

    private ChatNotificationsViewModel mNewChatModel;

    /**
     * Constructor initialize list of rooms.
     *
     * @param items List of ChatRoom objects visible to the user.
     */
    public ChatRoomRecyclerViewAdapter(List<ChatRoom> items, FragmentActivity activity) {
        mRooms = items;
        mActivity = activity;
        mNewChatModel = new ViewModelProvider(activity).get(ChatNotificationsViewModel.class);
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_chat_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.setRoom(mRooms.get(position));
    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    /**
     * The class describes how each Chatroom should look on the page.
     *
     * @version November 19 2020
     */
    class RoomViewHolder extends RecyclerView.ViewHolder {

        private final View mView;

        private ChatRoom mRoom;

        private FragmentChatCardBinding binding;

        /**
         * Initialize the ViewHolder.
         *
         * @param view current view context for page
         */
        public RoomViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentChatCardBinding.bind(view);
        }

        /**
         * Initialize ChatRoom object and populate binding.
         *
         * @param room ChatRoom object
         */
        void setRoom(final ChatRoom room) {
            mRoom = room;
            binding.labelName.setText(room.getName());
            binding.imageNotification.setVisibility(View.INVISIBLE);

            if (!mRoom.getImageUrl().isEmpty() && !mRoom.getImageUrl().equals("null")) {
                Glide.with(binding.labelName.getContext()).load(mRoom.getImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_icon_24dp)
                        .into(binding.imageRoom);
            } else {
                binding.imageRoom.setImageResource(R.drawable.ic_profile_icon_24dp);
            }

            //when someone clicks on a chat, takes to that chat list
            mView.setOnClickListener(view -> {
                NavController navController = Navigation.findNavController(mView);

                navController.getGraph().findNode(R.id.chatDisplayFragment).setLabel(mRoom.getName());
                navController.navigate(ChatMainFragmentDirections
                        .actionNavigationChatsToChatDisplayFragment(mRoom));
            });

            mNewChatModel.addMessageCountObserver(mActivity, notifications -> {
                int count = 0;
                if (notifications.containsKey(mRoom.getId())) {
                    count = notifications.get(mRoom.getId());
                }

                if(count > 0) {
                    //mew messages
                    binding.imageNotification.setVisibility(View.VISIBLE);
                } else {
                    //remove badge
                    binding.imageNotification.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
