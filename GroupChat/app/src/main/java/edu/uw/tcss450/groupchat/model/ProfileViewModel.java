package edu.uw.tcss450.groupchat.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.AuthFailureError;
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
import edu.uw.tcss450.groupchat.io.VolleyMultipartRequest;

public class ProfileViewModel extends AndroidViewModel {

    private MutableLiveData<Profile> mProfile;

    private MutableLiveData<JSONObject> mResponse;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        mProfile = new MutableLiveData<>();
        mResponse = new MutableLiveData<>(new JSONObject());
    }

    public void addProfileObserver(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<? super Profile> observer) {
        mProfile.observe(owner, observer);
    }

    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
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

    public void connectUpdate(final String first,
                              final String last,
                              final String username,
                              final String email,
                              final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "profile";

        JSONObject body = new JSONObject();
        try {
            body.put("first", first);
            body.put("last", last);
            body.put("username", username);
            body.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
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

    public void uploadImage(final byte[] data, final String jwt) {
        String url = "https://api.imgur.com/3/upload";

        //custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(new String(response.data));
                String imageURL = obj.getJSONObject("data").getString("link");
                changeImage(imageURL, jwt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        },
                error -> Log.e("IMAGE UPLOAD", error.toString())) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Client-ID bbf1ed520dda7f0");
                return params;
            }

            @Override
            public Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imageName = System.currentTimeMillis();
                params.put("image", new DataPart("" + imageName, data));
                return params;
            }
        };

        //adding the request to volley
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(volleyMultipartRequest);
    }

    private void changeImage(final String imageUrl, final String jwt) {
        String url = getApplication().getResources().getString(R.string.base_url)
                + "profile/image";

        JSONObject body = new JSONObject();
        try {
            body.put("image", imageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                r -> {
                    mProfile.getValue().setImage(imageUrl);
                    mProfile.setValue(mProfile.getValue());
                    },
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
            Profile profile = new Profile(response.getString("first"),
                    response.getString("last"),
                    response.getString("username"),
                    response.getString("email"),
                    response.getString("image"));
            mProfile.setValue(profile);
        } catch (JSONException e) {
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

    public class Profile {

        private final String mFirst;

        private final String mLast;

        private final String mUsername;

        private final String mEmail;

        private String mImage;

        private Profile(String first, String last, String username, String email, String image) {
            mFirst = first;
            mLast = last;
            mUsername = username;
            mEmail = email;
            mImage = image;
        }

        public String getFirst() {
            return mFirst;
        }

        public String getLast() {
            return mLast;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getEmail() {
            return mEmail;
        }

        public String getImage() {
            return mImage;
        }

        public void setImage(String image)  { mImage = image;}
    }
}
