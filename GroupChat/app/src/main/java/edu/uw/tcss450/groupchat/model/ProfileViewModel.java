package edu.uw.tcss450.groupchat.model;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.io.RequestQueueSingleton;

public class ProfileViewModel extends AndroidViewModel {

    private MutableLiveData<Profile> mProfile;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        mProfile = new MutableLiveData<>();
    }

    public void addProfileObserver(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<? super Profile> observer) {
        mProfile.observe(owner, observer);
    }

    public void connect(final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "profile";

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

    private void handleSuccess(final JSONObject response) {
        if (!response.has("success")) {
            throw new IllegalStateException("Unexpected response in ProfileViewModel: " + response);
        }
        try {
            Profile profile = new Profile(response.getString("name"),
                    response.getString("username"),
                    response.getString("email"));
            mProfile.setValue(profile);
        } catch (JSONException e) {
            Log.e("JSON PARSE ERROR", "Error: " + e.getMessage());
        }
    }

    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            Log.e("NETWORK ERROR", error.getMessage());
        }
        else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            Log.e("CLIENT ERROR", error.networkResponse.statusCode + " " + data);
        }
    }

    public class Profile {

        private final String mName;

        private final String mUsername;

        private final String mEmail;

        private Profile(String name, String username, String email) {
            mName = name;
            mUsername = username;
            mEmail = email;
        }

        public String getName() {
            return mName;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getEmail() {
            return mEmail;
        }
    }
}
