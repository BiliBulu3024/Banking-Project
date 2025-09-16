package com.example.bvbankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText etUsername = findViewById(R.id.edittext_username);
        EditText etPassword = findViewById(R.id.edittext_password);
        Button btnLogin = findViewById(R.id.button_login);
        Button btnCreate = findViewById(R.id.button_createnewaccount);

        // Xử lý Login
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.equals("admin") && password.equals("123")) {
                Intent intent = new Intent(LoginActivity.this, AdminDashboard.class);
                startActivity(intent);
            } else if (username.equals("user") && password.equals("123")) {
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Sai username hoặc password", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý Create New Account (chưa có backend → mock)
        btnCreate.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng tạo tài khoản sẽ có sau", Toast.LENGTH_SHORT).show();
        });
    }
}