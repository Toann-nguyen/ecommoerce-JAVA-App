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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class MainActivity extends AppCompatActivity {

    // khai báo biến cho các thành phần giao diện
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView messageTextView;
    private Button registerButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        messageTextView = findViewById(R.id.messageTextView);
        // dang ky
        registerButton = findViewById(R.id.registerButton);

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
                                    // Đăng nhập thành công
                                    messageTextView.setText("Đăng nhập thành công");
                                    Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Exception e = task.getException();
                                    if (e instanceof FirebaseAuthInvalidUserException) {
                                        // Tài khoản chưa tồn tại
                                        messageTextView.setText("Không có tài khoản");
                                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        // Sai email hoặc mật khẩu
                                        messageTextView.setText("Email hoặc mật khẩu không đúng");
                                    } else if (e instanceof FirebaseNetworkException) {
                                        // Lỗi mạng từ Firebase
                                        messageTextView.setText("Đăng nhập thất bại: Lỗi mạng, vui lòng kiểm tra kết nối");
                                    } else {
                                        // Các lỗi khác
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
    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng đã đăng nhập, hiển thị thông báo
        // Nếu người dùng đã đăng nhập, chuyển sang một activity khác (ví dụ: trang chính)
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
            startActivity(intent);
          //  finish(); // Kết thúc MainActivity để không quay lại
        }
    }
}