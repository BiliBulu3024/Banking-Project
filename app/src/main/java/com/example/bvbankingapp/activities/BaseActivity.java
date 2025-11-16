package com.example.bvbankingapp.activities;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bvbankingapp.R;
import com.example.bvbankingapp.Utils.UtilsAuth;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupMenu(MaterialToolbar topAppBar) {

        if (topAppBar == null) return;

        topAppBar.setOnMenuItemClickListener(item -> {

            int id = item.getItemId();

            // ============================
            // 📌 Thông tin cá nhân
            // ============================
            if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            // ============================
            // 📌 Lịch sử giao dịch
            // ============================
            if (id == R.id.menu_transactions) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                return true;
            }

            // ============================
            // 📌 Dashboard (Trang chính)
            // ============================
            if (id == R.id.menu_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            }

            // ============================
            // 📌 Báo cáo giao dịch (biểu đồ)
            // ============================
            if (id == R.id.menu_reports) {
                Intent i = new Intent(this, DashboardActivity.class);
                i.putExtra("openChart", true); // Gửi flag cho Dashboard mở biểu đồ
                startActivity(i);
                return true;
            }

            // ============================
            // 📌 Đăng xuất
            // ============================
            if (id == R.id.menu_logout) {
                UtilsAuth.clearAuthData(this);

                Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

                finish();
                return true;
            }

            return false;
        });
    }
}
