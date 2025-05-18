package models

import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = listOf(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val shippingAddress: String = "",
    val paymentMethod: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
} 