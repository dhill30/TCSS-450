package edu.uw.tcss450.groupchat.model.contacts;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.io.RequestQueueSingleton;
import edu.uw.tcss450.groupchat.ui.contacts.Contact;

/**
 * This abstract view model is the parent for the actual contacts view models.
 *
 * @version December, 2020
 */
public abstract class ContactsViewModel extends AndroidViewModel {

    protected MutableLiveData<List<Contact>> mContacts;

    protected MutableLiveData<JSONObject> mResponse;

    private MutableLiveData<String> mUsername;

    protected int mContactType;

    /**
     * Default constructor for this view model.
     * @param application reference to the current application
     */
    public ContactsViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mContacts = new MutableLiveData<>();
        mUsername = new MutableLiveData<>();
        mContactType = -1;
        initValues();
    }

    /**
     * Add an observer to the response object.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Add an observer to the list of contacts.
     * @param owner the fragment's LifecycleOwner
     * @param observer an observer to observe
     */
    public void addContactsObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super List<Contact>> observer) {
        mContacts.observe(owner, observer);
    }

    public void addUsernameObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super String> observer) {
        mUsername.observe(owner, observer);
    }

    /**
     * Returns the list of the user's contacts.
     * @return the list of contacts
     */
    public List<Contact> getContacts(){
        return mContacts.getValue();
    }

    /**
     * Returns the email of a contact from the passed username.
     * @param name the username of the contact
     * @return the email of the contact
     */
    public String getContactFromUserName(final String name) {
        for (Contact contact : mContacts.getValue()) {
            if (name.equals(contact.getUsername())) {
                return contact.getEmail();
            }
        }
        return "-1";
    }

    public void addContact(final Contact contact) {
        if (!mContacts.getValue().contains(contact)) {
            mContacts.getValue().add(0, contact);
        }
        mContacts.setValue(mContacts.getValue());
    }

    public void removeContact(final Contact contact) {
        mContacts.getValue().remove(contact);
        mContacts.setValue(mContacts.getValue());
    }

    /**
     * Makes a request to the web service to get the list of the user's contacts.
     * @param jwt the user's signed JWT
     */
    public abstract void connect(final String jwt);

    /**
     * Makes a request to the web service to get a contact's username.
     * @param contactId the contact id to get username from
     * @param jwt the user's signed JWT
     */
    public abstract void connectContact(final int contactId, final String jwt);

    protected void handleSuccess(final JSONObject result) {
        for (int i = 0; i < mContacts.getValue().size(); i++) {
            if (mContacts.getValue().get(i).getType() != 0) {
                mContacts.getValue().remove(mContacts.getValue().get(i));
            }
        }
        try {
            if (result.has("contacts")) {
                JSONArray contacts = result.getJSONArray("contacts");

                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject jsonContact = contacts.getJSONObject(i);
                    Contact contact = new Contact(jsonContact.getInt("contactid"),
                            jsonContact.getString("username"),
                            jsonContact.getString("name"),
                            jsonContact.getString("email"),
                            jsonContact.getString("image"),
                            mContactType);
                    mContacts.getValue().remove(contact);
                    mContacts.getValue().add(contact);
                }
            } else {
                Log.e("ERROR", "No contacts array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
        //sort the list of contacts alphabetically
        Collections.sort(mContacts.getValue());
        mContacts.setValue(mContacts.getValue());
    }

    protected void handleUsername(final JSONObject result) {
        try {
            if (result.has("username")) {
                mUsername.setValue(result.getString("username"));
            } else {
                Log.e("ERROR", "No contacts array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
    }

    protected void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            try {
                mResponse.setValue(new JSONObject("{" +
                        "error:\"" + error.getMessage() + "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
        else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            try {
                mResponse.setValue(new JSONObject("{" +
                        "code:" + error.networkResponse.statusCode +
                        ", data:" + data + "}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
    }

    private void initValues() {
        try {
            mResponse.setValue(new JSONObject("{\"init\":\"init\"}"));
        } catch (JSONException e) {
            Log.d("JSON Error", e.getMessage());
        }
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(0, "", "", "", "",-1));
        mContacts.setValue(contacts);
        mUsername.setValue("");
    }
}
