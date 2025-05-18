package com.example.ecommerce.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.R
import com.example.ecommerce.adapters.OrderAdapter
import com.example.ecommerce.models.Order
import com.example.ecommerce.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserOrderHistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_order_history)

        recyclerView = findViewById(R.id.ordersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
        loadUserOrders()
    }

    private fun setupAdapter() {
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onOrderClick = { order -> showOrderDetails(order) }
        )
        recyclerView.adapter = orderAdapter
    }

    private fun loadUserOrders() {
        val userId = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            try {
                orderRepository.getUserOrders(userId).fold(
                    onSuccess = { orders ->
                        orderAdapter = OrderAdapter(
                            orders = orders,
                            onOrderClick = { order -> showOrderDetails(order) }
                        )
                        recyclerView.adapter = orderAdapter
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@UserOrderHistoryActivity,
                            "Error loading orders: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@UserOrderHistoryActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showOrderDetails(order: Order) {
        // TODO: Implement order details dialog or activity
        Toast.makeText(this, "Order #${order.id}", Toast.LENGTH_SHORT).show()
    }
} 