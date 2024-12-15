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

public class Homepage extends AppCompatActivity {
    private static final String TAG = "Homepage";

    private static final int RC_SIGN_IN = 1001;
    private SignInButton googleSignInButton;
    private Button sendButton;
    private NetworkSender networkSender;
    private AuthenticationManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

//        setupWindowInsets();
        initializeViews();
        initializeAuthManager();
        initializeNetworkSender();
        checkIfAlreadySignedIn();
    }

//    private void setupWindowInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }


        private void initializeViews() {
            googleSignInButton = findViewById(R.id.google_sign_in_button);
            sendButton = findViewById(R.id.button);

            googleSignInButton.setOnClickListener(this:: signIn);
            sendButton.setOnClickListener(this::onClick);
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
            sendButton.setVisibility(View.GONE);
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
        sendButton.setVisibility(View.VISIBLE);

        // Store the token to use in sendMessage
        String idToken = account.getIdToken();
        if (idToken != null) {
            sendMessageWithToken(idToken);
        }
    }

    public void onClick(View v) {
        sendButton.setEnabled(false);
        sendMessage();
    }

    private void sendMessageWithToken(String token) {
        String message = "Login Successful! Sending token to backend.";

        networkSender.sendHttpRequest("/message", message, token, new NetworkSender.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                showToast("Backend Response: " + response);
            }

            @Override
            public void onError(String errorMessage) {
                showToast("Error sending token: " + errorMessage);
            }
        });
    }
    private void sendMessage() {
        String message = "u a bitch";
        String token = "your-token-here"; // Replace with actual token if needed

        networkSender.sendHttpRequest("/message", message, token, new NetworkSender.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                showToast("Success: " + response);
                runOnUiThread(() -> sendButton.setEnabled(true));
            }

            @Override
            public void onError(String errorMessage) {
                showToast("Error: " + errorMessage);
                runOnUiThread(() -> sendButton.setEnabled(true));
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(() ->
                Toast.makeText(Homepage.this, message, Toast.LENGTH_LONG).show()
        );
    }
}