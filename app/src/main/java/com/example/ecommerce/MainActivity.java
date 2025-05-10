package com.example.ecommerce;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.ecommerce.admin.AdminDashboardActivity;
import com.example.ecommerce.admin.AdminPanelActivity;

import models.User;
import utils.PermissionManager;

public class MainActivity extends AppCompatActivity {

    // khai báo biến cho các thành phần giao diện
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView messageTextView;
    private TextView forgotPasswordTextView;
    private Button registerButton;
    private Button btnAdmin;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Khởi tạo PermissionManager
        permissionManager = PermissionManager.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        messageTextView = findViewById(R.id.messageTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        // dang ky
        registerButton = findViewById(R.id.registerButton);
//        btnAdmin = findViewById(R.id.btnAdmin);

        setupButtons();
    }

    private void setupButtons() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    messageTextView.setText("Không có kết nối internet");
                    return;
                }
                String email = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                if (!validateInput(email, password)) {
                    return;
                }
                // Đăng nhập với Firebase
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    loginSuccess(task.getResult().getUser());
                                } else {
                                    Exception e = task.getException();
                                    if (e instanceof FirebaseAuthInvalidUserException) {
                                        messageTextView.setText("Không có tài khoản");
                                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        messageTextView.setText("Email hoặc mật khẩu không đúng");
                                    } else if (e instanceof FirebaseNetworkException) {
                                        messageTextView.setText("Đăng nhập thất bại: Lỗi mạng, vui lòng kiểm tra kết nối");
                                    } else {
                                        messageTextView.setText("Đăng nhập thất bại: " + e.getLocalizedMessage());
                                    }
                                }
                            }
                        });
            }
        });
        // chuyen sang trang RegisterActivity
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang RegisterActivity
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });

        // Setup forgot password text view
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to forgot password screen
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Admin access button (for testing)
        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kiểm tra người dùng đã đăng nhập chưa
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        messageTextView.setText("Vui lòng đăng nhập trước");
                        return;
                    }

                    // Tải thông tin người dùng với callback để đảm bảo đã có dữ liệu mới kiểm tra
                    permissionManager.loadCurrentUser(new PermissionManager.UserLoadCallback() {
                        @Override
                        public void onUserLoaded(User user) {
                            if (permissionManager.isAdmin()) {
                                // Hiển thị thông tin để debug
                                Toast.makeText(MainActivity.this,
                                        "Admin: " + user.getEmail() + ", Vai trò: " + user.getRole(),
                                        Toast.LENGTH_LONG).show();

                                // Chuyển đến trang Admin Panel
                                startActivity(new Intent(MainActivity.this, AdminPanelActivity.class));
                            } else {
                                // Nếu không phải admin, tạo quyền admin thử
                                User adminUser = new User(
                                        currentUser.getUid(),
                                        currentUser.getEmail(),
                                        currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Admin User",
                                        "", "", "", "admin"
                                );

                                // Thêm các quyền cho admin
                                adminUser.addPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS);
                                adminUser.addPermission(PermissionManager.PERMISSION_MANAGE_ORDERS);
                                adminUser.addPermission(PermissionManager.PERMISSION_MANAGE_USERS);
                                adminUser.addPermission(PermissionManager.PERMISSION_VIEW_REPORTS);

                                // Lưu vào Firestore và cập nhật PermissionManager
                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(currentUser.getUid())
                                        .set(adminUser)
                                        .addOnSuccessListener(aVoid -> {
                                            permissionManager.setCurrentUser(adminUser);
                                            Toast.makeText(MainActivity.this,
                                                    "Đã cấp quyền Admin, vui lòng thử lại",
                                                    Toast.LENGTH_LONG).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            messageTextView.setText("Lỗi cấp quyền: " + e.getMessage());
                                        });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            messageTextView.setText("Lỗi tải thông tin: " + error);
                        }
                    });
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Kiểm tra định dạng email và mật khẩu
    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            messageTextView.setText("Vui lòng nhập email hợp lệ");
            return false;
        }
        if (password.length() < 6) {
            messageTextView.setText("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        return true;
    }

    private void loginSuccess(FirebaseUser user) {
        // Hiển thị thông báo đăng nhập thành công
        messageTextView.setText("Đăng nhập thành công!");
        messageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        // Kiểm tra kết nối internet
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối mạng. Một số tính năng có thể không hoạt động đúng.", Toast.LENGTH_LONG).show();
        }

        // Tải thông tin người dùng và quyền hạn với callback
        permissionManager.loadCurrentUser(new PermissionManager.UserLoadCallback() {
            @Override
            public void onUserLoaded(User user) {
                // Người dùng đã được tải, chuyển hướng đến màn hình phù hợp
                if (permissionManager.isAdmin()) {
                    // Đối với Admin, chuyển đến trang quản trị riêng
                    Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                } else {
                    // Đối với người dùng thường, chuyển đến trang chính
                    Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void onError(String error) {
                // Có lỗi khi tải thông tin người dùng
                Toast.makeText(MainActivity.this,
                        "Lỗi tải thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
                // Chuyển đến màn hình chính mặc định
                Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng đã đăng nhập, hiển thị thông báo
        // Nếu người dùng đã đăng nhập, chuyển sang một activity khác (ví dụ: trang chính)
        if (auth.getCurrentUser() != null) {
            permissionManager.loadCurrentUser(new PermissionManager.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    loginSuccess(auth.getCurrentUser());
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, "Lỗi tải thông tin người dùng: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}