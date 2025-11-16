package com.example.bvbankingapp.Utils;

import android.util.Log;

import com.example.bvbankingapp.Utils.UtilsAuth;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final int TIMEOUT = 10000; // 10 seconds
    private static final String BASE_URL = "http://10.0.2.2:8080";

    // ============================
    // POST (có hoặc không có token)
    // ============================
    public static JSONObject post(String endpoint, JSONObject body, String token) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }

            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes("utf-8"));
            os.close();

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ?
                    conn.getInputStream() : conn.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) sb.append(line);
            br.close();
            conn.disconnect();

            JSONObject response = new JSONObject();
            response.put("code", code);
            response.put("body", sb.toString());
            return response;

        } catch (Exception e) {
            Log.e("ApiClient", "POST Error", e);
            return null;
        }
    }

    // ===========
    // GET có token
    // ===========
    public static JSONObject get(String endpoint, String token) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            if (token != null && !token.isEmpty())
                conn.setRequestProperty("Authorization", "Bearer " + token);

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ?
                    conn.getInputStream() : conn.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) sb.append(line);
            br.close();
            conn.disconnect();

            JSONObject response = new JSONObject();
            response.put("code", code);
            response.put("body", sb.toString());
            return response;

        } catch (Exception e) {
            Log.e("ApiClient", "GET Error", e);
            return null;
        }
    }
}
