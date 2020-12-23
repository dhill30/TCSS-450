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
import edu.uw.tcss450.groupchat.databinding.FragmentContactsOutgoingBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsOutgoingViewModel;

/**
 * Fragment for outgoing contact requests tab.
 *
 * @version December 2020
 */
public class ContactsOutgoingFragment extends Fragment {

    private ContactsOutgoingViewModel mModel;

    private UserInfoViewModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(ContactsOutgoingViewModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);

        mModel.connect(mUserModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts_outgoing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContactsOutgoingBinding binding = FragmentContactsOutgoingBinding.bind(getView());

        binding.outgoingSwipeContainer.setRefreshing(true);

        final RecyclerView recyclerView = binding.outgoingListRoot;
        recyclerView.setAdapter(new ContactsRecyclerViewAdapter(new ArrayList<>(), getActivity()));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divItemDecor);

        binding.outgoingSwipeContainer.setOnRefreshListener(() ->
                mModel.connect(mUserModel.getJwt()));

        mModel.addContactsObserver(getViewLifecycleOwner(), outgoing -> {
            Contact blank = new Contact(0, "", "", "", "", 0);
            if (outgoing.contains(blank)) outgoing.remove(blank);

            ((ContactsRecyclerViewAdapter) recyclerView.getAdapter()).setList(outgoing);
            binding.outgoingSwipeContainer.setRefreshing(false);
            binding.contactsOutgoingWait.setVisibility(View.GONE);
        });
    }
}