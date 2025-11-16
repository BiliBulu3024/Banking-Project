package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.example.bvbankingapp.services.TransactionService;
import com.google.android.material.appbar.MaterialToolbar;

public class WithdrawActivity extends BaseActivity  {

    private EditText etAmount, etDesc;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setupMenu(topAppBar);

        etAmount = findViewById(R.id.etAmount);
        etDesc = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> submitWithdraw());
    }

    private void submitWithdraw() {

        String amountStr = etAmount.getText().toString().trim();
        String description = etDesc.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔵 Gọi API rút tiền
        TransactionService.withdraw(this, amount, description,
                new TransactionService.TransactionActionCallback() {

                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(WithdrawActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onUnauthorized() {
                        runOnUiThread(() -> {
                            UtilsAuth.clearAuthData(WithdrawActivity.this);
                            Toast.makeText(WithdrawActivity.this, "Phiên đăng nhập hết hạn!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(WithdrawActivity.this, LoginActivity.class));
                            finish();
                        });
                    }

                    @Override
                    public void onError(String msg) {
                        runOnUiThread(() ->
                                Toast.makeText(WithdrawActivity.this, msg, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }
}
