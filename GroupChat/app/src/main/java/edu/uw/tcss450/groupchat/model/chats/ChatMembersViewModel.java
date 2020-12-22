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

/**
 * This view model holds the members of each chat room.
 *
 * @version December 10, 2020
 */
public class ChatMembersViewModel extends AndroidViewModel {

    private Map<Integer, MutableLiveData<List<String>>> mMembers;

    /**
     * Constructor for the view model.
     *
     * @param application the application this view model is part of
     */
    public ChatMembersViewModel(@NonNull Application application) {
        super(application);
        mMembers = new HashMap<>();
    }

    /**
     * Return a reference to the List<> associated with the chat room. If the view model does not
     * have a mapping for this chatId, it will be created.
     *
     * @param chatId the id of the chat room List to retrieve
     * @return a reference to the list of member emails
     */
    public List<String> getMembersListByChatId(final int chatId) {
        return getOrCreateMapEntry(chatId).getValue();
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

    /**
     * When a chat member is received externally to this ViewModel, add it with this method.
     * @param chatId the chat room id to add to
     * @param email the email of the user to add
     */
    public void addMember(final int chatId, final String email) {
        List<String> list = getMembersListByChatId(chatId);
        if (!list.contains(email)) {
            list.add(email);
        }
        getOrCreateMapEntry(chatId).setValue(list);
    }

    private MutableLiveData<List<String>> getOrCreateMapEntry(final int chatId) {
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
            List<String> list = getMembersListByChatId(response.getInt("chatId"));
            JSONArray members = response.getJSONArray("rows");
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);
                String username = member.getString("username");
                if (!list.contains(username)) {
                    list.add(username);
                }
            }
            Collections.sort(list);
            getOrCreateMapEntry(response.getInt("chatId")).setValue(list);
        } catch (JSONException e) {
            Log.e("JSON PARSE ERROR", "Found in handle Success ChatMembersViewModel");
            Log.e("JSON PARSE ERROR", "Error: " + e.getMessage());
        }
    }

    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            Log.e("NETWORK ERROR", error.getMessage());
        }
        else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            Log.e("CLIENT ERROR",
                    error.networkResponse.statusCode + " " + data);
        }
    }
}
