package com.example.artisticintelligence;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Homepage extends AppCompatActivity {
    private static final String TAG = "Homepage";
    private Button sendButton;
    private NetworkSender networkSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        initializeViews();
        initializeNetworkSender();
    }



    private void initializeViews() {
        sendButton = findViewById(R.id.button);
        sendButton.setOnClickListener(this::onClick);
    }

    private void initializeNetworkSender() {
        networkSender = new NetworkSender();
    }

    public void onClick(View v) {
        sendButton.setEnabled(false);
        sendMessage();
    }

    private void sendMessage() {
        String message = "Hello, server!";
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