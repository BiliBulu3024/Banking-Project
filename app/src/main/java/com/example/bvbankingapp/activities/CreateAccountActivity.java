package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.ApiClient;

import org.json.JSONObject;

public class CreateAccountActivity extends AppCompatActivity {
        private EditText etUsername, etEmail, etPassword, etConfirm;
        private Button btnCreate;
        private ProgressBar progress;

        private static final String REGISTER_ENDPOINT = "/api/users/register";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_account);

            etUsername = findViewById(R.id.new_username);
            etEmail = findViewById(R.id.new_email);
            etPassword = findViewById(R.id.new_password);
            etConfirm = findViewById(R.id.new_confirmation);
            btnCreate = findViewById(R.id.button_create);
            progress = findViewById(R.id.progressBar);

            btnCreate.setOnClickListener(v -> handleRegister());
        }

        private void handleRegister() {
            String user = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();

            // validation
            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showMsg("Vui lòng nhập đầy đủ");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showMsg("Email không hợp lệ");
                return;
            }
            if (!pass.equals(confirm)) {
                showMsg("Mật khẩu không trùng khớp");
                return;
            }

            progress.setVisibility(View.VISIBLE);
            btnCreate.setEnabled(false);

            new Thread(() -> {
                try {
                    JSONObject body = new JSONObject();
                    body.put("username", user);
                    body.put("email", email);
                    body.put("password", pass);

                    JSONObject resp = ApiClient.post(REGISTER_ENDPOINT, body, null);

                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        btnCreate.setEnabled(true);

                        if (resp == null) {
                            showMsg("Không kết nối được server");
                            return;
                        }

                        int code = resp.optInt("code");
                        String json = resp.optString("body");

                        if (code == 200 || code == 201) {
                            showMsg("Tạo tài khoản thành công!");
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            showMsg("Lỗi: " + json);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private void showMsg(String msg) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
