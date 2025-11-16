package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.example.bvbankingapp.services.TransactionService;
import com.google.android.material.appbar.MaterialToolbar;

public class DepositActivity extends BaseActivity {

    private EditText etAmount, etDesc;
    private TextView tvAccount;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setupMenu(topAppBar);

        tvAccount = findViewById(R.id.tvDepositAccount);
        etAmount = findViewById(R.id.etAmount);
        etDesc = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        TextView tvAccount = findViewById(R.id.tvDepositAccount);
        String acc = UtilsAuth.getAccountNumber(this);

        // =============================
        // ⭐ HIỂN THỊ TÀI KHOẢN KHÔNG CÓ "ACC"
        // =============================
        String fullAcc = UtilsAuth.getAccountNumber(this); // ACC12345678
        String displayAcc = fullAcc;

        if (fullAcc != null && fullAcc.startsWith("ACC")) {
            displayAcc = fullAcc.substring(3); // bỏ ACC
        }

        if (displayAcc != null)
            tvAccount.setText("Account: " + displayAcc);
        else
            tvAccount.setText("Account: ---");

        btnSubmit.setOnClickListener(v -> submitDeposit());
    }


    private void submitDeposit() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDesc.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        TransactionService.deposit(this, amount, description,
                new TransactionService.TransactionActionCallback() {

                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(DepositActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onUnauthorized() {
                        runOnUiThread(() -> {
                            UtilsAuth.clearAuthData(DepositActivity.this);
                            Toast.makeText(DepositActivity.this,
                                    "Phiên đăng nhập hết hạn!",
                                    Toast.LENGTH_LONG).show();

                            startActivity(new Intent(DepositActivity.this, LoginActivity.class));
                            finish();
                        });
                    }

                    @Override
                    public void onError(String msg) {
                        runOnUiThread(() ->
                                Toast.makeText(DepositActivity.this, msg, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }
}
