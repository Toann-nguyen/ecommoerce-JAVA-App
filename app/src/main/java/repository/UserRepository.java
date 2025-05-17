package repository;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.User;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private CollectionReference usersRef;

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.usersRef = db.collection("users");
    }

    /**
     * Lấy người dùng đang đăng nhập hiện tại
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Callback cho người dùng
     */
    public interface UserCallback {
        void onSuccess(User user);

        void onError(String errorMessage);
    }

    /**
     * Lấy thông tin chi tiết của người dùng hiện tại từ Firestore
     */
    public void getUserData(UserCallback callback) {
        FirebaseUser currentUser = getCurrentUser();

        if (currentUser == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        String uid = currentUser.getUid();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Không tìm thấy dữ liệu người dùng");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Callback cho các tác vụ thành công/lỗi
     */
    public interface TaskCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    /**
     * Cập nhật thông tin người dùng trong Firestore
     */
    public void updateUserData(User user, TaskCallback callback) {
        if (getCurrentUser() == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        String uid = getCurrentUser().getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", user.getFullName());
        userMap.put("phone", user.getPhone());
        userMap.put("address", user.getAddress());

        if (user.getAvatarUrl() != null) {
            userMap.put("avatarUrl", user.getAvatarUrl());
        }

        db.collection("users")
                .document(uid)
                .update(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Update display name in Firebase Auth
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getFullName())
                            .build();

                    getCurrentUser().updateProfile(profileUpdates)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Callback cho upload hình ảnh
     */
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);

        void onError(String errorMessage);

        void onProgress(double progress);
    }

    /**
     * Upload hình ảnh đại diện
     */
    public void uploadProfileImage(Uri imageUri, ImageUploadCallback callback) {
        if (getCurrentUser() == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        String uid = getCurrentUser().getUid();
        StorageReference storageRef = storage.getReference().child("profile_images/" + uid + ".jpg");

        UploadTask uploadTask = storageRef.putFile(imageUri);

        // Theo dõi tiến trình upload
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            callback.onProgress(progress);
        });

        // Xử lý sau khi upload hoàn tất
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageRef.getDownloadUrl();
        }).addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();

            // Cập nhật URL ảnh trong Firebase Auth
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

            getCurrentUser().updateProfile(profileUpdates);

            // Cập nhật URL ảnh trong Firestore
            db.collection("users")
                    .document(uid)
                    .update("avatarUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(imageUrl))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Đăng xuất người dùng hiện tại
     */
    public void signOut() {
        auth.signOut();
    }

    public void getUsersByIds(List<String> userIds, OnUsersLoadedListener listener) {
        List<User> users = new ArrayList<>();

        if (userIds.isEmpty()) {
            listener.onUsersLoaded(users);
            return;
        }

        // Counter to track completed queries
        final int[] counter = {userIds.size()};

        for (String userId : userIds) {
            usersRef.document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Set the user ID since it's the document ID
                            user.setUserId(documentSnapshot.getId());
                            users.add(user);
                        }

                        counter[0]--;
                        if (counter[0] == 0) {
                            // All queries completed
                            listener.onUsersLoaded(users);
                        }
                    })
                    .addOnFailureListener(e -> {
                        counter[0]--;
                        if (counter[0] == 0) {
                            if (users.isEmpty()) {
                                listener.onError(e.getMessage());
                            } else {
                                listener.onUsersLoaded(users);
                            }
                        }
                    });
        }
    }

    public interface OnUsersLoadedListener {
        void onUsersLoaded(List<User> users);

        void onError(String error);
    }
}