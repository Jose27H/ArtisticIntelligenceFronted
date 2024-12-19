package com.example.artisticintelligence;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AuthenticationManager {

    // Constants
    private static final String TAG = "AuthenticationManager"; // Log tag for debugging

    // Google Sign-In client and context
    private GoogleSignInClient googleSignInClient;
    private Context context;

    public AuthenticationManager(Context context) {
        this.context = context; // Initialize context

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Request user's email
                .requestIdToken("106905994125-blt7hpufeifo2lt46gafm8frf9fmaed5.apps.googleusercontent.com") // Request ID token
                .build();

        // Initialize Google Sign-In client with the options
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        // Return an intent to start the Google Sign-In process
        return googleSignInClient.getSignInIntent();
    }

    public void signOut(OnCompleteListener<Void> callback) {
        // Sign out the user and notify via the provided callback
        googleSignInClient.signOut().addOnCompleteListener(callback);
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask, SignInCallback callback) {
        try {
            // Attempt to retrieve the signed-in account from the task
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                // Sign-in successful; log and notify the callback
                Log.d(TAG, "Sign-in successful: " + account.getEmail());
                callback.onSuccess(account);
            } else {
                // Account is null; notify error via callback
                callback.onError("Sign-in failed. Account is null.");
            }
        } catch (Exception e) {
            // Handle sign-in errors
            Log.e(TAG, "Sign-in error", e);
            callback.onError("Sign-in error: " + e.getMessage());
        }
    }

    // Callback interface for handling sign-in events
    public interface SignInCallback {
        void onSuccess(GoogleSignInAccount account); // Called on successful sign-in
        void onError(String errorMessage); // Called on sign-in error
    }
}
