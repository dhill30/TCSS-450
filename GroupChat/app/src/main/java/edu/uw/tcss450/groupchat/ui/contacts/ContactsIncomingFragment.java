package edu.uw.tcss450.groupchat.ui.contacts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentContactsIncomingBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsIncomingViewModel;

/**
 * Fragment for incoming contact requests tab.
 *
 * @version December 2020
 */
public class ContactsIncomingFragment extends Fragment {

    private ContactsIncomingViewModel mModel;

    private UserInfoViewModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(ContactsIncomingViewModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);

        mModel.connect(mUserModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts_incoming, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContactsIncomingBinding binding = FragmentContactsIncomingBinding.bind(getView());

        binding.incomingSwipeContainer.setRefreshing(true);

        final RecyclerView recyclerView = binding.incomingListRoot;
        recyclerView.setAdapter(new ContactsRecyclerViewAdapter(new ArrayList<>(), getActivity()));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divItemDecor);

        binding.incomingSwipeContainer.setOnRefreshListener(() ->
                mModel.connect(mUserModel.getJwt()));

        mModel.addContactsObserver(getViewLifecycleOwner(), incoming -> {
            Contact blank = new Contact("", "", "", 0);
            if (incoming.contains(blank)) incoming.remove(blank);

            ((ContactsRecyclerViewAdapter) recyclerView.getAdapter()).setList(incoming);
            binding.incomingSwipeContainer.setRefreshing(false);
            binding.contactsIncomingWait.setVisibility(View.GONE);
        });
    }
}