package edu.uw.tcss450.groupchat.model.chats;

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
 * This view model holds the members of each chat room.
 *
 * @version December 10, 2020
 */
public class ChatMembersViewModel extends AndroidViewModel {

    private MutableLiveData<JSONObject> mResponse;

    private Map<Integer, MutableLiveData<List<Contact>>> mMembers;

    private MutableLiveData<String> mUsername;

    /**
     * Constructor for the view model.
     *
     * @param application the application this view model is part of
     */
    public ChatMembersViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>(new JSONObject());
        mMembers = new HashMap<>();
        mUsername = new MutableLiveData<>("");
    }

    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    public void addMembersObserver(final int chatId,
                                   @NonNull LifecycleOwner owner,
                                   @NonNull Observer<? super List<Contact>> observer) {
        getOrCreateMapEntry(chatId).observe(owner, observer);
    }

    public void addUsernameObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super String> observer) {
        mUsername.observe(owner, observer);
    }

    /**
     * Makes a request to the web service to get the list of chat room members.
     * Parses the response and adds the member emails to the List associated with the room.
     *
     * @param chatId the chat room id to request member of
     * @param jwt the user's signed JWT
     */
    public void connect(final int chatId, final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chats/" + chatId;

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleSuccess,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
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

    public void connectMember(final int memberId, final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chatrooms/username/" + memberId;

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleUsername,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
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

    public void connectRemoveUser(final int chatId, final String name, final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chatrooms/admin/" + chatId + "/" + name;

        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                mResponse::setValue,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
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

    public void connectDeleteChat(final int chatId, final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chatrooms/chat/" + chatId;

        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                mResponse::setValue,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
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

    private MutableLiveData<List<Contact>> getOrCreateMapEntry(final int chatId) {
        if (!mMembers.containsKey(chatId)) {
            mMembers.put(chatId, new MutableLiveData<>(new ArrayList<>()));
        }
        return mMembers.get(chatId);
    }

    private void handleSuccess(final JSONObject response) {
        if (!response.has("chatId")) {
            throw new IllegalStateException("Unexpected response in ChatMembersViewModel: " + response);
        }
        try {
            List<Contact> list = new ArrayList<>();
            JSONArray members = response.getJSONArray("rows");
            for (int i = 0; i < members.length(); i++) {
                JSONObject memberJson = members.getJSONObject(i);
                Contact member = new Contact(memberJson.getInt("memberid"),
                        memberJson.getString("username"),
                        memberJson.getString("name"),
                        memberJson.getString("email"),
                        memberJson.getString("image"),
                        6);
                list.add(member);
            }
            Collections.sort(list);
            getOrCreateMapEntry(response.getInt("chatId")).setValue(list);
        } catch (JSONException e) {
            Log.e("JSON PARSE ERROR", "Found in handle Success ChatMembersViewModel");
            Log.e("JSON PARSE ERROR", "Error: " + e.getMessage());
        }
    }

    public void handleUsername(final JSONObject response) {
        if (!response.has("username")) {
            throw new IllegalStateException("Unexpected response in ChatMembersViewModel: " + response);
        }
        try {
            String username = response.getString("username");
            mUsername.setValue(username);
        } catch (JSONException e) {
            Log.e("JSON PARSE ERROR", "Found in handle Username ChatMembersViewModel");
            Log.e("JSON PARSE ERROR", "Error: " + e.getMessage());
        }
    }

    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            try {
                mResponse.setValue(new JSONObject("{" +
                        "error:\"" + error.getMessage() +
                        "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
        else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            try {
                mResponse.setValue(new JSONObject("{" +
                        "code:" + error.networkResponse.statusCode +
                        ", data:" + data +
                        "}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
    }
}
