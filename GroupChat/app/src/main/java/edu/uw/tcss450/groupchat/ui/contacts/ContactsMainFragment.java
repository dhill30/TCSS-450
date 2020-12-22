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
import edu.uw.tcss450.groupchat.databinding.FragmentContactsMainBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsMainViewModel;

/**
 * Fragment for default page of Contacts.
 *
 * @version December 4, 2020
 */
public class ContactsMainFragment extends Fragment {

    private ContactsMainViewModel mModel;

    private UserInfoViewModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(ContactsMainViewModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);

        mModel.connect(mUserModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContactsMainBinding binding = FragmentContactsMainBinding.bind(getView());

        binding.swipeContainer.setRefreshing(true);

        final RecyclerView recyclerView = binding.listRoot;
        recyclerView.setAdapter(new ContactsRecyclerViewAdapter(new ArrayList<>(), getActivity()));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divItemDecor);

        binding.swipeContainer.setOnRefreshListener(() ->
                mModel.connect(mUserModel.getJwt()));

        mModel.addContactsObserver(getViewLifecycleOwner(), contacts -> {
            Contact blank = new Contact("", "", "", "",0);
            if (contacts.contains(blank)) contacts.remove(blank);

            ((ContactsRecyclerViewAdapter) recyclerView.getAdapter()).setList(contacts);
            binding.swipeContainer.setRefreshing(false);
            binding.contactsMainWait.setVisibility(View.GONE);
        });
    }
}