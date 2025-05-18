package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import models.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit,
    private val isAdmin: Boolean = false
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(android.R.id.text1)
        val orderDate: TextView = view.findViewById(android.R.id.text2)
        val orderStatus: TextView = view.findViewById(android.R.id.text3)
        val orderTotal: TextView = view.findViewById(android.R.id.text4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_4, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        holder.orderId.text = "Order #${order.id.take(8)}"
        holder.orderDate.text = "Date: ${dateFormat.format(order.createdAt)}"
        holder.orderStatus.text = "Status: ${order.status}"
        holder.orderTotal.text = "Total: $${String.format("%.2f", order.totalAmount)}"

        holder.itemView.setOnClickListener { onOrderClick(order) }
    }

    override fun getItemCount() = orders.size
} 