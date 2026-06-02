package com.example.wertugo.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wertugo.R;
import com.example.wertugo.api.ApiClient;
import com.example.wertugo.api.ApiService;
import com.example.wertugo.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION_STORAGE = 2;

    private ImageView ivProfilePicture;
    private EditText etUsername;
    private AutoCompleteTextView etCountry;
    private TextView tvEmail;
    private MaterialButton btnSave, btnLogout;
    private TokenManager tokenManager;
    private ApiService apiService;
    private Uri selectedImageUri = null;

    private String currentUsername = "";
    private String currentEmail = "";
    private String currentCountry = "";

    // Data sementara untuk perubahan
    private String tempNewEmail = null;
    private boolean emailChanged = false;
    private String tempNewPassword = null;
    private boolean passwordChanged = false;

    private final String[] countries = {"Indonesia", "Malaysia", "Singapore", "Thailand", "China"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService();

        initViews();
        setupCountryDropdown();
        loadProfile();

        // Event listeners
        ivProfilePicture.setOnClickListener(v -> checkPermissionAndOpenGallery());
        findViewById(R.id.btn_change_email).setOnClickListener(v -> showChangeEmailDialog());
        findViewById(R.id.btn_change_password).setOnClickListener(v -> showChangePasswordDialog());
        btnSave.setOnClickListener(v -> updateProfile());
        btnLogout.setOnClickListener(v -> logout());

        // Navbar
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeUserActivity.class));
            finish();
        });
        findViewById(R.id.nav_search).setOnClickListener(v ->
                Toast.makeText(this, "Fitur pencarian belum tersedia", Toast.LENGTH_SHORT).show());
        findViewById(R.id.nav_message).setOnClickListener(v ->
                Toast.makeText(this, "Fitur pesan belum tersedia", Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        etUsername = findViewById(R.id.et_username);
        etCountry = findViewById(R.id.et_country);
        tvEmail = findViewById(R.id.tv_email);
        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupCountryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countries);
        etCountry.setAdapter(adapter);
        etCountry.setThreshold(1);
    }

    private void loadProfile() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        Call<Map<String, Object>> call = apiService.getProfile("Bearer " + token);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    currentUsername = (String) data.get("username");
                    currentEmail = (String) data.get("email");
                    currentCountry = (String) data.get("country");
                    String fotoUrl = (String) data.get("foto_profil");

                    etUsername.setText(currentUsername);
                    tvEmail.setText(currentEmail);
                    etCountry.setText(currentCountry, false);

                    // Reset flags
                    emailChanged = false;
                    passwordChanged = false;
                    tempNewEmail = null;
                    tempNewPassword = null;

                    // 🔽🔽🔽 LETAKKAN KODE GLIDE DI SINI 🔽🔽🔽
                    if (fotoUrl != null && !fotoUrl.equals("default-profile.png")) {
                        // Sesuaikan BASE_URL dengan ApiClient.BASE_URL (misal "http://10.0.2.2:8000/")
                        String fullUrl = ApiClient.BASE_URL + fotoUrl;
                        Glide.with(ProfileActivity.this)
                                .load(fullUrl)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)   // Jangan simpan cache disk
                                .skipMemoryCache(true)                       // Lewati memory cache
                                .placeholder(R.drawable.ic_default_profile)
                                .circleCrop()
                                .into(ivProfilePicture);
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.ic_default_profile);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ======================== GALERI & IZIN ========================
    private void checkPermissionAndOpenGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_STORAGE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Izin diperlukan untuk ganti foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(ivProfilePicture);
        }
    }

    // ======================== DIALOG GANTI EMAIL ========================
    private void showChangeEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ganti Email");

        final EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email baru");
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Password saat ini");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AlertDialog dialog = builder.create();
        // Tidak bisa pakai setView langsung karena dua EditText
        // Kita buat LinearLayout secara programatik
        androidx.appcompat.widget.LinearLayoutCompat layout = new androidx.appcompat.widget.LinearLayoutCompat(this);
        layout.setOrientation(androidx.appcompat.widget.LinearLayoutCompat.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputEmail);
        layout.addView(inputPassword);
        builder.setView(layout);

        builder.setPositiveButton("Ganti", (dialogInterface, which) -> {
            String newEmail = inputEmail.getText().toString().trim();
            String currentPass = inputPassword.getText().toString().trim();
            if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentPass.isEmpty()) {
                Toast.makeText(this, "Masukkan password saat ini", Toast.LENGTH_SHORT).show();
                return;
            }
            // Simpan sementara, nanti akan dikirim bersama current_password di updateProfile
            tempNewEmail = newEmail;
            emailChanged = true;
            tvEmail.setText(newEmail);
            Toast.makeText(this, "Email akan diperbarui saat klik Simpan", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // ======================== DIALOG GANTI PASSWORD ========================
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ganti Password");

        final EditText inputCurrent = new EditText(this);
        inputCurrent.setHint("Password saat ini");
        inputCurrent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final EditText inputNew = new EditText(this);
        inputNew.setHint("Password baru (min 8 karakter)");
        inputNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final EditText inputConfirm = new EditText(this);
        inputConfirm.setHint("Konfirmasi password baru");
        inputConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        androidx.appcompat.widget.LinearLayoutCompat layout = new androidx.appcompat.widget.LinearLayoutCompat(this);
        layout.setOrientation(androidx.appcompat.widget.LinearLayoutCompat.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputCurrent);
        layout.addView(inputNew);
        layout.addView(inputConfirm);
        builder.setView(layout);

        builder.setPositiveButton("Ganti", (dialog, which) -> {
            String currentPass = inputCurrent.getText().toString().trim();
            String newPass = inputNew.getText().toString().trim();
            String confirmPass = inputConfirm.getText().toString().trim();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Password baru tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }
            // Simpan sementara
            tempNewPassword = newPass;
            passwordChanged = true;
            Toast.makeText(this, "Password akan diperbarui saat klik Simpan", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // ======================== SIMPAN PERUBAHAN ========================
    private void updateProfile() {
        String newUsername = etUsername.getText().toString().trim();
        String newCountry = etCountry.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Nama pengguna tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasEmailChange = emailChanged && tempNewEmail != null;
        boolean hasPasswordChange = passwordChanged && tempNewPassword != null;

        // Jika ada perubahan email atau password, minta current password (lagi) untuk keamanan
        if (hasEmailChange || hasPasswordChange) {
            showConfirmCurrentPasswordDialog(newUsername, newCountry, hasEmailChange, hasPasswordChange);
        } else {
            // Tidak ada perubahan sensitif, langsung kirim
            sendUpdateRequest(newUsername, newCountry, null, null, null);
        }
    }

    private void showConfirmCurrentPasswordDialog(String newUsername, String newCountry, boolean hasEmailChange, boolean hasPasswordChange) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Password");
        final EditText input = new EditText(this);
        input.setHint("Masukkan password Anda untuk mengonfirmasi perubahan");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton("Konfirmasi", (dialog, which) -> {
            String currentPassword = input.getText().toString().trim();
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Password saat ini harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            String newEmail = hasEmailChange ? tempNewEmail : null;
            String newPass = hasPasswordChange ? tempNewPassword : null;
            sendUpdateRequest(newUsername, newCountry, currentPassword, newEmail, newPass);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void sendUpdateRequest(String username, String country, String currentPassword,
                                   String newEmail, String newPass) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Sesi habis", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        Map<String, RequestBody> textFields = new HashMap<>();
        textFields.put("username", createPartFromString(username));
        textFields.put("country", createPartFromString(country));

        if (currentPassword != null) {
            textFields.put("current_password", createPartFromString(currentPassword));
        }
        if (newEmail != null) {
            textFields.put("email", createPartFromString(newEmail));
        }
        if (newPass != null) {
            textFields.put("password", createPartFromString(newPass));
            textFields.put("password_confirmation", createPartFromString(newPass));
        }

        MultipartBody.Part fotoPart = null;
        if (selectedImageUri != null) {
            File file = getFileFromUri(selectedImageUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                fotoPart = MultipartBody.Part.createFormData("foto_profil", file.getName(), requestFile);
            }
        }

        Call<Map<String, Object>> call = apiService.updateProfileWithImage("Bearer " + token, textFields, fotoPart);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    // Muat ulang data dari server
                    loadProfile();
                    // Reset flags
                    emailChanged = false;
                    passwordChanged = false;
                    tempNewEmail = null;
                    tempNewPassword = null;
                    selectedImageUri = null;
                } else {
                    parseError(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("profile_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void parseError(Response<?> response) {
        try {
            String errorBody = response.errorBody().string();
            JSONObject json = new JSONObject(errorBody);
            String message = json.optString("message", "Terjadi kesalahan");
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    // ======================== LOGOUT ========================
    private void logout() {
        String token = tokenManager.getToken();
        // Hapus token lokal TERLEBIH DAHULU
        tokenManager.clearToken();

        if (token != null) {
            Call<Void> call = apiService.logout("Bearer " + token);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Token sudah dihapus, langsung ke login
                    goToLogin();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Meskipun gagal, tetap pindah ke login karena token lokal sudah hilang
                    goToLogin();
                }
            });
        } else {
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}