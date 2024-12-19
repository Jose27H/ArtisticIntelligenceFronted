package com.example.artisticintelligence;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class Homepage extends AppCompatActivity {

    // Constants
    private static final int RC_SIGN_IN = 1001; // Request code for sign-in

    // UI components
    private SignInButton googleSignInButton;

    // Managers and utilities
    private NetworkSender networkSender;
    private AuthenticationManager authManager;

    // User information
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge design for this activity
        setContentView(R.layout.activity_homepage); // Set the layout for the homepage

        initializeViews(); // Initialize UI components
        initializeAuthManager(); // Initialize authentication manager
        initializeNetworkSender(); // Initialize network sender

        logout(); // Perform logout if necessary
    }

    private void initializeViews() {
        googleSignInButton = findViewById(R.id.google_sign_in_button); // Find the sign-in button

        // Set a click listener for the Google Sign-In button
        googleSignInButton.setOnClickListener(this::signIn);
    }

    private void initializeAuthManager() {
        authManager = new AuthenticationManager(this); // Create an instance of AuthenticationManager
    }

    private void initializeNetworkSender() {
        networkSender = new NetworkSender(); // Create an instance of NetworkSender
    }

    private void signIn(View v) {
        // Get the sign-in intent from the AuthenticationManager and start the activity for result
        Intent signInIntent = authManager.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // Handle the sign-in result
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            authManager.handleSignInResult(task, new AuthenticationManager.SignInCallback() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    // Display a welcome message
                    Toast.makeText(Homepage.this, "Welcome, " + account.getDisplayName(), Toast.LENGTH_LONG).show();
                    onLoginSuccess(account); // Handle login success
                }

                @Override
                public void onError(String errorMessage) {
                    // Display an error message
                    Toast.makeText(Homepage.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void onLoginSuccess(GoogleSignInAccount account) {
        // Hide the Google Sign-In button after successful login
        googleSignInButton.setVisibility(View.GONE);

        // Retrieve the ID token for backend communication
        String idToken = account.getIdToken();

        if (idToken != null) {
            sendMessageWithToken(idToken); // Send the token to the backend
        }
    }

    private void sendMessageWithToken(String token) {
        // Message to be sent to the backend
        String message = "Login Successful! Sending token to backend.";

        // Create a JSON payload with the message and token
        String jsonPayload = String.format("{\"message\": \"%s\", \"token\": \"%s\"}", message, token);

        // Use the NetworkSender to send the request
        networkSender.sendHttpRequest("/login", jsonPayload, null, new NetworkSender.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Parse the server response
                    JSONObject jsonResponse = new JSONObject(response);

                    // Extract the user ID from the response
                    userId = jsonResponse.getString("user_id");
                    navigateToWelcomeActivity(userId, token); // Navigate to the Welcome activity
                } catch (JSONException e) {
                    showToast("Error parsing server response: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                showToast("Error sending token: " + errorMessage); // Display an error message
            }
        });
    }

    private void showToast(final String message) {
        // Display a toast message on the UI thread
        runOnUiThread(() ->
                Toast.makeText(Homepage.this, message, Toast.LENGTH_LONG).show()
        );
    }

    private void logout() {
        // Log out the user and reset the UI
        authManager.signOut(task -> {
            if (task.isSuccessful()) {
                resetUIAfterLogout(); // Reset the UI
            } else {
                Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetUIAfterLogout() {
        // Show the Google Sign-In button after logout
        googleSignInButton.setVisibility(View.VISIBLE);
    }

    private void navigateToWelcomeActivity(String userid, String token) {
        // Navigate to the Welcome activity with user ID and token
        Intent intent = new Intent(this, Welcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
        intent.putExtra("USER_ID", userid);
        intent.putExtra("GOOGLE_TOKEN", token); // Pass the Google ID token
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Add transition animation
        startActivity(intent);
        finish(); // Finish the current activity
    }
}
