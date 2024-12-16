package com.example.artisticintelligence;

import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "Welcome";
    private NetworkSender networkSender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
       userId = getIntent().getStringExtra("GOOGLE_ID");
        networkSender = new NetworkSender();
      loadName();

    }

    private void loadName() {
        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("user_id", userId);

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
                        showToast("Backend responded load " + name);

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