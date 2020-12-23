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
import android.widget.SearchView;

import java.util.ArrayList;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.databinding.FragmentContactsSearchBinding;
import edu.uw.tcss450.groupchat.model.UserInfoViewModel;
import edu.uw.tcss450.groupchat.model.contacts.ContactsSearchViewModel;

/**
 * Fragment for searching contacts tab.
 *
 * @version December 2020
 */
public class ContactsSearchFragment extends Fragment {

    private ContactsSearchViewModel mModel;

    private UserInfoViewModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(ContactsSearchViewModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);

        mModel.connect(mUserModel.getJwt());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContactsSearchBinding binding = FragmentContactsSearchBinding.bind(view);

        binding.searchSwipeContainer.setRefreshing(true);

        final RecyclerView recyclerView = binding.searchListRoot;
        recyclerView.setAdapter(new ContactsRecyclerViewAdapter(new ArrayList<>(), getActivity()));
        DividerItemDecoration divItemDecor =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divItemDecor);

        binding.searchSwipeContainer.setOnRefreshListener(() ->
                mModel.connect(mUserModel.getJwt(),
                        binding.searchUsers.getQuery().toString()));

        binding.searchUsers.setSubmitButtonEnabled(true);
        binding.searchUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mModel.connect(mUserModel.getJwt(), query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) mModel.connect(mUserModel.getJwt());
                return false;
            }
        });

        mModel.addContactsObserver(getViewLifecycleOwner(), searched -> {
            Contact blank = new Contact(0, "", "", "", "", -1);
            if (searched.contains(blank)) searched.remove(blank);

            ((ContactsRecyclerViewAdapter) recyclerView.getAdapter()).setList(searched);
            binding.searchSwipeContainer.setRefreshing(false);
            binding.contactsSearchWait.setVisibility(View.GONE);
        });
    }
}