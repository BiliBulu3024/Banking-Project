package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.UtilsAuth;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends BaseActivity {

    private TextView tvUsername, tvEmail;
    private EditText etEmail, etPassword;
    private Button btnEditSave;

    private boolean isEditMode = false;

    private static final String PROFILE_URL = "http://10.0.2.2:8080/api/account/me";
    private static final String UPDATE_URL   = "http://10.0.2.2:8080/api/account/update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvProfileUsername);
        tvEmail = findViewById(R.id.tvProfileEmail);

        etEmail = findViewById(R.id.etProfileEmail);
        etPassword = findViewById(R.id.etProfilePassword);

        btnEditSave = findViewById(R.id.btnEditSave);

        setupMenu(findViewById(R.id.topAppBar));

        fetchProfileData();

        // Sự kiện nút chuyển đổi chế độ
        btnEditSave.setOnClickListener(v -> {
            if (isEditMode) {
                updateProfile();
            } else {
                switchToEditMode();
            }
        });
    }

    // =======================================================
    // LOAD PROFILE
    // =======================================================
    private void fetchProfileData() {
        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(this);

                if (token == null) {
                    redirectToLogin("Vui lòng đăng nhập lại.");
                    return;
                }

                HttpURLConnection conn = setupConnection(PROFILE_URL, token, "GET");
                int code = conn.getResponseCode();
                String response = readResponse(conn);

                if (code == 200) {
                    JSONObject json = new JSONObject(response);

                    runOnUiThread(() -> {
                        tvUsername.setText(json.optString("username"));
                        tvEmail.setText(json.optString("email"));
                        etEmail.setText(json.optString("email"));
                    });

                } else if (code == 401 || code == 403) {
                    redirectToLogin("Phiên đăng nhập hết hạn!");

                } else {
                    showToast("Không thể tải hồ sơ (code " + code + ")");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi kết nối server");
            }
        }).start();
    }

    // =======================================================
    // CHUYỂN CHẾ ĐỘ VIEW → EDIT
    // =======================================================
    private void switchToEditMode() {
        isEditMode = true;

        tvEmail.setVisibility(TextView.GONE);
        etEmail.setVisibility(EditText.VISIBLE);
        etPassword.setVisibility(EditText.VISIBLE);

        btnEditSave.setText("Lưu thay đổi");
    }

    // =======================================================
    // CHUYỂN CHẾ ĐỘ EDIT → VIEW
    // =======================================================
    private void switchToViewMode() {
        isEditMode = false;

        tvEmail.setText(etEmail.getText().toString());
        tvEmail.setVisibility(TextView.VISIBLE);

        etEmail.setVisibility(EditText.GONE);
        etPassword.setVisibility(EditText.GONE);
        etPassword.setText("");

        btnEditSave.setText("Chỉnh sửa thông tin");
    }

    // =======================================================
    // UPDATE PROFILE
    // =======================================================
    private void updateProfile() {
        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(this);

                if (token == null) {
                    redirectToLogin("Vui lòng đăng nhập lại.");
                    return;
                }

                JSONObject body = new JSONObject();
                body.put("email", etEmail.getText().toString().trim());
                body.put("password", etPassword.getText().toString().trim());

                HttpURLConnection conn = setupConnection(UPDATE_URL, token, "PUT");
                conn.setDoOutput(true);
                conn.getOutputStream().write(body.toString().getBytes("utf-8"));

                int code = conn.getResponseCode();
                String response = readResponse(conn);

                if (code == 200) {
                    // Lưu email mới vào SharedPreferences
                    UtilsAuth.saveAuthData(
                            this,
                            UtilsAuth.getToken(this),
                            UtilsAuth.getUsername(this),
                            UtilsAuth.getRole(this),
                            etEmail.getText().toString().trim(),  // email mới
                            UtilsAuth.getAccountNumber(this)
                    );
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_LONG).show();
                        switchToViewMode();
                    });

                } else if (code == 401 || code == 403) {
                    redirectToLogin("Phiên đăng nhập hết hạn!");

                } else {
                    showToast("Không thể cập nhật (code " + code + ")");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi server trong quá trình cập nhật");
            }
        }).start();
    }

    // =======================================================
    // UTILITIES
    // =======================================================
    private HttpURLConnection setupConnection(String urlStr, String token, String method) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        InputStream is = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        return sb.toString();
    }

    private void redirectToLogin(String msg) {
        runOnUiThread(() -> {
            UtilsAuth.clearAuthData(this);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}
