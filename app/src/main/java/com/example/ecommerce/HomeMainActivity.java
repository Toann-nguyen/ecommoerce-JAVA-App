package com.example.ecommerce;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseNetworkException;

public class HomeMainActivity extends AppCompatActivity {
    private Button logoutButton;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Liên kết nút đăng xuất
        logoutButton = findViewById(R.id.logoutButton);

        // Sự kiện nhấn nút đăng xuất
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đăng xuất khỏi Firebase
                auth.signOut();

                // Chuyển hướng về MainActivity
                Intent intent = new Intent(HomeMainActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Kết thúc HomeActivity để không quay lại
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng chưa đăng nhập, chuyển về MainActivity
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(HomeMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}