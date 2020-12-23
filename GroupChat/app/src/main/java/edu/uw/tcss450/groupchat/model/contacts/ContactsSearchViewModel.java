package edu.uw.tcss450.groupchat.model.contacts;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.io.RequestQueueSingleton;
import edu.uw.tcss450.groupchat.ui.contacts.Contact;

/**
 * This view model holds a list of the searched potential contacts.
 *
 * @version December, 2020
 */
public class ContactsSearchViewModel extends ContactsViewModel {

    /**
     * Default constructor for this view model.
     * @param application reference to the current application
     */
    public ContactsSearchViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Makes a request to the web service to get a list of up to 10 random users.
     * @param jwt the user's signed JWT
     */
    public void connect(String jwt) {
        mContactType = 4;

        String url = getApplication().getResources().getString(R.string.base_url)
                + "contacts/search?term=%";

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleSuccess,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * Makes a request to the web service to search for users by the term.
     * @param jwt the user's signed JWT
     * @param term the term to search available users by
     */
    public void connect(final String jwt, final String term) {
        mContactType = 4;

        String url = getApplication().getResources().getString(R.string.base_url)
                + "contacts/search?term=" + term + "%";

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleSuccess,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * Makes a request to the web service to get a contact's username.
     * @param memberId the member id to get username from
     * @param jwt the user's signed JWT
     */
    public void connectContact(final int memberId, final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "profile/" + memberId;

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleUsername,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * Makes a request to the web service to initiate a contact request with the specified user.
     * @param jwt the user's signed JWT
     * @param name the username of the user to initiate a request with
     */
    public void connectAdd(final String jwt, final String name) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "contacts?name=" + name;

        Request request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                mResponse::setValue,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //add headers <key, value>
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    @Override
    protected void handleSuccess(final JSONObject result) {
        List<Contact> sorted = new ArrayList<>();
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
                    sorted.add(contact);
                }
            } else {
                Log.e("ERROR", "No contacts array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
        //sort the list of contacts alphabetically
        Collections.sort(sorted);
        mContacts.setValue(sorted);
    }
}
