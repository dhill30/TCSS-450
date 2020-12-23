package edu.uw.tcss450.groupchat.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import edu.uw.tcss450.groupchat.R;

/**
 * View model for user's information.
 *
 * @version November 5
 */
public class UserInfoViewModel extends ViewModel {

    /** User email. */
    private String mEmail;

    /** User username. */
    private String mUsername;

    /** User login token. */
    private String mJwt;

    /** Current Theme **/
    private Integer mTheme;

    /** Current color Mode **/
    private Integer mMode;

    private UserInfoViewModel(String email, String username, String jwt) {
        mEmail = email;
        mUsername = username;
        mJwt = jwt;
        mTheme = R.style.Theme_PurpleGold;
        mMode = 0;
    }

    /**
     * Get user email.
     *
     * @return user email string
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Get user username.
     *
     * @return user username string
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Get user login.
     *
     * @return user login token string
     */
    public String getJwt() {
        return mJwt;
    }

    /**
     * Get app theme.
     *
     * @return current theme of the app
     */
    public int getTheme() {
        return mTheme;
    }

    /**
     * Get app mode.
     *
     * @return current mode of the app
     */
    public int getMode() {
        return mMode;
    }

    /**
     * Update the user's information.
     *
     * @param email the user's email
     * @param username the user's username
     * @param jwt the user's valid JWT
     */
    public void update(final String email, final String username, final String jwt) {
        mEmail = email;
        mUsername = username;
        mJwt = jwt;
    }

    /**
     * Set app theme.
     *
     * @param theme theme to change app to
     */
    public void setTheme(final int theme) {
        mTheme = theme;
    }

    /**
     * Set app mode.
     *
     * @param mode mode to switch to
     */
    public void setMode(final int mode) {
        mMode = mode;
    }

    /**
     * Utility Factory class for initializing UserInfoViewModel.
     */
    public static class UserInfoViewModelFactory implements ViewModelProvider.Factory {

        private final String email;
        private final String username;
        private final String jwt;

        /**
         * Main public constructor to initialize the Factory.
         *
         * @param email user email string
         * @param jwt user login token string
         */
        public UserInfoViewModelFactory(String email, String username, String jwt) {
            this.email = email;
            this.username = username;
            this.jwt = jwt;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass == UserInfoViewModel.class) {
                return (T) new UserInfoViewModel(email, username, jwt);
            }
            throw new IllegalArgumentException("Argument must be: " + UserInfoViewModel.class);
        }
    }

}
