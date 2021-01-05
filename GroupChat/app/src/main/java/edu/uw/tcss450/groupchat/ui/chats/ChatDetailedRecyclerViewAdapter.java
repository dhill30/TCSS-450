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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentChatDetailedBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatMessageViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatNotificationsViewModel;
import edu.uw.tcss450.groupchat.ui.HomeFragmentDirections;

/**
 * This class is a Recycler View Adapter for chats
 * displays a list of chat rooms a user is part of
 * will display latest message and sender i.e. chat room details.
 *
 * @version December 10, 2020
 */
public class ChatDetailedRecyclerViewAdapter extends
        RecyclerView.Adapter<ChatDetailedRecyclerViewAdapter.DetailedViewHolder> {

    private List<ChatRoom> mRooms;

    private List<ChatMessage> mMessages;

    private FragmentActivity mActivity;

    private ChatNotificationsViewModel mNewChatModel;

    private ChatMessageViewModel mMessageModel;

    private UserInfoViewModel mUserModel;

    /**
     * Constructor for recyclerview adapter
     * @param chats maps messages in a room
     * @param activity
     */
    public ChatDetailedRecyclerViewAdapter(Map<ChatRoom, ChatMessage> chats, FragmentActivity activity) {
        mRooms = new ArrayList<>();
        mMessages = new ArrayList<>();
        for (Map.Entry<ChatRoom, ChatMessage> entry : chats.entrySet()) {
            mRooms.add(entry.getKey());
            mMessages.add(entry.getValue());
        }
        mActivity = activity;
        mNewChatModel = new ViewModelProvider(activity).get(ChatNotificationsViewModel.class);
        mMessageModel = new ViewModelProvider(activity).get(ChatMessageViewModel.class);
        mUserModel = new ViewModelProvider(activity).get(UserInfoViewModel.class);
    }


    @NonNull
    @Override
    public DetailedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetailedViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_chat_detailed, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DetailedViewHolder holder, int position) {
        holder.setChat(mRooms.get(position), mMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    /**
     * ViewHolder for  this class's recyclerview adapter
     */
    class DetailedViewHolder extends RecyclerView.ViewHolder {

        private final View mView;

        private ChatRoom mRoom;

        private FragmentChatDetailedBinding binding;

        /**
         * Initialize the ViewHolder.
         *
         * @param view current view context for the page
         */
        public DetailedViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentChatDetailedBinding.bind(view);
        }

        /**
         * Sets how the chat messages are displayed
         * @param room chat room
         * @param message messages sent/received
         */
        void setChat(final ChatRoom room, final ChatMessage message) {
            mRoom = room;
            mMessageModel.getFirstMessages(room.getId(), mUserModel.getJwt());

            //set label text to chatroom name
            binding.labelChatName.setText(room.getName());
            binding.textMessageName.setText(message.getSender());
            if (message.getMessage().length() > 25) {
                binding.textMessageBody.setText(message.getMessage().substring(0, 25) + "...");
            } else {
                binding.textMessageBody.setText(message.getMessage());
            }
            binding.textMessageTime.setText(getLocalTime(message.getTimeStamp()));
            binding.imageNotification.setVisibility(View.INVISIBLE);

            if (!mRoom.getImageUrl().isEmpty() && !mRoom.getImageUrl().equals("null")) {
                Glide.with(binding.labelChatName.getContext()).load(mRoom.getImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_icon_24dp)
                        .into(binding.imageRoom);
            } else {
                binding.imageRoom.setImageResource(R.drawable.ic_profile_icon_24dp);
            }

            //navigate to the selected chat room
            mView.setOnClickListener(view -> {
                NavController navController = Navigation.findNavController(mView);

                navController.getGraph().findNode(R.id.chatDisplayFragment).setLabel(mRoom.getName());
                navController.navigate(HomeFragmentDirections
                        .actionNavigationHomeToChatDisplayFragment(mRoom));
            });

            //keeps track of notifications
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

        /**
         * Method gets and formats time of a message sent/received
         * @param timeStamp
         * @return
         */
        private String getLocalTime(final String timeStamp) {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat other = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
            SimpleDateFormat today = new SimpleDateFormat("h:mm a", Locale.getDefault());
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            other.setTimeZone(TimeZone.getDefault());
            today.setTimeZone(TimeZone.getDefault());

            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();

            String time = "";
            try {
                Date temp = in.parse(timeStamp);
                in.setTimeZone(TimeZone.getDefault());
                Date date = in.parse(in.format(temp));
                if (now.getTime() - date.getTime() > (24 * 60 * 60 * 1000)) {
                    time = other.format(date);
                } else {
                    time = today.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return time;
        }
    }
}
