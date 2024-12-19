package com.example.artisticintelligence;

import android.util.Log;

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

public class NetworkSender {

    // Constants
    private static final String TAG = "NetworkSender"; // Log tag for debugging
    private static final String BASE_URL = "https://artisticintelligencebackend-production.up.railway.app"; // Base URL of the backend
    private static final int CONNECTION_TIMEOUT = 45000; // Timeout for establishing connection (in ms)
    private static final int READ_TIMEOUT = 45000; // Timeout for reading data (in ms)
    private static final String CHARSET = "UTF-8"; // Character encoding for payloads

    // Interface for callback responses
    public interface ResponseCallback {
        void onSuccess(String response); // Called when the request is successful
        void onError(String errorMessage); // Called when an error occurs
    }

    public NetworkSender() {
        disableSSLVerification(); // Disable SSL verification for development/debug builds
    }

    private void disableSSLVerification() {
        try {
            // Trust all certificates for HTTPS connections (insecure, for debug only)
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
            Log.e(TAG, "Error disabling SSL verification", e); // Log errors during SSL configuration
        }
    }

    public void sendHttpRequest(String endpoint, String jsonPayload, String token, ResponseCallback callback) {
        new Thread(() -> {
            try {
                // Make the HTTP request and pass the response to the callback
                String response = makeHttpRequest(endpoint, jsonPayload, token);
                callback.onSuccess(response);
            } catch (Exception e) {
                callback.onError("Exception: " + e.getMessage()); // Pass errors to the callback
            }
        }).start();
    }

    private String makeHttpRequest(String route, String jsonPayload, String token) throws Exception {
        HttpURLConnection conn = null;
        try {
            // Initialize the connection
            URL url = new URL(BASE_URL + route);
            conn = (HttpURLConnection) url.openConnection();
            setupConnection(conn, "POST"); // Set up the connection for a POST request

            // Add authorization header if a token is provided
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            sendRequest(conn, jsonPayload); // Send the JSON payload

            // Handle the response
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return readResponse(conn); // Read and return successful response
            } else {
                String errorResponse = readErrorResponse(conn);
                throw new Exception("HTTP Error " + responseCode + ": " + errorResponse); // Throw exception for errors
            }
        } finally {
            if (conn != null) {
                conn.disconnect(); // Close the connection
            }
        }
    }

    private void setupConnection(HttpURLConnection conn, String method) throws Exception {
        conn.setRequestMethod(method); // Set the HTTP method
        conn.setDoOutput(!method.equalsIgnoreCase("GET")); // Allow output for non-GET methods
        conn.setDoInput(true); // Allow input for all methods
        conn.setUseCaches(false); // Disable caching
        conn.setConnectTimeout(CONNECTION_TIMEOUT); // Set connection timeout
        conn.setReadTimeout(READ_TIMEOUT); // Set read timeout

        // Set common headers
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "ArtisticIntelligence-Android");
    }

    private void sendRequest(HttpURLConnection conn, String jsonPayload) throws Exception {
        try (OutputStream os = conn.getOutputStream()) {
            // Write the JSON payload to the output stream
            byte[] input = jsonPayload.getBytes(CHARSET);
            os.write(input, 0, input.length);
        }
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), CHARSET))) {
            // Read and return the response from the input stream
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
                new InputStreamReader(conn.getErrorStream(), CHARSET))) {
            // Read and return the error response from the error stream
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
