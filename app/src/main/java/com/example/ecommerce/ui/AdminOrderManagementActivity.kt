package com.example.ecommerce.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.R
import com.example.ecommerce.adapters.OrderAdapter
import com.example.ecommerce.models.Order
import com.example.ecommerce.models.OrderStatus
import com.example.ecommerce.repository.OrderRepository
import kotlinx.coroutines.launch

class AdminOrderManagementActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private val orderRepository = OrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_management)

        recyclerView = findViewById(R.id.ordersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
        loadAllOrders()
    }

    private fun setupAdapter() {
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onOrderClick = { order -> showOrderManagementDialog(order) },
            isAdmin = true
        )
        recyclerView.adapter = orderAdapter
    }

    private fun loadAllOrders() {
        lifecycleScope.launch {
            try {
                orderRepository.getAllOrders().fold(
                    onSuccess = { orders ->
                        orderAdapter = OrderAdapter(
                            orders = orders,
                            onOrderClick = { order -> showOrderManagementDialog(order) },
                            isAdmin = true
                        )
                        recyclerView.adapter = orderAdapter
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@AdminOrderManagementActivity,
                            "Error loading orders: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminOrderManagementActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showOrderManagementDialog(order: Order) {
        val statuses = OrderStatus.values()
        val statusNames = statuses.map { it.name }

        AlertDialog.Builder(this)
            .setTitle("Manage Order #${order.id.take(8)}")
            .setItems(statusNames.toTypedArray()) { _, which ->
                updateOrderStatus(order, statuses[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOrderStatus(order: Order, newStatus: OrderStatus) {
        lifecycleScope.launch {
            try {
                orderRepository.updateOrderStatus(order.id, newStatus).fold(
                    onSuccess = {
                        Toast.makeText(
                            this@AdminOrderManagementActivity,
                            "Order status updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadAllOrders() // Refresh the list
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@AdminOrderManagementActivity,
                            "Error updating order status: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminOrderManagementActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
} 