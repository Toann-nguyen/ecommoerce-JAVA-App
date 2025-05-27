package com.example.ecommerce;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.CartItem;
import models.Order;
import models.Product;
import models.Review;
import repository.FirebaseRepository;
import repository.ReviewRepository;

public class ProductReviewActivity extends AppCompatActivity {

    private String orderId;
    private boolean directReview;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewProducts;
    private View emptyView;
    private View loadingView;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private ReviewRepository reviewRepository;

    // Interface for product click events
    private interface ProductClickCallback {
        void onProductClick(Product product);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review_list);

        // Verificar usuario
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để đánh giá sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        reviewRepository = new ReviewRepository();

        // Obtener orderId
        orderId = getIntent().getStringExtra("ORDER_ID");
        directReview = getIntent().getBooleanExtra("DIRECT_REVIEW", false);

        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadOrderProducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        emptyView = findViewById(R.id.emptyView);
        loadingView = findViewById(R.id.loadingView);

        // Setup RecyclerView
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        toolbar.setTitle("Đánh giá sản phẩm");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadOrderProducts() {
        showLoading();

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideLoading();

                    Order order = documentSnapshot.toObject(Order.class);
                    if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    displayProducts(order);
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void displayProducts(Order order) {
        List<CartItem> items = order.getItems();

        // Si es revisión directa y solo hay un producto, mostrar el diálogo inmediatamente
        if (directReview && items.size() == 1) {
            Product product = items.get(0).getProduct();
            if (product != null) {
                showReviewDialog(product);
            }
            return;
        }

        // Preparar adapter
        ProductForReviewAdapter adapter = new ProductForReviewAdapter(items,
                product -> showReviewDialog(product));
        recyclerViewProducts.setAdapter(adapter);
    }

    private void showReviewDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_review, null);

        ImageView imgProduct = dialogView.findViewById(R.id.imgProduct);
        TextView tvProductName = dialogView.findViewById(R.id.tvProductName);
        TextView tvOrderId = dialogView.findViewById(R.id.tvOrderId);
        TextView tvProductPrice = dialogView.findViewById(R.id.tvProductPrice);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        TextInputEditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);

        // Configurar información del producto
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imgProduct);
        }

        tvProductName.setText(product.getName());
        tvOrderId.setText("Đơn hàng #" + orderId);

        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvProductPrice.setText(currencyFormat.format(product.getPrice()) + " đ");

        // Crear AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configurar botones
        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (directReview) {
                finish(); // Si es revisión directa, cerrar la actividad al cancelar
            }
        });

        btnSubmitReview.setOnClickListener(v -> {
            // Validar entrada
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng đánh giá sao cho sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(this, "Vui lòng nhập nhận xét về sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear y enviar la revisión
            submitReview(product, rating, comment, alertDialog);
        });

        // Mostrar el diálogo
        alertDialog.show();
    }

    private void submitReview(Product product, float rating, String comment, AlertDialog dialogToClose) {
        // Mostrar loading
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.progress_dialog)
                .create();
        progressDialog.show();

        // Crear objeto de revisión
        Review review = new Review(
                currentUser.getUid(),
                currentUser.getEmail(),
                currentUser.getDisplayName(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                comment,
                rating,
                orderId
        );

        // Guardar en Firebase
        reviewRepository.createReview(review, new ReviewRepository.ReviewCallback() {
            @Override
            public void onSuccess(Review createdReview) {
                progressDialog.dismiss();
                Toast.makeText(ProductReviewActivity.this, "Đánh giá của bạn đã được gửi thành công", Toast.LENGTH_SHORT).show();
                dialogToClose.dismiss();

                // Si es revisión directa o ya no quedan productos por revisar, volver a la pantalla anterior
                if (directReview) {
                    finish();
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(ProductReviewActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        recyclerViewProducts.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingView.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        recyclerViewProducts.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    // Adapter para mostrar productos en RecyclerView
    private class ProductForReviewAdapter extends RecyclerView.Adapter<ProductForReviewAdapter.ViewHolder> {

        private List<CartItem> items;
        private ProductClickCallback listener;

        ProductForReviewAdapter(List<CartItem> items, ProductClickCallback listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_product_for_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CartItem item = items.get(position);
            Product product = item.getProduct();

            if (product != null) {
                holder.bind(product);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvProductName, tvProductPrice, tvQuantity;
            Button btnReview;

            ViewHolder(View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                btnReview = itemView.findViewById(R.id.btnReview);
            }

            void bind(Product product) {
                // Imagen del producto
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Glide.with(ProductReviewActivity.this)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imgProduct);
                }

                // Nombre del producto
                tvProductName.setText(product.getName());

                // Precio del producto
                NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                tvProductPrice.setText(currencyFormat.format(product.getPrice()) + " đ");

                // Cantidad
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    tvQuantity.setText("Số lượng: " + items.get(position).getQuantity());
                }

                // Botón para revisar
                btnReview.setOnClickListener(v -> {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onProductClick(product);
                    }
                });
            }
        }
    }
}
