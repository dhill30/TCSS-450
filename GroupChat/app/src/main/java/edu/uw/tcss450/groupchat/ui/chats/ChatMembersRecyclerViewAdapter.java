package edu.uw.tcss450.groupchat.ui.chats;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentChatMemberCardBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatMembersViewModel;
import edu.uw.tcss450.groupchat.ui.contacts.Contact;

/**
 * RecyclerView for the chat members fragment.
 *
 * @version January, 2021
 */
public class ChatMembersRecyclerViewAdapter extends
        RecyclerView.Adapter<ChatMembersRecyclerViewAdapter.ChatMemberViewHolder> {

    private FragmentActivity mActivity;

    private List<Contact> mMembers;

    private UserInfoViewModel mUserModel;

    private ChatMembersViewModel mMembersModel;

    private ChatMembersFragmentArgs mArgs;

    public ChatMembersRecyclerViewAdapter(final FragmentActivity activity,
                                          final List<Contact> items,
                                          final ChatMembersFragmentArgs args) {
        mActivity = activity;
        mMembers = items;
        mArgs = args;

        mUserModel = new ViewModelProvider(activity).get(UserInfoViewModel.class);
        mMembersModel = new ViewModelProvider(activity).get(ChatMembersViewModel.class);
    }

    @NonNull
    @Override
    public ChatMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatMemberViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_chat_member_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMemberViewHolder holder, int position) {
        holder.setMember(mMembers.get(position));
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    public class ChatMemberViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public FragmentChatMemberCardBinding binding;

        private Contact mMember;

        public ChatMemberViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentChatMemberCardBinding.bind(view);
        }

        void setMember(final Contact member) {
            mMember = member;
            binding.textMemberUsername.setText(mMember.getUsername());
            binding.textMemberEmail.setText(mMember.getEmail());
            if (!mMember.getImage().isEmpty() && !mMember.getImage().equals("null")) {
                Glide.with(binding.textMemberUsername.getContext()).load(mMember.getImage())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_icon_24dp)
                        .into(binding.imageMember);
            } else {
                binding.imageMember.setImageResource(R.drawable.ic_profile_icon_24dp);
            }

            if (!mMember.getUsername().equals(mUserModel.getUsername()) && mArgs.getRoom().getAdmin()) {
                binding.imageMemberRemove.setVisibility(View.VISIBLE);
                binding.imageMemberRemove.setOnClickListener(this::removeFromRoom);
            }
        }

        private void removeFromRoom(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Kick Chat Member");

            builder.setMessage("Kicking chat member: " + mMember.getUsername());

            builder.setPositiveButton("Confirm", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
                mMembersModel.connectMember(mMember.getId(), mUserModel.getJwt());

                mMembersModel.addUsernameObserver(mActivity, username -> {
                    mMembersModel.connectRemoveUser(mArgs.getRoom().getId(), username, mUserModel.getJwt());
                    Snackbar snack = Snackbar.make(mView,
                            "Removed " + mMember.getUsername() + " from this chat",
                            Snackbar.LENGTH_LONG);
                    snack.setAnchorView(mActivity.findViewById(R.id.nav_view));
                    snack.show();
                    dialog.dismiss();
                });
            });
        }
    }
}
