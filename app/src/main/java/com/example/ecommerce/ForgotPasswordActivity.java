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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView messageTextView;
    private TextView backToLoginTextView;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        messageTextView = findViewById(R.id.messageTextView);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);
        progressBar = findViewById(R.id.progressBar);

        // Setup button click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to login screen
                finish();
            }
        });
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        // Validate email
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        // Check network connection
        if (!isNetworkAvailable()) {
            messageTextView.setText("No internet connection");
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        resetPasswordButton.setEnabled(false);

        // Send password reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        resetPasswordButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            messageTextView.setText("Password reset email sent. Check your email inbox.");
                            messageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            if (task.getException() != null) {
                                messageTextView.setText("Failed: " + task.getException().getMessage());
                            } else {
                                messageTextView.setText("Failed to send reset email. Please try again later.");
                            }
                            messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}