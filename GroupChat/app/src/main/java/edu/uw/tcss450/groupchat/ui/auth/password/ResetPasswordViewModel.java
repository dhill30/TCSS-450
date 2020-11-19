package edu.uw.tcss450.groupchat.ui.auth.password;

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
import java.util.Objects;

import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.io.RequestQueueSingleton;

/**
 * View Model for Reset Password page to store latest HTTP response.
 *
 * @version November 18
 */
public class ResetPasswordViewModel extends AndroidViewModel {

    private MutableLiveData<JSONObject> mResponse;

    /**
     * Main default constructor for View Model.
     *
     * @param application reference to the current application
     */
    public ResetPasswordViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mResponse.setValue(new JSONObject());
    }

    /**
     * Add observer for receiving server's responses.
     *
     * @param owner The LifeCycle owner that will control the observer
     * @param observer The observer that will receive the events
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Make an HTTP request for reset password action.
     * @param email email of user
     */
    public void connect(final String email) {
        String url = getApplication().getResources().getString(R.string.base_url) +
                "auth";

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                mResponse::setValue,
                this::handleError) {

//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> headers = new HashMap<>();
//                // add headers <key,value>
//                String credentials = email;
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(),
//                        Base64.NO_WRAP);
//                headers.put("Authorization", auth);
//                return headers;
//            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
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
            String data = new String(error.networkResponse.data, Charset.defaultCharset())
                    .replace('\"', '\'');
            try {
                JSONObject response = new JSONObject();
                response.put("code", error.networkResponse.statusCode);
                response.put("data", new JSONObject(data));
                mResponse.setValue(response);
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
    }

}
