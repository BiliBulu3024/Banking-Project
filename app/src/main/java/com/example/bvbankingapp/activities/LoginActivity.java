package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import com.example.bvbankingapp.AdminDashboard;
import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.ApiClient;
import com.example.bvbankingapp.Utils.UtilsAuth;

public class LoginActivity extends AppCompatActivity {

        private EditText etUser, etPass;
        private Button btnLogin, btnCreate;
        private ProgressBar progress;

    private static final String LOGIN_ENDPOINT = "/api/auth/login";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

//            // AUTO LOGIN
//            String token = UtilsAuth.getToken(this);
//            if (token != null && !token.isEmpty()) {
//                autoNavigate();
//                return;
//            }

            etUser = findViewById(R.id.edittext_username);
            etPass = findViewById(R.id.edittext_password);
            btnLogin = findViewById(R.id.button_login);
            progress = findViewById(R.id.progressBar);
            btnCreate = findViewById(R.id.button_create_new_account);

            btnLogin.setOnClickListener(v -> handleLogin());



            btnCreate.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            });


        }

    private void handleLogin() {
        String username = etUser.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMsg("Nhập đủ thông tin");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password);

                JSONObject resp = ApiClient.post(LOGIN_ENDPOINT, body, null);

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (resp == null) {
                        showMsg("Không kết nối được server");
                        return;
                    }

                    int code = resp.optInt("code");
                    String json = resp.optString("body");

                    if (code == 200) {
                        try {
                            JSONObject obj = new JSONObject(json);

                            String token = obj.optString("token");
                            String role = obj.optString("role");
                            String usernameResp = obj.optString("username");
                            String email = obj.optString("email");
                            String accountNumber = obj.optString("accountNumber");

                            // ===============================
                            // 🔥 SAVE FULL USER INFO
                            // ===============================
                            UtilsAuth.saveAuthData(
                                    this,
                                    token,
                                    usernameResp,
                                    role,
                                    email,
                                    accountNumber
                            );

                            showMsg("Đăng nhập thành công!");
                            autoNavigate();

                        } catch (Exception e) {
                            e.printStackTrace();
                            showMsg("Lỗi đọc dữ liệu đăng nhập");
                        }
                    } else {
                        showMsg("Sai tài khoản hoặc mật khẩu");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void autoNavigate() {
            String role = UtilsAuth.getRole(this);
            if ("ADMIN".equalsIgnoreCase(role)) {
                startActivity(new Intent(this, AdminDashboard.class));
            } else {
                startActivity(new Intent(this, DashboardActivity.class));
            }
            finish();
        }

        private void showMsg(String msg) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
