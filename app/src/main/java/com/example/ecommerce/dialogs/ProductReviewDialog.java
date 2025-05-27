package com.example.ecommerce.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

import models.Product;
import models.Review;
import repository.ReviewRepository;

public class ProductReviewDialog extends Dialog {

    private final Product product;
    private final String orderId;
    private final Context context;
    private final ReviewSubmittedListener listener;

    private ImageView imgProduct;
    private TextView tvProductName, tvOrderId, tvProductPrice;
    private RatingBar ratingBar;
    private TextInputEditText etComment;
    private Button btnCancel, btnSubmitReview;

    private ReviewRepository reviewRepository;
    private FirebaseUser currentUser;

    public interface ReviewSubmittedListener {
        void onReviewSubmitted();
    }

    public ProductReviewDialog(@NonNull Context context, Product product, String orderId, ReviewSubmittedListener listener) {
        super(context);
        this.context = context;
        this.product = product;
        this.orderId = orderId;
        this.listener = listener;
        reviewRepository = new ReviewRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_product_review);

        // Inicializar vistas
        initViews();

        // Configurar información del producto
        displayProductInfo();

        // Configurar botones
        setupButtons();

        // Ajustar tamaño del diálogo
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initViews() {
        imgProduct = findViewById(R.id.imgProduct);
        tvProductName = findViewById(R.id.tvProductName);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnCancel = findViewById(R.id.btnCancel);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
    }

    private void displayProductInfo() {
        // Mostrar imagen del producto
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imgProduct);
        }

        // Mostrar nombre del producto
        tvProductName.setText(product.getName());

        // Mostrar ID de pedido
        tvOrderId.setText("Đơn hàng #" + orderId);

        // Mostrar precio del producto
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvProductPrice.setText(currencyFormat.format(product.getPrice()) + " đ");
    }

    private void setupButtons() {
        // Configurar botón Cancelar
        btnCancel.setOnClickListener(v -> dismiss());

        // Configurar botón Enviar comentario
        btnSubmitReview.setOnClickListener(v -> validateAndSubmitReview());
    }

    private void validateAndSubmitReview() {
        // Validar calificación
        float rating = ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(context, "Vui lòng đánh giá sao cho sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar comentario
        String comment = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(context, "Vui lòng nhập nhận xét về sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar usuario
        if (currentUser == null) {
            Toast.makeText(context, "Bạn cần đăng nhập để đánh giá sản phẩm", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

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

        // Mostrar diálogo de progreso
        AlertDialog progressDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(R.layout.progress_dialog)
                .create();
        progressDialog.show();

        // Enviar revisión a Firebase
        reviewRepository.createReview(review, new ReviewRepository.ReviewCallback() {
            @Override
            public void onSuccess(Review createdReview) {
                // Ocultar diálogo de progreso
                progressDialog.dismiss();

                // Mostrar mensaje de éxito
                Toast.makeText(context, "Cảm ơn bạn đã đánh giá sản phẩm!", Toast.LENGTH_SHORT).show();

                // Notificar al listener
                if (listener != null) {
                    listener.onReviewSubmitted();
                }

                // Cerrar diálogo
                dismiss();
            }

            @Override
            public void onError(String errorMessage) {
                // Ocultar diálogo de progreso
                progressDialog.dismiss();

                // Mostrar mensaje de error
                Toast.makeText(context, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}