package com.example.ecommerce.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.ecommerce.MainActivity;
import com.example.ecommerce.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import models.User;
import repository.UserRepository;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    // View cho profile screen
    private ImageView ivProfileAvatar;
    private TextView tvProfileFullName, tvProfileEmail, tvProfilePhone, tvProfileAddress;
    private MaterialButton btnEditProfile, btnLogout;
    private ProgressBar profileProgressBar;
    private View profileView;

    // View cho edit profile screen
    private ImageView ivEditProfileAvatar;
    private TextInputEditText etFullName, etEmail, etPhone, etAddress;
    private Button btnSaveProfile;
    private ImageButton btnBack, btnChangeAvatar;
    private ProgressBar editProfileProgressBar;
    private View editProfileView;

    // Quản lý dữ liệu
    private UserRepository userRepository;
    private User currentUser;
    private Uri selectedImageUri;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the profile view
        profileView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inflate the edit profile view (but don't attach to container)
        editProfileView = inflater.inflate(R.layout.fragment_edit_profile, null);

        // Khởi tạo repository
        userRepository = new UserRepository();

        // Thiết lập views
        initProfileViews();
        initEditProfileViews();

        // Tải dữ liệu người dùng
        loadUserData();

        return profileView;
    }

    private void initProfileViews() {
        ivProfileAvatar = profileView.findViewById(R.id.ivProfileAvatar);
        tvProfileFullName = profileView.findViewById(R.id.tvProfileFullName);
        tvProfileEmail = profileView.findViewById(R.id.tvProfileEmail);
        tvProfilePhone = profileView.findViewById(R.id.tvProfilePhone);
        tvProfileAddress = profileView.findViewById(R.id.tvProfileAddress);
        btnEditProfile = profileView.findViewById(R.id.btnEditProfile);
        btnLogout = profileView.findViewById(R.id.btnLogout);
        profileProgressBar = profileView.findViewById(R.id.profileProgressBar);

        // Set listeners
        btnEditProfile.setOnClickListener(v -> switchToEditMode());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void initEditProfileViews() {
        ivEditProfileAvatar = editProfileView.findViewById(R.id.ivEditProfileAvatar);
        etFullName = editProfileView.findViewById(R.id.etFullName);
        etEmail = editProfileView.findViewById(R.id.etEmail);
        etPhone = editProfileView.findViewById(R.id.etPhone);
        etAddress = editProfileView.findViewById(R.id.etAddress);
        btnSaveProfile = editProfileView.findViewById(R.id.btnSaveProfile);
        btnBack = editProfileView.findViewById(R.id.btnBack);
        btnChangeAvatar = editProfileView.findViewById(R.id.btnChangeAvatar);
        editProfileProgressBar = editProfileView.findViewById(R.id.editProfileProgressBar);

        // Set listeners
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnBack.setOnClickListener(v -> switchToViewMode());
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
    }

    private void loadUserData() {
        profileProgressBar.setVisibility(View.VISIBLE);

        userRepository.getUserData(new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                profileProgressBar.setVisibility(View.GONE);
                currentUser = user;
                displayUserData();
            }

            @Override
            public void onError(String errorMessage) {
                profileProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserData() {
        if (currentUser != null) {
            // Hiển thị thông tin trong profile view
            tvProfileFullName.setText(currentUser.getFullName());
            tvProfileEmail.setText(currentUser.getEmail());
            tvProfilePhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "Chưa cập nhật");
            tvProfileAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "Chưa cập nhật");

            // Load avatar nếu có
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getAvatarUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivProfileAvatar);
            }

            // Đưa dữ liệu vào edit form
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());
            etAddress.setText(currentUser.getAddress());

            // Load avatar cho edit view
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getAvatarUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivEditProfileAvatar);
            }
        }
    }

    private void switchToEditMode() {
        isEditMode = true;

        // Replace the current view with edit profile view
        ViewGroup parent = (ViewGroup) profileView.getParent();
        int index = parent.indexOfChild(profileView);

        parent.removeView(profileView);
        parent.addView(editProfileView, index);
    }

    private void switchToViewMode() {
        isEditMode = false;

        // Replace edit profile view with profile view
        ViewGroup parent = (ViewGroup) editProfileView.getParent();
        int index = parent.indexOfChild(editProfileView);

        parent.removeView(editProfileView);
        parent.addView(profileView, index);

        // Reload user data to refresh the view
        loadUserData();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Hiển thị ảnh đã chọn
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivEditProfileAvatar);
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            return;
        }

        editProfileProgressBar.setVisibility(View.VISIBLE);

        // Nếu có ảnh mới, upload trước
        if (selectedImageUri != null) {
            uploadProfileImage();
        } else {
            // Nếu không có ảnh mới, chỉ cập nhật thông tin khác
            updateUserData(fullName, phone, address, currentUser.getAvatarUrl());
        }
    }

    private void uploadProfileImage() {
        userRepository.uploadProfileImage(selectedImageUri, new UserRepository.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                // Sau khi upload ảnh xong, cập nhật thông tin người dùng
                String fullName = etFullName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String address = etAddress.getText().toString().trim();

                updateUserData(fullName, phone, address, imageUrl);
            }

            @Override
            public void onError(String errorMessage) {
                editProfileProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi upload ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(double progress) {
                // Hiển thị tiến trình upload nếu cần
            }
        });
    }

    private void updateUserData(String fullName, String phone, String address, String avatarUrl) {
        currentUser.setFullName(fullName);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);
        currentUser.setAvatarUrl(avatarUrl);

        userRepository.updateUserData(currentUser, new UserRepository.TaskCallback() {
            @Override
            public void onSuccess() {
                editProfileProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                switchToViewMode();
            }

            @Override
            public void onError(String errorMessage) {
                editProfileProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        userRepository.signOut();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}