package edu.uw.tcss450.groupchat.ui.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentContactCardBinding;
import edu.uw.tcss450.groupchat.model.ProfileViewModel;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.chats.ChatRoomViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsIncomingViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsMainViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsOutgoingViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsSearchViewModel;
import edu.uw.tcss450.groupchat.ui.chats.ChatRoom;

/**
 * The class describe how each Contact should look on the page and manage
 * the list of contacts.
 *
 * @version December 22, 2020
 */
public class ContactsRecyclerViewAdapter extends
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ContactViewHolder> {

    private List<Contact> mContacts;

    private ContactsMainViewModel mContactsModel;

    private ContactsIncomingViewModel mIncomingModel;

    private ContactsOutgoingViewModel mOutgoingModel;

    private ContactsSearchViewModel mSearchModel;

    private ChatRoomViewModel mChatRoomModel;

    private ProfileViewModel mProfileModel;

    private UserInfoViewModel mUserModel;

    private FragmentActivity mActivity;

    /**
     * Constructor to initialize the list of contacts.
     *
     * @param items List of Contact objects
     */
    public ContactsRecyclerViewAdapter(final List<Contact> items,
                                       final FragmentActivity activity) {
        mContacts = items;
        mActivity = activity;

        ViewModelProvider provider = new ViewModelProvider(activity);
        mContactsModel = provider.get(ContactsMainViewModel.class);
        mIncomingModel = provider.get(ContactsIncomingViewModel.class);
        mOutgoingModel = provider.get(ContactsOutgoingViewModel.class);
        mSearchModel = provider.get(ContactsSearchViewModel.class);
        mChatRoomModel = provider.get(ChatRoomViewModel.class);
        mProfileModel = provider.get(ProfileViewModel.class);
        mUserModel = provider.get(UserInfoViewModel.class);

        mChatRoomModel.connect(mUserModel.getJwt());
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_contact_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.setContact(mContacts.get(position));
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public void setList(final List<Contact> items) {
        mContacts = items;
        notifyDataSetChanged();
    }

    /**
     * Describes how each Contact should look on the page and provides
     * initialization of interactions.
     *
     * @version December 4, 2020
     */
    public class ContactViewHolder extends RecyclerView.ViewHolder {

        /** The current View object of the page. */
        public final View mView;

        /** The ViewBinding for view object */
        public FragmentContactCardBinding binding;

        private Contact mContact;

        /**
         * Initialize the ViewHolder.
         *
         * @param view current view context for page
         */
        public ContactViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentContactCardBinding.bind(view);
        }

        /**
         * Initialize Contact object and populate binding.
         *
         * @param contact Contact object
         */
        void setContact(final Contact contact) {
            mContact = contact;
            binding.textUsername.setText(mContact.getUsername());
            binding.textName.setText(mContact.getName());
            binding.textEmail.setText(mContact.getEmail());

            if (!mContact.getImage().isEmpty() && !mContact.getImage().equals("null"))
                Glide.with(binding.textName.getContext()).load(mContact.getImage())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_icon_24dp)
                        .into(binding.imageProfile);
            else {
                binding.imageProfile.setImageResource(R.drawable.ic_profile_icon_24dp);
            }

            binding.imageAdd.setVisibility(View.INVISIBLE);
            binding.imageRemove.setVisibility(View.INVISIBLE);
            binding.imageChat.setVisibility(View.INVISIBLE);
            binding.imageAccept.setVisibility(View.INVISIBLE);
            binding.imageReject.setVisibility(View.INVISIBLE);
            binding.imageClear.setVisibility(View.INVISIBLE);

            switch (mContact.getType()) {
                case 0:
                    binding.imageClear.setVisibility(View.VISIBLE);
                    binding.imageClear.setOnClickListener(click -> {
                        mContacts.remove(mContact);
                        notifyDataSetChanged();
                    });
                    break;
                case 1:
                    binding.imageChat.setVisibility(View.VISIBLE);
                    binding.imageChat.setOnClickListener(this::addUserToChat);
                    binding.imageRemove.setVisibility(View.VISIBLE);
                    binding.imageRemove.setOnClickListener(this::removeContact);
                    break;
                case 2:
                    binding.imageAccept.setVisibility(View.VISIBLE);
                    binding.imageAccept.setOnClickListener(this::acceptRequest);
                    binding.imageReject.setVisibility(View.VISIBLE);
                    binding.imageReject.setOnClickListener(this::rejectRequest);
                    break;
                case 3:
                    binding.imageRemove.setVisibility(View.VISIBLE);
                    binding.imageRemove.setOnClickListener(this::cancelRequest);
                    break;
                case 4:
                    binding.imageAdd.setVisibility(View.VISIBLE);
                    binding.imageAdd.setOnClickListener(this::sendContactRequest);
                    break;
                default:
                    Log.d("Contact Holder", "OnClickListener not set up properly");
                    break;
            }
        }

        private void addUserToChat(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Add to Chat Room");

            List<String> rooms = new ArrayList<>();
            for (ChatRoom room : mChatRoomModel.getRooms()) {
                rooms.add(room.getName());
            }
            String[] roomNames = rooms.toArray(new String[rooms.size()]);

            AtomicInteger selected = new AtomicInteger(-1);
            builder.setSingleChoiceItems(roomNames, selected.get(), (dlg, i) -> selected.set(i));

            builder.setPositiveButton("Add", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.dismiss());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(click -> {
                mContactsModel.connectContact(mContact.getId(), mUserModel.getJwt());

                mContactsModel.addUsernameObserver(mActivity, username -> {
                    int roomId = mChatRoomModel.getRoomFromName(roomNames[selected.get()]);
                    mContactsModel.connectAdd(mUserModel.getJwt(), username, roomId);
                    Snackbar snack = Snackbar.make(mView,
                            username + " has been added to " + roomNames[selected.get()],
                            Snackbar.LENGTH_LONG);
                    snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                            .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snack.show();
                    dialog.dismiss();
                });
            });
        }

        private void removeContact(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Remove Contact");

            builder.setMessage("Removing contact with: " + mContact.getUsername());

            builder.setPositiveButton("Confirm", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
                mContactsModel.connectContact(mContact.getId(), mUserModel.getJwt());

                mContactsModel.addUsernameObserver(mActivity, username -> {
                    mContactsModel.connectRemove(mUserModel.getJwt(), username);
                    mContactsModel.removeContact(mContact);
                    Snackbar snack = Snackbar.make(mView,
                            "Removed " + username + " from contacts",
                            Snackbar.LENGTH_LONG);
                    snack.setAnchorView(mActivity.findViewById(R.id.nav_view));
                    snack.show();
                    dialog.dismiss();
                });
            });
        }

        private void acceptRequest(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Accept Contact Request");

            builder.setMessage("Accepting request from: " + mContact.getUsername());

            builder.setPositiveButton("Confirm", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
                mIncomingModel.connectContact(mContact.getId(), mUserModel.getJwt());

                mIncomingModel.addUsernameObserver(mActivity, username -> {
                    mIncomingModel.connectAccept(mUserModel.getJwt(), username);
                    mIncomingModel.removeContact(mContact);
                    Snackbar snack = Snackbar.make(mView,
                            username + " added to Contacts",
                            Snackbar.LENGTH_LONG);
                    snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                            .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snack.setAnchorView(mActivity.findViewById(R.id.nav_view));
                    snack.show();
                    dialog.dismiss();
                });
            });
        }

        private void rejectRequest(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Reject Contact Request");

            builder.setMessage("Rejecting request from: " + mContact.getUsername());

            builder.setPositiveButton("confirm", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
                mIncomingModel.connectContact(mContact.getId(), mUserModel.getJwt());

                mIncomingModel.addUsernameObserver(mActivity, username -> {
                    mIncomingModel.connectReject(mUserModel.getJwt(), username);
                    mIncomingModel.removeContact(mContact);
                    Snackbar snack = Snackbar.make(mView,
                            "Rejected request from " + username,
                            Snackbar.LENGTH_LONG);
                    snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                            .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snack.setAnchorView(mActivity.findViewById(R.id.nav_view));
                    snack.show();
                    dialog.dismiss();
                });
            });
        }

        private void cancelRequest(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Cancel Contact Request");

            builder.setMessage("Canceling request to: " + mContact.getUsername());

            builder.setPositiveButton("Confirm", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
                mOutgoingModel.connectContact(mContact.getId(), mUserModel.getJwt());

                mOutgoingModel.addUsernameObserver(mActivity, username -> {
                    mOutgoingModel.connectCancel(mUserModel.getJwt(), username);
                    mOutgoingModel.removeContact(mContact);
                    Snackbar snack = Snackbar.make(mView,
                            "Canceled request to " + username,
                            Snackbar.LENGTH_LONG);
                    snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                            .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snack.setAnchorView(mActivity.findViewById(R.id.nav_view));
                    snack.show();
                    dialog.dismiss();
                });
            });
        }

        private void sendContactRequest(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Send Contact Request");

            builder.setMessage("Sending request to: " + mContact.getUsername());

            builder.setPositiveButton("Confirm", (dlg, i) -> {
                // do nothing b/c overridden
            });

            builder.setNegativeButton("Cancel", (dlg, i) -> dlg.cancel());

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(click -> {
                mSearchModel.connectContact(mContact.getId(), mUserModel.getJwt());

                mSearchModel.addUsernameObserver(mActivity, username -> {
                    mSearchModel.connectAdd(mUserModel.getJwt(), username);
                    Snackbar snack = Snackbar.make(mView,
                            "Sent request to " + username,
                            Snackbar.LENGTH_LONG);
                    snack.getView().findViewById(com.google.android.material.R.id.snackbar_text)
                            .setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snack.setAnchorView(mActivity.findViewById(R.id.nav_view));
                    snack.show();
                    dialog.dismiss();
                });
            });
        }
    }
}
