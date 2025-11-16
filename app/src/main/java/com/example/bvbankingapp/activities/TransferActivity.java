package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.example.bvbankingapp.services.TransactionService;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

public class TransferActivity extends BaseActivity {

    private EditText etTargetAccount, etAmount, etDescription;
    private Button btnTransfer;
    private TextView tvReceiverName;

    private String receiverName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setupMenu(topAppBar);

        etTargetAccount = findViewById(R.id.etTargetAccount);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        btnTransfer = findViewById(R.id.btnSubmit);
        tvReceiverName = findViewById(R.id.tvReceiverName);

        // 🔍 Lookup khi nhập số tài khoản rút gọn
        etTargetAccount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String accShort = s.toString().trim();

                if (accShort.length() < 4) {
                    tvReceiverName.setText("");
                    receiverName = null;
                    return;
                }

                // ⭐ Chuyển số rút gọn → số đầy đủ ACCxxxx
                String fullAcc = accShort.startsWith("ACC") ? accShort : "ACC" + accShort;

                lookupAccount(fullAcc);
            }
        });

        btnTransfer.setOnClickListener(v -> submitTransfer());
    }

    // =======================================================
    // 🔍 TRA CỨU CHỦ TÀI KHOẢN
    // =======================================================
    private void lookupAccount(String fullAcc) {
        TransactionService.lookupAccount(this, fullAcc, new TransactionService.LookupCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    receiverName = obj.optString("owner");

                    runOnUiThread(() ->
                            tvReceiverName.setText("👤 Chủ tài khoản: " + receiverName)
                    );

                } catch (Exception e) {
                    runOnUiThread(() -> tvReceiverName.setText("Lỗi đọc dữ liệu"));
                }
            }

            @Override
            public void onNotFound() {
                receiverName = null;
                runOnUiThread(() ->
                        tvReceiverName.setText("❌ Không tìm thấy tài khoản")
                );
            }

            @Override
            public void onUnauthorized() {
                UtilsAuth.clearAuthData(TransferActivity.this);
                startActivity(new Intent(TransferActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() ->
                        Toast.makeText(TransferActivity.this, msg, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // =======================================================
    // 🔁 THỰC HIỆN CHUYỂN TIỀN
    // =======================================================
    private void submitTransfer() {

        String shortAcc = etTargetAccount.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (shortAcc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        if (receiverName == null) {
            Toast.makeText(this, "Không thể chuyển – Tài khoản nhận không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⭐ Convert số rút gọn → full ACCxxxx
        String fullAcc = shortAcc.startsWith("ACC") ? shortAcc : "ACC" + shortAcc;

        // ⭐ Check chuyển cho chính mình
        String myAccFull = UtilsAuth.getAccountNumber(this);
        if (fullAcc.equals(myAccFull)) {
            Toast.makeText(this, "Không thể tự chuyển cho chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        TransactionService.transfer(this, fullAcc, amount, desc,
                new TransactionService.TransactionActionCallback() {

                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(TransferActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onUnauthorized() {
                        UtilsAuth.clearAuthData(TransferActivity.this);
                        startActivity(new Intent(TransferActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String msg) {
                        runOnUiThread(() ->
                                Toast.makeText(TransferActivity.this, msg, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }
}
