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
    private final String mEmail;

    /** User login token. */
    private final String mJwt;

    /** Current Theme **/
    private MutableLiveData<Integer> mTheme;

    /** Current color Mode **/
    private MutableLiveData<Integer> mMode;

    private UserInfoViewModel(String email, String jwt) {
        mEmail = email;
        mJwt = jwt;
        mTheme = new MutableLiveData<>();
        mTheme.setValue(R.style.Theme_PurpleGold);
        mMode = new MutableLiveData<>(0);
    }

    public void addThemeObserver(@NonNull LifecycleOwner owner,
                                 @NonNull Observer<? super Integer> observer) {
        mTheme.observe(owner, observer);
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
        return mTheme.getValue();
    }

    public int getMode() {
        return mMode.getValue();
    }

    /**
     * Set app theme.
     *
     * @param theme theme to change app to
     */
    public void setTheme(int theme) {
        mTheme.setValue(theme);
    }

    public void setMode(int mode) {
        mMode.setValue(mode);
    }

    /**
     * Utility Factory class for initializing UserInfoViewModel.
     */
    public static class UserInfoViewModelFactory implements ViewModelProvider.Factory {
        private final String email;
        private final String jwt;

        /**
         * Main public constructor to initialize the Factory.
         *
         * @param email user email string
         * @param jwt user login token string
         */
        public UserInfoViewModelFactory(String email, String jwt) {
            this.email = email;
            this.jwt = jwt;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass == UserInfoViewModel.class) {
                return (T) new UserInfoViewModel(email, jwt);
            }
            throw new IllegalArgumentException("Argument must be: " + UserInfoViewModel.class);
        }
    }

}
