package com.example.bvbankingapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class UtilsAuth {

    private static final String PREF_FILE = "secure_auth_prefs";

    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "auth_username";
    private static final String KEY_ROLE = "auth_role";
    private static final String KEY_EMAIL = "auth_email";
    private static final String KEY_ACCOUNT_NUMBER = "auth_accountNumber";
    private static final String KEY_ACCOUNT = "auth_account_number";

    private static SharedPreferences getSecurePrefs(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREF_FILE,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e("UtilsAuth", "Lỗi EncryptedSharedPreferences", e);
            return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        }
    }

    // ============================================================
    // 🔹 SAVE AUTH DATA
    // ============================================================
    public static void saveAuthData(Context context,
                                    String token,
                                    String username,
                                    String role,
                                    String email,
                                    String accountNumber) {

        SharedPreferences prefs = getSecurePrefs(context);
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ACCOUNT_NUMBER, accountNumber)
                .apply();

        Log.d("UtilsAuth", "✔️ Đã lưu JWT + user info an toàn (đầy đủ)");
    }


    // ============================================================
    // 🔹 GETTERS
    // ============================================================
    public static String getToken(Context context) {
        return getSecurePrefs(context).getString(KEY_TOKEN, null);
    }

    public static String getUsername(Context context) {
        return getSecurePrefs(context).getString(KEY_USERNAME, null);
    }

    public static String getRole(Context context) {
        return getSecurePrefs(context).getString(KEY_ROLE, null);
    }

    public static String getEmail(Context context) {
        return getSecurePrefs(context).getString(KEY_EMAIL, null);
    }

    public static String getAccountNumber(Context context) {
        return getSecurePrefs(context).getString(KEY_ACCOUNT_NUMBER, null);
    }


    // ============================================================
    // 🔹 CLEAR AUTH
    // ============================================================
    public static void clearAuthData(Context context) {
        getSecurePrefs(context).edit().clear().apply();
        Log.d("UtilsAuth", "🧹 Đã xóa dữ liệu đăng nhập");
    }

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }
}
