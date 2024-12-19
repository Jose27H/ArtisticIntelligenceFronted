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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class Welcome extends AppCompatActivity {
    String userId;
    TextView nameView;

    String authToken;

    Button generateImageButton;
    Button viewGeneratedImageButton;

    Button viewImagesFromCommunityButton;

    private static final String TAG = "Welcome";
    private NetworkSender networkSender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        setListeners();

        userId = getIntent().getStringExtra("USER_ID");
        authToken = getIntent().getStringExtra("GOOGLE_TOKEN");
        networkSender = new NetworkSender();
      loadName();

    }

    private void setListeners(){
        nameView = findViewById(R.id.header_text);
        generateImageButton = findViewById(R.id.generate_image_button);
        viewGeneratedImageButton = findViewById(R.id.view_generated_images_button);
        viewImagesFromCommunityButton = findViewById(R.id.view_community_images_button);

        generateImageButton.setOnClickListener(this::clickGenerateImage);
        viewGeneratedImageButton.setOnClickListener(this::clickViewGeneratedImages);
        viewImagesFromCommunityButton.setOnClickListener(this::clickViewImagesFromCommunity);

    }

    private void clickGenerateImage(View v){
        Intent intent = new Intent(this, PromptActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GOOGLE_TOKEN", authToken); // Pass the Google ID as an extra
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
    }

    private void clickViewGeneratedImages(View v) {
        Intent intent = new Intent(this, ViewImagesActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GOOGLE_TOKEN", authToken);
        intent.putExtra("FLAG", true);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);;
        startActivity(intent);
    }

    private void clickViewImagesFromCommunity(View v) {
        Intent intent = new Intent(this, ViewImagesActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GOOGLE_TOKEN", authToken);
        intent.putExtra("FLAG", false);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
    }

    private void loadName() {
        try {
            JSONObject jsonPayload = new JSONObject();

            jsonPayload.put("user_id", userId);
            Log.d(TAG, "Payload being sent: " + jsonPayload);

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

                        // If no error, get the name
                        String name = jsonResponse.getString("name");
                        runOnUiThread(() -> nameView.setText("Welcome: " + name));



                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        showToast("Error: Could not parse backend response");
                    }
                }

                @Override
                public void onError(String errorMessage) {
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
        runOnUiThread(() ->
                Toast.makeText(Welcome.this, message, Toast.LENGTH_LONG).show()
        );
    }


}