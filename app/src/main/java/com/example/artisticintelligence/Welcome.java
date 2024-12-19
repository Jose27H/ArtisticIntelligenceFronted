package com.example.artisticintelligence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Welcome extends AppCompatActivity {

    // User information
    String userId;
    String authToken;

    // UI components
    TextView nameView;
    Button generateImageButton;
    Button viewGeneratedImageButton;
    Button viewImagesFromCommunityButton;

    // Network manager
    private static final String TAG = "Welcome";
    private NetworkSender networkSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge design
        setContentView(R.layout.activity_welcome); // Set the layout for the Welcome activity

        setListeners(); // Initialize and set listeners for UI components

        // Retrieve user information from intent extras
        userId = getIntent().getStringExtra("USER_ID");
        authToken = getIntent().getStringExtra("GOOGLE_TOKEN");

        networkSender = new NetworkSender(); // Initialize NetworkSender instance

        loadName(); // Fetch and display the user's name
    }

    private void setListeners() {
        // Bind UI components
        nameView = findViewById(R.id.header_text);
        generateImageButton = findViewById(R.id.generate_image_button);
        viewGeneratedImageButton = findViewById(R.id.view_generated_images_button);
        viewImagesFromCommunityButton = findViewById(R.id.view_community_images_button);

        // Set click listeners for the buttons
        generateImageButton.setOnClickListener(this::clickGenerateImage);
        viewGeneratedImageButton.setOnClickListener(this::clickViewGeneratedImages);
        viewImagesFromCommunityButton.setOnClickListener(this::clickViewImagesFromCommunity);
    }

    private void clickGenerateImage(View v) {
        // Navigate to the PromptActivity
        Intent intent = new Intent(this, PromptActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GOOGLE_TOKEN", authToken); // Pass the Google ID token
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Add transition animation
        startActivity(intent);
    }

    private void clickViewGeneratedImages(View v) {
        // Navigate to ViewImagesActivity with user-specific images
        Intent intent = new Intent(this, ViewImagesActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GOOGLE_TOKEN", authToken);
        intent.putExtra("FLAG", true); // Indicate that user-specific images should be displayed
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Add transition animation
        startActivity(intent);
    }

    private void clickViewImagesFromCommunity(View v) {
        // Navigate to ViewImagesActivity with community images
        Intent intent = new Intent(this, ViewImagesActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GOOGLE_TOKEN", authToken);
        intent.putExtra("FLAG", false); // Indicate that community images should be displayed
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Add transition animation
        startActivity(intent);
    }

    private void loadName() {
        try {
            // Create a JSON payload with user ID
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("user_id", userId);
            Log.d(TAG, "Payload being sent: " + jsonPayload);

            // Send a request to fetch the user's name
            networkSender.sendHttpRequest("/loadName", jsonPayload.toString(), null, new NetworkSender.ResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        // Check if there's an error field in the response
                        if (jsonResponse.has("error")) {
                            String error = jsonResponse.getString("error");
                            Log.e(TAG, "Backend error: " + error);
                            showToast("Error: " + error);
                            return;
                        }

                        // Extract and display the user's name
                        String name = jsonResponse.getString("name");
                        runOnUiThread(() -> nameView.setText("Welcome: " + name));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        showToast("Error: Could not parse backend response");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle network errors
                    Log.e(TAG, "Network error: " + errorMessage);
                    showToast("Connection error: " + errorMessage);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request payload", e);
            showToast("Error: Could not create request");
        }
    }

    private void showToast(final String message) {
        // Display a toast message on the UI thread
        runOnUiThread(() ->
                Toast.makeText(Welcome.this, message, Toast.LENGTH_LONG).show()
        );
    }
}
