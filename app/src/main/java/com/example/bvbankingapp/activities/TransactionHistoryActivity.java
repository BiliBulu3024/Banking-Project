package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.TransactionAdapter;
import com.example.bvbankingapp.TransactionItem;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.example.bvbankingapp.services.TransactionService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class TransactionHistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;

    private static final String TAG = "TransactionHistory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setupMenu(topAppBar);

        recyclerView = findViewById(R.id.recyclerTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadTransactionHistory();
    }

    // =====================================================
    // LOAD TRANSACTION HISTORY
    // =====================================================
    private void loadTransactionHistory() {

        // 1️⃣ Kiểm tra token trước
        String token = UtilsAuth.getToken(this);
        if (token == null || token.trim().isEmpty()) {
            redirectToLogin("Phiên đăng nhập không hợp lệ!");
            return;
        }

        // 2️⃣ Call API từ service
        TransactionService.fetchTransactions(this,
                new TransactionService.TransactionCallback() {

                    @Override
                    public void onSuccess(List<TransactionItem> list) {
                        Log.d(TAG, "API SUCCESS - total items = " + list.size());

                        runOnUiThread(() -> {
                            if (list == null || list.isEmpty()) {
                                Toast.makeText(TransactionHistoryActivity.this,
                                        "Không có giao dịch nào!", Toast.LENGTH_LONG).show();
                            }

                            adapter = new TransactionAdapter(list);
                            recyclerView.setAdapter(adapter);
                        });
                    }

                    @Override
                    public void onUnauthorized() {
                        Log.d(TAG, "API 401/403 — Token expired");

                        runOnUiThread(() -> {
                            redirectToLogin("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "API ERROR: " + message);

                        runOnUiThread(() -> {
                            Toast.makeText(
                                    TransactionHistoryActivity.this,
                                    "Lỗi tải dữ liệu: " + message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                });
    }

    // =====================================================
    // REDIRECT TO LOGIN
    // =====================================================
    private void redirectToLogin(String msg) {
        UtilsAuth.clearAuthData(this);

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

        finish();
    }
}
