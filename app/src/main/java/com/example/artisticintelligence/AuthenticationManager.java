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
    private static final String TAG = "AuthenticationManager";
    private GoogleSignInClient googleSignInClient;
    private Context context;

    public AuthenticationManager(Context context) {
        this.context = context;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("106905994125-blt7hpufeifo2lt46gafm8frf9fmaed5.apps.googleusercontent.com")
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void signOut(OnCompleteListener<Void> callback) {
        googleSignInClient.signOut().addOnCompleteListener(callback);
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask, SignInCallback callback) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Sign-in successful: " + account.getEmail());
                callback.onSuccess(account);
            } else {
                callback.onError("Sign-in failed. Account is null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Sign-in error", e);
            callback.onError("Sign-in error: " + e.getMessage());
        }
    }

    public interface SignInCallback {
        void onSuccess(GoogleSignInAccount account);
        void onError(String errorMessage);
    }
}
