package com.example.ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

import models.Product;
import models.Review;
import repository.FirebaseRepository;
import repository.ReviewRepository;

public class AdminProductReviewsFragment extends Fragment implements AdminReviewAdapter.ReviewActionListener {

    private Spinner spinnerProducts;
    private RecyclerView rvReviews;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private ProgressBar progressBar;

    private AdminReviewAdapter adapter;
    private ReviewRepository reviewRepository;
    private FirebaseRepository firebaseRepository;

    private List<Product> products = new ArrayList<>();
    private String selectedProductId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_product_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reviewRepository = new ReviewRepository();
        firebaseRepository = new FirebaseRepository();

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadProducts();
    }

    private void initViews(View view) {
        spinnerProducts = view.findViewById(R.id.spinnerProducts);
        rvReviews = view.findViewById(R.id.rvReviews);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        progressBar = view.findViewById(R.id.progressBar);

        spinnerProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= products.size()) {
                    selectedProductId = products.get(position - 1).getId(); // -1 because first item is "Chọn sản phẩm"
                    loadReviews();
                } else {
                    selectedProductId = null;
                    adapter.updateData(new ArrayList<>());
                    showEmptyState("Hãy chọn một sản phẩm để xem đánh giá");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProductId = null;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminReviewAdapter(new ArrayList<>(), getContext(), this);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            if (selectedProductId != null) {
                loadReviews();
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadProducts() {
        showLoading();

        firebaseRepository.getAllProducts(new FirebaseRepository.ProductsCallback() {
            @Override
            public void onCallback(List<Product> productList) {
                products.clear();
                products.addAll(productList);
                populateProductsSpinner();
                hideLoading();
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                Toast.makeText(getContext(), "Lỗi khi tải sản phẩm: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateProductsSpinner() {
        List<String> productNames = new ArrayList<>();
        productNames.add("Chọn sản phẩm");

        for (Product product : products) {
            productNames.add(product.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                productNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducts.setAdapter(adapter);
    }

    private void loadReviews() {
        if (selectedProductId == null) {
            return;
        }

        showLoading();

        reviewRepository.getReviewsByProductId(selectedProductId, new ReviewRepository.ReviewsCallback() {
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
            showEmptyState("Không có đánh giá nào cho sản phẩm này");
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
