package com.example.artisticintelligence;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class Homepage extends AppCompatActivity {
    private static final String TAG = "Homepage";

    private static final int RC_SIGN_IN = 1001;
    private SignInButton googleSignInButton;

    private NetworkSender networkSender;
    private AuthenticationManager authManager;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        initializeViews();
        initializeAuthManager();
        initializeNetworkSender();

        logout();
//        checkIfAlreadySignedIn();
    }



        private void initializeViews() {
            googleSignInButton = findViewById(R.id.google_sign_in_button);


            googleSignInButton.setOnClickListener(this:: signIn);

        }


    private void initializeAuthManager() {
        authManager = new AuthenticationManager(this);
    }

    private void initializeNetworkSender() {
        networkSender = new NetworkSender();
    }


    private void checkIfAlreadySignedIn() {
        GoogleSignInAccount account = authManager.getLastSignedInAccount();
        if (account != null) {
            Log.d(TAG, "User already signed in: " + account.getEmail());
            onLoginSuccess(account);
        } else {
            Log.d(TAG, "User not signed in. Showing Google Sign-In button.");
            googleSignInButton.setVisibility(View.VISIBLE);

        }
    }

    private void signIn(View v) {
        Intent signInIntent = authManager.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            authManager.handleSignInResult(task, new AuthenticationManager.SignInCallback() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    Toast.makeText(Homepage.this, "Welcome, " + account.getDisplayName() , Toast.LENGTH_LONG).show();
                    onLoginSuccess(account);
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(Homepage.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void onLoginSuccess(GoogleSignInAccount account) {
        // Hide the Google Sign-In button and show the Send Message button
        googleSignInButton.setVisibility(View.GONE);

        // Store the token to use in sendMessage
        String idToken = account.getIdToken();

        if (idToken != null) {
            sendMessageWithToken(idToken);

        }
    }



    private void sendMessageWithToken(String token) {
        String message = "Login Successful! Sending token to backend.";

        // Create the JSON payload
        String jsonPayload = String.format("{\"message\": \"%s\", \"token\": \"%s\"}", message, token);

        // Use the updated sendHttpRequest method
        networkSender.sendHttpRequest("/login", jsonPayload, null, new NetworkSender.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Parse the response JSON
                    JSONObject jsonResponse = new JSONObject(response);

                    // Extract the user_id
                 userId = jsonResponse.getString("user_id");
                 navigateToWelcomeActivity(userId, token);


                } catch (JSONException e) {
                    showToast("Error parsing server response: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                showToast("Error sending token: " + errorMessage);

            }
        });
    }


    private void showToast(final String message) {
        runOnUiThread(() ->
                Toast.makeText(Homepage.this, message, Toast.LENGTH_LONG).show()
        );
    }

    private void logout() {
        authManager.signOut(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                resetUIAfterLogout();
            } else {
                Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Reset UI to show the Google Sign-In button
    private void resetUIAfterLogout() {
        googleSignInButton.setVisibility(View.VISIBLE);


    }

    private void navigateToWelcomeActivity(String userid, String token) {
        Intent intent = new Intent(this, Welcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // added flags to the intent to clear the activity stack
        intent.putExtra("USER_ID", userid);
        intent.putExtra("GOOGLE_TOKEN", token);// Pass the Google ID as an extra
        startActivity(intent);
        finish();

    }
}