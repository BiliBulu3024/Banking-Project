package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private MaterialToolbar topAppBar;
    private TextView tvWelcome, tvAccountNumber, tvBalance;
    private Button btnViewReport, btnDeposit, btnTransfer;
    private LineChart lineChart;
    private PieChart pieChart;

    private static final String ACCOUNT_INFO_URL = "http://10.0.2.2:8080/api/account/info";
    private static final String SUMMARY_URL = "http://10.0.2.2:8080/api/transactions/summary/chart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 🔹 Init Views
        topAppBar = findViewById(R.id.topAppBar);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvBalance = findViewById(R.id.tvBalance);
        btnViewReport = findViewById(R.id.btnViewReport);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        btnDeposit = findViewById(R.id.btnDeposit);
        btnTransfer = findViewById(R.id.btnTransfer);
        // 🔹 Setup Menu
        setupMenu(topAppBar);

        // 🔹 Load Account Info
        loadAccountInfo();

        // 🔹 If openChart flag → auto open chart
        boolean openChart = getIntent().getBooleanExtra("openChart", false);
        if (openChart) {
            fetchReport();
        }

        // 🔹 Button click
        btnViewReport.setOnClickListener(v -> fetchReport());


        btnDeposit.setOnClickListener(v -> {
            startActivity(new Intent(this, DepositActivity.class));
        });

        btnTransfer.setOnClickListener(v -> {
            startActivity(new Intent(this, TransferActivity.class));
        });

    }
    @Override
    protected void onResume() {
        super.onResume();

        loadAccountInfo();

        if (lineChart.getVisibility() == View.VISIBLE ||
                pieChart.getVisibility() == View.VISIBLE) {
            fetchReport();
        }
    }
    // ============================================================
    // 1️⃣ LOAD ACCOUNT INFO
    // ============================================================
    private void loadAccountInfo() {
        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(this);
                if (token == null || token.trim().isEmpty()) {
                    redirectToLogin("Token không hợp lệ!");
                    return;
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(ACCOUNT_INFO_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                int code = conn.getResponseCode();
                String response = readResponse(conn);

                Log.d("Dashboard", "ACCOUNT_INFO code=" + code + " body=" + response);

                if (code == 200) {
                    JSONObject json = new JSONObject(response);
                    updateAccountUI(json);

                } else if (code == 401 || code == 403) {
                    redirectToLogin("Phiên đăng nhập đã hết hạn!");

                } else {
                    showToast("Không thể tải thông tin (code " + code + ")");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi kết nối backend!");
            }
        }).start();
    }

    private void updateAccountUI(JSONObject json) {

        String username = json.optString("username", null);
        String accountNumber = json.optString("accountNumber", null);
        double balance = json.optDouble("balance", 0);

        // ⭐ Loại bỏ prefix
        String shortAcc = accountNumber;
        if (accountNumber != null && accountNumber.startsWith("ACC")) {
            shortAcc = accountNumber.substring(3);
        }

        // ⭐ Cập nhật SharedPreferences (full ACC)
        String token = UtilsAuth.getToken(this);
        String role = UtilsAuth.getRole(this);
        String email = UtilsAuth.getEmail(this);

        UtilsAuth.saveAuthData(
                this,
                token,
                username,
                role,
                email,
                accountNumber  // lưu full
        );

        // ⭐ Dùng biến final trong lambda
        final String fUser = username;
        final String fAcc = shortAcc;
        final double fBal = balance;

        runOnUiThread(() -> {
            tvWelcome.setText("Xin chào, " + fUser + "!");
            tvAccountNumber.setText("Account: " + fAcc);
            tvBalance.setText("Balance: ₫ " + fBal);
        });
    }

    // ============================================================
    // 2️⃣ FETCH REPORT (CHART DATA)
    // ============================================================
    private void fetchReport() {
        btnViewReport.setEnabled(false);

        new Thread(() -> {
            try {
                String token = UtilsAuth.getToken(this);
                if (token == null || token.trim().isEmpty()) {
                    redirectToLogin("Token không hợp lệ!");
                    return;
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(SUMMARY_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                int code = conn.getResponseCode();
                String response = readResponse(conn);

                Log.d("Dashboard", "SUMMARY code=" + code + " body=" + response);

                if (code == 200) {
                    JSONObject json = new JSONObject(response);

                    // Defensive JSON
                    if (!json.has("chartData")) {
                        showToast("Dữ liệu biểu đồ không có!");
                        return;
                    }

                    JSONObject chartData = json.getJSONObject("chartData");

                    runOnUiThread(() -> {
                        showLineChart(chartData.optJSONObject("lineChart"));
                        showPieChart(chartData.optJSONObject("pieChart"));
                        btnViewReport.setEnabled(true);
                    });

                } else if (code == 401 || code == 403) {
                    redirectToLogin("Phiên đăng nhập hết hạn!");

                } else {
                    showToast("Không thể tải báo cáo (code " + code + ")");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi kết nối server!");
            }
        }).start();
    }

    // ============================================================
    // 3️⃣ RENDER LINE CHART
    // ============================================================
    private void showLineChart(JSONObject data) {
        if (data == null) {
            showToast("Không có dữ liệu biểu đồ đường");
            return;
        }

        try {
            List<Entry> entries = new ArrayList<>();
            int index = 0;

            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                entries.add(new Entry(index++, (float) data.optDouble(key)));
            }

            LineDataSet set = new LineDataSet(entries, "Biến động số dư");
            set.setColors(ColorTemplate.MATERIAL_COLORS);
            set.setLineWidth(2f);
            set.setCircleRadius(4f);

            lineChart.setData(new LineData(set));
            lineChart.setVisibility(View.VISIBLE);
            lineChart.animateX(800);

        } catch (Exception e) {
            showToast("Không thể hiển thị biểu đồ đường");
        }
    }

    // ============================================================
    // 4️⃣ RENDER PIE CHART
    // ============================================================
    private void showPieChart(JSONObject data) {
        if (data == null) {
            showToast("Không có dữ liệu biểu đồ tròn");
            return;
        }

        try {
            List<PieEntry> entries = new ArrayList<>();

            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String label = keys.next();
                entries.add(new PieEntry((float) data.optDouble(label), label));
            }

            PieDataSet set = new PieDataSet(entries, "Tỷ lệ giao dịch");
            set.setColors(ColorTemplate.COLORFUL_COLORS);
            set.setValueTextSize(12f);

            pieChart.setData(new PieData(set));
            pieChart.setVisibility(View.VISIBLE);
            pieChart.animateY(800);

        } catch (Exception e) {
            showToast("Không thể hiển thị biểu đồ tròn");
        }
    }

    // ============================================================
    // 5️⃣ UTILITIES
    // ============================================================
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

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void redirectToLogin(String msg) {
        runOnUiThread(() -> {
            UtilsAuth.clearAuthData(this);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}
