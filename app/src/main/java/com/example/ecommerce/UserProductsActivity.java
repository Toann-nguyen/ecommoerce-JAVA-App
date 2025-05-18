package com.example.ecommerce;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.adapter.SimpleProductAdapter;
import com.example.ecommerce.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SimpleProductAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_products);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        db = FirebaseFirestore.getInstance();
        loadUserProducts();
    }

    private void loadUserProducts() {
        String userEmail = "nguyenminhtoan2712py@gmail.com"; // Get from user session
        
        db.collection("orders")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Product> products = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Get items array from order
                    List<Product> orderProducts = document.get("items", List.class);
                    if (orderProducts != null) {
                        products.addAll(orderProducts);
                    }
                }
                
                adapter = new SimpleProductAdapter(products);
                recyclerView.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                // Handle error
            });
    }
}
