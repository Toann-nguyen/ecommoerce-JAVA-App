package repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import models.Order;

public class OrderRepository {
    private static final String TAG = "OrderRepository";
    private final FirebaseFirestore db;
    private static final String COLLECTION_ORDERS = "orders";

    public OrderRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Tạo đơn hàng mới
     *
     * @param order    Đơn hàng mới
     * @param callback Callback khi hoàn thành
     */
    public void createOrder(Order order, OrderCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    // Cập nhật ID cho đơn hàng
                    String orderId = documentReference.getId();
                    documentReference.update("id", orderId)
                            .addOnSuccessListener(aVoid -> {
                                order.setId(orderId);
                                callback.onSuccess(order);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi khi cập nhật ID đơn hàng", e);
                                callback.onError("Đã tạo đơn hàng nhưng không thể cập nhật ID");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tạo đơn hàng", e);
                    callback.onError("Không thể tạo đơn hàng: " + e.getMessage());
                });
    }

    /**
     * Lấy tất cả đơn hàng của người dùng
     *
     * @param userId   ID của người dùng
     * @param callback Callback khi hoàn thành
     */
    public void getOrdersByUserId(String userId, OrdersCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        orders = queryDocumentSnapshots.toObjects(Order.class);
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy đơn hàng", e);
                    callback.onError("Không thể lấy đơn hàng: " + e.getMessage());
                });
    }

    /**
     * Cập nhật trạng thái đơn hàng
     *
     * @param orderId ID đơn hàng
     * @param status  Trạng thái mới
     * @return Task để theo dõi tiến trình
     */
    public Task<Void> updateOrderStatus(String orderId, String status) {
        return db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update("status", status);
    }

    /**
     * Hủy đơn hàng
     *
     * @param orderId ID đơn hàng
     * @return Task để theo dõi tiến trình
     */
    public Task<Void> cancelOrder(String orderId) {
        return updateOrderStatus(orderId, Order.STATUS_CANCELLED);
    }

    /**
     * Callback cho thao tác với một đơn hàng
     */
    public interface OrderCallback {
        void onSuccess(Order order);

        void onError(String errorMessage);
    }

    /**
     * Callback cho thao tác với danh sách đơn hàng
     */
    public interface OrdersCallback {
        void onSuccess(List<Order> orders);

        void onError(String errorMessage);
    }
}