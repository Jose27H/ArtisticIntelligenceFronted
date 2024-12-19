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
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static final String CHARSET = "UTF-8";

    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    public NetworkSender() {
            disableSSLVerification(); // Only for development/debug builds
    }

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

    public void sendHttpRequest(String endpoint, String jsonPayload, String token, ResponseCallback callback) {
        new Thread(() -> {
            try {
                String response = makeHttpRequest(endpoint, jsonPayload, token);
                callback.onSuccess(response);
            } catch (Exception e) {
                callback.onError("Exception: " + e.getMessage());
            }
        }).start();
    }

    private String makeHttpRequest(String route, String jsonPayload, String token) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + route);
            conn = (HttpURLConnection) url.openConnection();
            setupConnection(conn, "POST");

            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            sendRequest(conn, jsonPayload);

            int responseCode = conn.getResponseCode();
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

    private void setupConnection(HttpURLConnection conn, String method) throws Exception {
        conn.setRequestMethod(method);
        conn.setDoOutput(!method.equalsIgnoreCase("GET"));
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "ArtisticIntelligence-Android");
    }

    private void sendRequest(HttpURLConnection conn, String jsonPayload) throws Exception {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(CHARSET);
            os.write(input, 0, input.length);
        }
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), CHARSET))) {
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
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
