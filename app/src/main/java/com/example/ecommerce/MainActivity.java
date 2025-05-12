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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.common.SignInButton;

public class MainActivity extends AppCompatActivity  implements GoogleSignInHelper.GoogleSignInCallback  {

    // khai báo biến cho các thành phần giao diện
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView messageTextView;
    private Button registerButton;
    private FirebaseAuth auth;

    private GoogleSignInHelper googleSignInHelper;
    private SignInButton googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();
        // Khởi tạo GoogleSignInHelper
        googleSignInHelper = new GoogleSignInHelper(this, this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        messageTextView = findViewById(R.id.messageTextView);
        // dang ky
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

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
                                    messageTextView.setText("Đăng nhập thành công");
                                    Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
                                    startActivity(intent);
                                    finish();
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
        // Thiết lập sự kiện cho nút đăng nhập bằng Google
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    messageTextView.setText("Không có kết nối internet");
                    return;
                }
                // Gọi phương thức đăng nhập từ GoogleSignInHelper
                googleSignInHelper.signIn();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Chuyển kết quả cho GoogleSignInHelper xử lý
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data);
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
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onSignInSuccess(FirebaseUser user) {
        String userName = user.getDisplayName();
        Toast.makeText(this, "Xin chào " + userName, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, HomeMainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSignInFailure(Exception e) {
        messageTextView.setText("Đăng nhập Google thất bại: " + e.getMessage());
    }
}