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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.io.RequestQueueSingleton;
import edu.uw.tcss450.groupchat.ui.chats.ChatMessage;
import edu.uw.tcss450.groupchat.ui.chats.ChatRoom;

/**
 * View Model for the list of chat rooms, recent and all.
 *
 * @version December, 2020
 */
public class ChatRoomViewModel extends AndroidViewModel {

    private MutableLiveData<JSONObject> mResponse;

    private MutableLiveData<List<ChatRoom>> mRooms;

    private MutableLiveData<Map<ChatRoom, ChatMessage>> mRecent;

    private MutableLiveData<Integer> mCurrentRoom;

    private MutableLiveData<HashMap<Integer, HashSet<String>>> mTypingCount;

    /**
     * Main default constructor the this ViewModel.
     *
     * @param application reference to the current application.
     */
    public ChatRoomViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>(new JSONObject());
        mRooms = new MutableLiveData<>();
        mRecent = new MutableLiveData<>();
        initRooms();
        mCurrentRoom = new MutableLiveData<>(-1);
        mTypingCount = new MutableLiveData<>(new HashMap<>());
    }

    /**
     * Add an observer to the response object.
     *
     * @param owner the LifecycleOwner object of the chat room
     * @param observer an observer to observe
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Add an observer to the main list of rooms.
     *
     * @param owner the LifecycleOwner object of the chat room
     * @param observer an observer to observe
     */
    public void addRoomsObserver(@NonNull LifecycleOwner owner,
                                 @NonNull Observer<? super List<ChatRoom>> observer) {
        mRooms.observe(owner, observer);
    }

    /**
     * Add an observer to the list of recent rooms.
     *
     * @param owner the LifecycleOwner object of the chat room
     * @param observer an observer to observe
     */
    public void addRecentObserver(@NonNull LifecycleOwner owner,
                                 @NonNull Observer<? super Map<ChatRoom, ChatMessage>> observer) {
        mRecent.observe(owner, observer);
    }

    /**
     * Add an observer to the current room object.
     *
     * @param owner the LifecycleOwner object of the chat room
     * @param observer an observer to observe
     */
    public void addCurrentRoomObserver(@NonNull LifecycleOwner owner,
                                       @NonNull Observer<? super Integer> observer) {
        mCurrentRoom.observe(owner, observer);
    }

    /**
     * Returns the chat id of the current chat room.
     * @return chat id of current room
     */
    public int getCurrentRoom() {
        return mCurrentRoom.getValue();
    }

    /**
     * Returns the list of ChatRoom objects.
     * @return list of chat rooms
     */
    public List<ChatRoom> getRooms() {
        return mRooms.getValue();
    }

    /**
     * Returns the chat id of the room given its name.
     *
     * @param name name of the chat room to get
     * @return chat id of chat room
     */
    public int getRoomFromName(final String name) {
        for (ChatRoom room : mRooms.getValue()) {
            if (name.equals(room.getName())) {
                return room.getId();
            }
        }
        return -1;
    }

    /**
     * Sets the current chat room id.
     * @param id chat id of current room
     */
    public void setCurrentRoom(final int id) {
        mCurrentRoom.setValue(id);
    }

    /**
     * Makes a request to the web service to get the list of chat rooms available to the user.
     *
     * @param jwt the user's signed JWT
     */
    public void connect(final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chatrooms";

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleRooms,
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
     * Makes a request to the web service to get the most recently updated chat rooms.
     *
     * @param jwt the user's signed JWT
     */
    public void connectRecent(final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chatrooms/recent";

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleRecent,
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
     * Makes a request to the web service to create a new chat room.
     *
     * @param jwt the user's signed JWT
     * @param name the name of the chat room to create
     */
    public void connectCreate(final String jwt, final String name) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chats";

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body, //push token found in the JSONObject body
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

    /**
     * Makes a request to the web service to add a user to a chat room.
     *
     * @param jwt the user's signed JWT
     * @param name the name of the user to add
     * @param chatId the chat room id to add to
     */
    public void connectAddToChat(final String jwt, final String name, final int chatId) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chats/" + chatId + "/" + name;

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
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    /**
     * Makes a request to the web service to remove the user from a chat room.
     *
     * @param jwt the user's signed JWT
     * @param roomId the chat id of the room to leave
     */
    public void connectLeave(final String jwt, final int roomId){
        String url = getApplication().getResources().getString(R.string.base_url)
                + "chats/" + roomId;

        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                mResponse::setValue,
                this::handleError) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    private void handleRooms(final JSONObject result) {
        List<ChatRoom> sorted = new ArrayList<>();
        try {
            if (result.has("rows")) {
                JSONArray rooms = result.getJSONArray("rows");

                for (int i = 0; i < rooms.length(); i++) {
                    JSONObject jsonRoom = rooms.getJSONObject(i);
                    ChatRoom room = new ChatRoom(
                            jsonRoom.getInt("chatid"),
                            jsonRoom.getString("name"));
                    sorted.add(room);
                }
            } else {
                Log.e("ERROR", "No rows array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
        Collections.sort(sorted);
        mRooms.setValue(sorted);
    }

    private void handleRecent(final JSONObject result) {
        Map<ChatRoom, ChatMessage> chats = new HashMap<>();
        try {
            if (result.has("chats")) {
                JSONArray rooms = result.getJSONArray("chats");

                for (int i = 0; i < rooms.length(); i++) {
                    JSONObject jsonRoom = rooms.getJSONObject(i);
                    ChatRoom room = new ChatRoom(
                            jsonRoom.getInt("chatid"),
                            jsonRoom.getString("name"));
                    ChatMessage message = new ChatMessage(
                            jsonRoom.getInt("messageid"),
                            jsonRoom.getString("message"),
                            jsonRoom.getString("username"),
                            jsonRoom.getString("timestamp"));
                    chats.put(room, message);
                }
            } else {
                Log.e("ERROR", "No chats array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
        Map<ChatRoom, ChatMessage> recent = chats.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        mRecent.setValue(recent);
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

    private void initRooms() {
        List<ChatRoom> rooms = new ArrayList<>();
        rooms.add(new ChatRoom(0, "init"));
        mRooms.setValue(rooms);

        Map<ChatRoom, ChatMessage> recent = new HashMap<>();
        recent.put(new ChatRoom(0, "init"), new ChatMessage(0, "", "", ""));
        mRecent.setValue(recent);
    }

    public Set<String> addTyper(String user, int chatId) {
        if (!mTypingCount.getValue().containsKey(chatId))
            mTypingCount.getValue().put(chatId, new HashSet<>(Collections.singleton(user)));
        else
            mTypingCount.getValue().get(chatId).add(user);
        return mTypingCount.getValue().get(chatId);
    }

    public Set<String> removeTyper(String user, int chatId) {
        if (mTypingCount.getValue().containsKey(chatId))
            mTypingCount.getValue().get(chatId).remove(user);
        return mTypingCount.getValue().get(chatId);
    }
}
