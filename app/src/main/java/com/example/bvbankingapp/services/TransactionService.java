package com.example.bvbankingapp.services;

import android.content.Context;

import com.example.bvbankingapp.TransactionItem;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.example.bvbankingapp.activities.WithdrawActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private static final String API_URL = "http://10.0.2.2:8080/api/transactions";



    // =========================================================
    // CALLBACK INTERFACE
    // =========================================================
    public interface TransactionCallback {
        void onSuccess(List<TransactionItem> list);
        void onUnauthorized();     // token hết hạn
        void onError(String message);
    }

    // =========================================================
    // MAIN FUNCTION – GỌI API LỊCH SỬ GIAO DỊCH
    // =========================================================
    public static void fetchTransactions(Context context, TransactionCallback callback) {

        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(context);

                if (token == null || token.isEmpty()) {
                    callback.onUnauthorized();
                    return;
                }

                URL url = new URL("http://10.0.2.2:8080/api/transactions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                // TOKEN HẾT HẠN
                if (code == 401 || code == 403) {
                    callback.onUnauthorized();
                    return;
                }

                // THÀNH CÔNG
                if (code == 200) {

                    JSONObject root = new JSONObject(sb.toString());
                    JSONArray arr = root.optJSONArray("transactions");

                    if (arr == null) arr = new JSONArray();

                    List<TransactionItem> list = new ArrayList<>();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        TransactionItem item = new TransactionItem(
                                obj.optString("type"),
                                obj.optDouble("amount"),
                                obj.optString("description"),
                                obj.optString("timestamp")
                        );
                        list.add(item);
                    }

                    callback.onSuccess(list);
                    return;
                }

                callback.onError("Lỗi tải dữ liệu. Mã lỗi: " + code);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Không thể kết nối server!");
            }
        }).start();
    }

    public interface TransactionActionCallback {
        void onSuccess(String message);
        void onUnauthorized();
        void onError(String msg);
    }

    public static void deposit(Context context, double amount, String description,
                               TransactionActionCallback callback) {

        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(context);
                if (token == null || token.isEmpty()) {
                    callback.onUnauthorized();
                    return;
                }

                // ✅ Dùng endpoint mới của AccountController
                URL url = new URL("http://10.0.2.2:8080/api/account/deposit");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // JSON body – KHÔNG gửi accountNumber, backend tự lấy từ JWT
                JSONObject body = new JSONObject();
                body.put("amount", amount);
                body.put("description", description);

                conn.getOutputStream().write(body.toString().getBytes("utf-8"));

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code == 200 || code == 201) {
                    callback.onSuccess("Nạp tiền thành công!");
                    return;
                }

                if (code == 401 || code == 403) {
                    callback.onUnauthorized();
                    return;
                }

                callback.onError("Lỗi khi nạp tiền (code: " + code + ")");

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Không thể kết nối server!");
            }
        }).start();
    }


    public static void withdraw(Context context, double amount, String description,
                                TransactionActionCallback callback) {

        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(context);
                if (token == null || token.isEmpty()) {
                    callback.onUnauthorized();
                    return;
                }

                URL url = new URL("http://10.0.2.2:8080/api/account/withdraw");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("amount", amount);
                body.put("description", description);

                conn.getOutputStream().write(body.toString().getBytes("utf-8"));

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code == 200 || code == 201) {
                    callback.onSuccess("Rút tiền thành công!");
                    return;
                }

                if (code == 401 || code == 403) {
                    callback.onUnauthorized();
                    return;
                }

                callback.onError("Lỗi khi rút tiền (code: " + code + ")");

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Không thể kết nối server!");
            }
        }).start();
    }

    public static void transfer(Context context, String targetAccount, double amount,
                                String description, TransactionActionCallback callback) {

        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(context);
                if (token == null || token.isEmpty()) {
                    callback.onUnauthorized();
                    return;
                }

                URL url = new URL("http://10.0.2.2:8080/api/account/transfer");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("targetAccount", targetAccount);
                body.put("amount", amount);
                body.put("description", description);

                conn.getOutputStream().write(body.toString().getBytes("utf-8"));

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code == 200 || code == 201) {
                    callback.onSuccess("Chuyển tiền thành công!");
                    return;
                }

                if (code == 401 || code == 403) {
                    callback.onUnauthorized();
                    return;
                }

                callback.onError("Không thể chuyển tiền (code: " + code + ")");

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Không thể kết nối server!");
            }
        }).start();
    }

    public static void lookupAccount(Context context, String acc, LookupCallback callback) {
        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(context);
                if (token == null) {
                    callback.onUnauthorized();
                    return;
                }

                URL url = new URL("http://10.0.2.2:8080/api/account/lookup/" + acc);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code == 200) callback.onSuccess(sb.toString());
                else if (code == 404) callback.onNotFound();
                else if (code == 401 || code == 403) callback.onUnauthorized();
                else callback.onError("Lỗi tra cứu: " + code);

            } catch (Exception e) {
                callback.onError("Lỗi kết nối server");
            }
        }).start();
    }

    public interface LookupCallback {
        void onSuccess(String json);
        void onNotFound();
        void onUnauthorized();
        void onError(String msg);
    }


}
