package com.example.artisticintelligence;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Homepage extends AppCompatActivity {
    private static final String TAG = "Homepage";
    private static final String API_URL = "https://artisticintelligencebackend-production.up.railway.app/message";
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 15000;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        setupWindowInsets();
        initializeViews();
        // For development only - disable SSL verification
        disableSSLVerification();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        sendButton = findViewById(R.id.button);
        sendButton.setOnClickListener(this::onClick);
    }

    // Warning: Only use this in development/testing
    private void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        sendButton.setEnabled(false);
        sendMessage();
    }

    private void sendMessage() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_URL);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("User-Agent", "ArtisticIntelligence-Android");

                Log.d(TAG, "Connecting to: " + API_URL);

                String jsonInputString = "{\"message\": \"Hello, server!\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                Log.d(TAG, "Response Code: " + conn.getResponseCode());
                Log.d(TAG, "Response Message: " + conn.getResponseMessage());

                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    String response = readResponse(conn);
                    showToast("Success: " + response);
                } else {
                    String errorResponse = readErrorResponse(conn);
                    showToast("Error " + responseCode + ": " + errorResponse);
                    Log.e(TAG, "Error Response: " + errorResponse);
                }

            } catch (Exception e) {
                String errorMessage = "Error: " + e.getMessage();
                showToast(errorMessage);
                Log.e(TAG, errorMessage, e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                runOnUiThread(() -> sendButton.setEnabled(true));
            }
        }).start();
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String readErrorResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private void showToast(final String message) {
        runOnUiThread(() ->
                Toast.makeText(Homepage.this, message, Toast.LENGTH_LONG).show()
        );
    }
}