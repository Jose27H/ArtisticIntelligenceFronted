package com.example.artisticintelligence;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar loadingProgress;
    private NetworkSender networkSender;
    private String userId;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);

        // Initialize views
        recyclerView = findViewById(R.id.images_recycler_view);
        loadingProgress = findViewById(R.id.loading_progress);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        // Get data from intent
        userId = getIntent().getStringExtra("USER_ID");
        authToken = getIntent().getStringExtra("GOOGLE_TOKEN");

        // Initialize NetworkSender
        networkSender = new NetworkSender();

        // Load images
        loadUserImages();
    }

    private void loadUserImages() {
        loadingProgress.setVisibility(View.VISIBLE);

        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("user_id", userId);

            networkSender.sendHttpRequest("/getUserImages", jsonPayload.toString(), authToken,
                    new NetworkSender.ResponseCallback() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);

                                if (jsonResponse.has("error")) {
                                    showError(jsonResponse.getString("error"));
                                    return;
                                }

                                JSONArray imagesArray = jsonResponse.getJSONArray("images");
                                // TODO: Create and set adapter with images data
                                runOnUiThread(() -> {
                                    loadingProgress.setVisibility(View.GONE);
                                    // Set adapter here
                                });

                            } catch (Exception e) {
                                showError("Error parsing response: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            showError("Network error: " + errorMessage);
                        }
                    });

        } catch (Exception e) {
            showError("Error creating request: " + e.getMessage());
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            loadingProgress.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }
}