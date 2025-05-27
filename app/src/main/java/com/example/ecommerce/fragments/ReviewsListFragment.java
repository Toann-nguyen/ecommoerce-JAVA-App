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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ecommerce.R;
import com.example.ecommerce.adapters.ReviewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import models.Review;
import repository.ReviewRepository;

public class ReviewsListFragment extends Fragment {

    public static final String ARG_TYPE = "type";
    public static final int TYPE_RECENT = 0;
    public static final int TYPE_MY_REVIEWS = 1;
    public static final int TYPE_PRODUCT_REVIEWS = 2;

    private int type;
    private String productId;

    private RecyclerView rvReviews;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private ProgressBar progressBar;

    private ReviewAdapter adapter;
    private ReviewRepository reviewRepository;
    private FirebaseUser currentUser;

    public static ReviewsListFragment newInstance(int type) {
        ReviewsListFragment fragment = new ReviewsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReviewsListFragment newInstance(int type, String productId) {
        ReviewsListFragment fragment = new ReviewsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString("product_id", productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE, TYPE_RECENT);
            productId = getArguments().getString("product_id");
        }

        reviewRepository = new ReviewRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        adapter = new ReviewAdapter(new ArrayList<>(), getContext());
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadReviews);
    }

    private void loadReviews() {
        showLoading();

        switch (type) {
            case TYPE_RECENT:
                loadRecentReviews();
                break;
            case TYPE_MY_REVIEWS:
                loadMyReviews();
                break;
            case TYPE_PRODUCT_REVIEWS:
                loadProductReviews();
                break;
        }
    }

    private void loadRecentReviews() {
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

    private void loadMyReviews() {
        if (currentUser == null) {
            hideLoading();
            showEmptyState("Vui lòng đăng nhập để xem đánh giá của bạn");
            return;
        }

        reviewRepository.getReviewsByUserId(currentUser.getUid(), new ReviewRepository.ReviewsCallback() {
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

    private void loadProductReviews() {
        if (productId == null) {
            hideLoading();
            showEmptyState("Không tìm thấy thông tin sản phẩm");
            return;
        }

        reviewRepository.getReviewsByProductId(productId, new ReviewRepository.ReviewsCallback() {
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
}