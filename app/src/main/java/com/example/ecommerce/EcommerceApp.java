package com.example.ecommerce;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import models.User;
import utils.PermissionManager;

public class EcommerceApp extends Application {
    private static final String TAG = "EcommerceApp";
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Đảm bảo có tài khoản admin
        ensureAdminExists();
    }

    private void ensureAdminExists() {
        // Kiểm tra tài khoản admin trong Firestore
        db.collection("users")
                .whereEqualTo("role", "admin")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "No admin found, creating one");
                        // Nếu không có admin nào, tạo tài khoản admin
                        String adminEmail = "admin@example.com";
                        String adminPassword = "admin123";

                        auth.createUserWithEmailAndPassword(adminEmail, adminPassword)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = task.getResult().getUser();
                                        if (user != null) {
                                            Log.d(TAG, "Admin account created with UID: " + user.getUid());
                                            createAdminUser(user.getUid(), adminEmail);
                                        }
                                    } else {
                                        Log.e(TAG, "Error creating admin account", task.getException());
                                        // Kiểm tra nếu email đã tồn tại, thử đăng nhập và cập nhật quyền admin
                                        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
                                                .addOnCompleteListener(loginTask -> {
                                                    if (loginTask.isSuccessful()) {
                                                        FirebaseUser existingUser = loginTask.getResult().getUser();
                                                        if (existingUser != null) {
                                                            createAdminUser(existingUser.getUid(), adminEmail);

                                                            // Đảm bảo cập nhật thủ công tài khoản admin trong PermissionManager
                                                            db.collection("users")
                                                                    .document(existingUser.getUid())
                                                                    .get()
                                                                    .addOnSuccessListener(documentSnapshot -> {
                                                                        if (documentSnapshot.exists()) {
                                                                            User adminUser = documentSnapshot.toObject(User.class);
                                                                            if (adminUser != null) {
                                                                                PermissionManager.getInstance().setCurrentUser(adminUser);
                                                                                Log.d(TAG, "Admin user set in PermissionManager");
                                                                            }
                                                        }
                                                    });
                                            }
                                            // Đăng xuất để không ảnh hưởng đến trạng thái đăng nhập
                                            auth.signOut();
                                        } else {
                                            Log.e(TAG, "Error signing in with existing email", loginTask.getException());
                                        }
                                    });
                            }
                        });
                } else {
                    Log.d(TAG, "Admin account already exists");
                }
            })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking for admin accounts", e));
    }

    private void createAdminUser(String uid, String email) {
        User adminUser = new User(uid, email, "Admin", "", "", "", "admin");

        // Thêm quyền cho admin
        adminUser.addPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS);
        adminUser.addPermission(PermissionManager.PERMISSION_MANAGE_ORDERS);
        adminUser.addPermission(PermissionManager.PERMISSION_MANAGE_USERS);
        adminUser.addPermission(PermissionManager.PERMISSION_VIEW_REPORTS);

        // Lưu vào Firestore
        db.collection("users")
                .document(uid)
                .set(adminUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Admin user data saved to Firestore");
                    // Tạo một PermissionManager instance và cập nhật thông tin người dùng hiện tại
                    PermissionManager.getInstance().setCurrentUser(adminUser);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error saving admin user data", e));
    }
}