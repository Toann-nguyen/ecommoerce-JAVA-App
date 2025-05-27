package com.example.ecommerce.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ecommerce.HomeMainActivity;
import com.example.ecommerce.ProductReviewActivity;
import com.example.ecommerce.R;

/**
 * Diálogo personalizado que se muestra después de completar un pedido,
 * con opciones para revisar productos o continuar comprando.
 */
public class OrderCompleteDialog extends Dialog {

    private final String orderId;
    private final Context context;

    public OrderCompleteDialog(@NonNull Context context, String orderId) {
        super(context);
        this.context = context;
        this.orderId = orderId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_complete_review);

        // Configurar el ID del pedido
        TextView tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderId.setText("Mã đơn hàng: #" + orderId);

        // Configurar el botón para continuar comprando
        Button btnContinueShopping = findViewById(R.id.btnContinueShopping);
        btnContinueShopping.setOnClickListener(v -> {
            dismiss();
            Intent intent = new Intent(context, HomeMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        });

        // Configurar el botón para ir a la revisión de productos
        Button btnReviewProducts = findViewById(R.id.btnReviewProducts);
        btnReviewProducts.setOnClickListener(v -> {
            dismiss();
            // Ir directamente a la actividad de revisión
            Intent intent = new Intent(context, ProductReviewActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("DIRECT_REVIEW", true);
            context.startActivity(intent);
        });

        // Hacer que el diálogo ocupe la mayor parte del ancho de la pantalla
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
