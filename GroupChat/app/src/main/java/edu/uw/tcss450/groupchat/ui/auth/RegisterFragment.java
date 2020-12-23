package edu.uw.tcss450.groupchat.ui.auth;

import static edu.uw.tcss450.groupchat.utils.PasswordValidator.*;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.groupchat.databinding.FragmentRegisterBinding;
import edu.uw.tcss450.groupchat.model.auth.RegisterViewModel;
import edu.uw.tcss450.groupchat.utils.PasswordValidator;

/**
 * Fragment for Register page.
 *
 * @version November 5
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    private RegisterViewModel mRegisterModel;

    private final PasswordValidator mNameValidator = checkPwdLength(1);

    private final PasswordValidator mEmailValidator = checkPwdLength(2)
            .and(checkExcludeWhiteSpace())
            .and(checkPwdSpecialChar("@"));

    private final PasswordValidator mUsernameValidator = checkPwdLength(1)
            .and(checkPwdDoNotInclude("+"))
            .and(checkExcludeWhiteSpace());

    private final PasswordValidator mPasswordValidator =
                    checkPwdLength(7)
                    .and(checkPwdDigit())
                    .and(checkPwdSpecialChar())
                    .and(checkExcludeWhiteSpace())
                    .and(checkPwdLowerCase().or(checkPwdUpperCase()))
                    .and(checkClientPredicate(pwd -> pwd.equals(binding.editPassword2.getText().toString())));

    /**
     * Default constructor.
     */
    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRegisterModel = new ViewModelProvider(getActivity())
                .get(RegisterViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonRegister.setOnClickListener(this::attemptRegister);
        mRegisterModel.addResponseObserver(getViewLifecycleOwner(),
                this::observeResponse);
    }

    private void attemptRegister(final View button) {
        binding.registerWait.setVisibility(View.VISIBLE);
        validateFirst();
    }

    private void validateFirst() {
        mNameValidator.processResult(
                mNameValidator.apply(binding.editFirst.getText().toString().trim()),
                this::validateLast,
                result -> {
                    String msg = "First name " + getValidationErrorMsg(result, 1, "");
                    binding.editFirst.setError(msg);
                    binding.registerWait.setVisibility(View.GONE);
                });
    }

    private void validateLast() {
        mNameValidator.processResult(
                mNameValidator.apply(binding.editLast.getText().toString().trim()),
                this::validateUsername,
                result -> {
                    String msg = "Last name " + getValidationErrorMsg(result, 1, "");
                    binding.editLast.setError(msg);
                    binding.registerWait.setVisibility(View.GONE);
                });
    }

    private void validateUsername() {
        mUsernameValidator.processResult(
                mUsernameValidator.apply(binding.editUsername.getText().toString().trim()),
                this::validateEmail,
                result -> {
                    String msg = "Username " + getValidationErrorMsg(result, 1, "+");
                    binding.editUsername.setError(msg);
                    binding.registerWait.setVisibility(View.GONE);
                });
    }

    private void validateEmail() {
        mEmailValidator.processResult(
                mEmailValidator.apply(binding.editEmail.getText().toString().trim()),
                this::validatePassword,
                result -> {
                    binding.editEmail.setError("Please enter a valid Email address.");
                    binding.registerWait.setVisibility(View.GONE);
                });
    }

    private void validatePassword() {
        mPasswordValidator.processResult(
                mPasswordValidator.apply(binding.editPassword1.getText().toString()),
                this::verifyAuthWithServer,
                result -> {
                    String msg = "Password " + getValidationErrorMsg(result, 7, "");
                    if (result == ValidationResult.PWD_CLIENT_ERROR)
                        binding.editPassword2.setError(msg);
                    else
                        binding.editPassword1.setError(msg);
                    binding.registerWait.setVisibility(View.GONE);
                });
    }

    private void verifyAuthWithServer() {
        mRegisterModel.connect(
                binding.editFirst.getText().toString(),
                binding.editLast.getText().toString(),
                binding.editUsername.getText().toString(),
                binding.editEmail.getText().toString(),
                binding.editPassword1.getText().toString());
        //This is an Asynchronous call. No statements after should rely on the result
    }

    private void navigateToLogin() {
        RegisterFragmentDirections.ActionRegisterFragmentToSignInFragment directions =
                RegisterFragmentDirections.actionRegisterFragmentToSignInFragment();

        directions.setEmail(binding.editEmail.getText().toString());
        directions.setPassword(binding.editPassword1.getText().toString());
        directions.setVerify("Please check your email to verify your account.");

        Navigation.findNavController(getView()).navigate(directions);
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to SignInViewModel.
     *
     * @param response the Response from the server
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            binding.registerWait.setVisibility(View.GONE);
            if (response.has("code")) {
                try {
                    String error = response.getJSONObject("data").getString("message");
                    if (error.equals("Username exists")) {
                        binding.editUsername.setError("Error Authenticating: " + error);
                    } else {
                        binding.editEmail.setError("Error Authenticating: " + error);
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            } else {
                navigateToLogin();
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }

    private String getValidationErrorMsg(ValidationResult error, int requiredLength, String excludeChar) {
        String result;
        switch (error) {
            case PWD_MISSING_DIGIT:
                result = "must include a digit";
                break;
            case PWD_INVALID_LENGTH:
                result = "length must be greater than " + requiredLength;
                break;
            case PWD_MISSING_LOWER:
                result = "must include alphabetical character";
                break;
            case PWD_MISSING_SPECIAL:
                result = "must include a special character";
                break;
            case PWD_INCLUDES_WHITESPACE:
                result = "must not include any whitespace";
                break;
            case PWD_INCLUDES_EXCLUDED:
                result = "must not include characters: " + excludeChar;
                break;
            default:
                result = "must match.";
                break;
        }
        return result;
    }
}