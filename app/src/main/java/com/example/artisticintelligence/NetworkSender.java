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
    private static final String TAG = "NetworkSender";
    private static final String BASE_URL = "https://artisticintelligencebackend-production.up.railway.app";
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 15000;

    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    public NetworkSender() {
        // Initialize with SSL verification disabled
        disableSSLVerification();
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
            Log.e(TAG, "Error disabling SSL verification", e);
        }
    }

    public void sendHttpRequest(String route, String message, String token, ResponseCallback callback) {
        new Thread(() -> {
            try {
                String response = makeHttpRequest(route, message, token);
                Log.d(TAG, "Request successful: " + response);
                callback.onSuccess(response);
            } catch (Exception e) {
                Log.e(TAG, "Request failed: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private String makeHttpRequest(String route, String message, String token) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + route);
            conn = (HttpURLConnection) url.openConnection();
            setupConnection(conn);

            String jsonInputString = createJsonMessage(message, token);
            sendRequest(conn, jsonInputString);

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);
            Log.d(TAG, "Response Message: " + conn.getResponseMessage());

            if (responseCode >= 200 && responseCode < 300) {
                return readResponse(conn);
            } else {
                String errorResponse = readErrorResponse(conn);
                throw new Exception("HTTP Error " + responseCode + ": " + errorResponse);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String createJsonMessage(String message, String token) {
        return String.format("{\"message\": \"%s\", \"token\": \"%s\"}",
                message,
                token != null ? token : ""
        );
    }

    private void setupConnection(HttpURLConnection conn) throws Exception {
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "ArtisticIntelligence-Android");
    }

    private void sendRequest(HttpURLConnection conn, String jsonInputString) throws Exception {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }
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
}