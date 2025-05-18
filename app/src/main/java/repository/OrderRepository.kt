package repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import models.Order
import models.OrderStatus

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    suspend fun createOrder(order: Order): Result<String> = try {
        val docRef = ordersCollection.document()
        val orderWithId = order.copy(id = docRef.id)
        docRef.set(orderWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserOrders(userId: String): Result<List<Order>> = try {
        val snapshot = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
        Result.success(orders)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllOrders(): Result<List<Order>> = try {
        val snapshot = ordersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
        Result.success(orders)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> = try {
        ordersCollection.document(orderId)
            .update("status", newStatus)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getOrderById(orderId: String): Result<Order> = try {
        val doc = ordersCollection.document(orderId).get().await()
        val order = doc.toObject(Order::class.java)
        if (order != null) {
            Result.success(order)
        } else {
            Result.failure(Exception("Order not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
} 