package com.example.ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ecommerce.R;
import com.example.ecommerce.adapters.AdminReviewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import models.Review;
import repository.ReviewRepository;

public class AdminRecentReviewsFragment extends Fragment implements AdminReviewAdapter.ReviewActionListener {

    private RecyclerView rvReviews;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private ProgressBar progressBar;

    private AdminReviewAdapter adapter;
    private ReviewRepository reviewRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reviewRepository = new ReviewRepository();

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadReviews();
    }

    private void initViews(View view) {
        rvReviews = view.findViewById(R.id.rvReviews);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new AdminReviewAdapter(new ArrayList<>(), getContext(), this);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadReviews);
    }

    private void loadReviews() {
        showLoading();

        reviewRepository.getRecentReviews(new ReviewRepository.ReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                hideLoading();
                updateUI(reviews);
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                showError(errorMessage);
            }
        });
    }

    private void updateUI(List<Review> reviews) {
        adapter.updateData(reviews);

        if (reviews.isEmpty()) {
            showEmptyState("Không có đánh giá nào");
        } else {
            hideEmptyState();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvReviews.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
        rvReviews.setVisibility(View.VISIBLE);
    }

    private void showError(String errorMessage) {
        Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
        showEmptyState("Đã xảy ra lỗi khi tải đánh giá");
    }

    @Override
    public void onDeleteReview(Review review) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteReview(review))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteReview(Review review) {
        if (review.getId() == null) return;

        showLoading();
        reviewRepository.deleteReview(
                review.getId(),
                aVoid -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                    loadReviews(); // Reload list after delete
                },
                e -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Lỗi khi xóa đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}