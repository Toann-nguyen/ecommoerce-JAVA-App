package repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Order;
import models.Product;

public class AdminFirebaseRepository {
    private static final String TAG = "AdminFirebaseRepository";
    private final FirebaseFirestore db;
    private final String COLLECTION_PRODUCTS = "products";
    private final String COLLECTION_CATEGORIES = "categories";
    private final String COLLECTION_ORDERS = "orders";

    public AdminFirebaseRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback cho hành động thêm/sửa/xóa
     */
    public interface ActionCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    /**
     * Callback cho danh sách sản phẩm
     */
    public interface ProductsCallback {
        void onCallback(List<Product> products);

        void onError(String errorMessage);
    }

    /**
     * Callback cho chi tiết sản phẩm
     */
    public interface ProductCallback {
        void onCallback(Product product);

        void onError(String errorMessage);
    }

    /**
     * Callback cho danh sách danh mục
     */
    public interface CategoriesCallback {
        void onCallback(List<Category> categories);

        void onError(String errorMessage);
    }

    /**
     * Callback cho danh sách chuỗi (tên danh mục)
     */
    public interface CategoryNamesCallback {
        void onCallback(List<String> categoryNames);

        void onError(String errorMessage);
    }

    /**
     * Callback cho danh sách đơn hàng
     */
    public interface OrdersCallback {
        void onCallback(List<Order> orders);

        void onError(String errorMessage);
    }

    /**
     * Callback cho chi tiết đơn hàng
     */
    public interface OrderCallback {
        void onCallback(Order order);

        void onError(String errorMessage);
    }

    /**
     * Lấy tất cả sản phẩm
     */
    public void getAllProducts(final ProductsCallback callback) {
        db.collection(COLLECTION_PRODUCTS)
                .orderBy("name", Query.Direction.ASCENDING) // Sắp xếp theo tên
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> productList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setId(doc.getId()); // Ensure ID is set
                            productList.add(product);
                        }
                    }
                    callback.onCallback(productList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting products", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    public void getProductById(String productId, final ProductCallback callback) {
        if (productId == null || productId.isEmpty()) {
            callback.onError("Product ID is null or empty");
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            product.setId(documentSnapshot.getId());
                            callback.onCallback(product);
                        } else {
                            callback.onError("Failed to parse product data");
                        }
                    } else {
                        callback.onError("Product not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting product", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Thêm sản phẩm mới
     */
    public void addProduct(Product product, final ActionCallback callback) {
        db.collection(COLLECTION_PRODUCTS)
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Product added with ID: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding product", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    public void updateProduct(Product product, final ActionCallback callback) {
        if (product.getId() == null || product.getId().isEmpty()) {
            callback.onError("Product ID is null or empty");
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .document(product.getId())
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product successfully updated");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating product", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Xóa sản phẩm
     */
    public void deleteProduct(String productId, final ActionCallback callback) {
        if (productId == null || productId.isEmpty()) {
            callback.onError("Product ID is null or empty");
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product successfully deleted");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting product", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Lấy danh sách danh mục
     */
    public void getCategories(final CategoriesCallback callback) {
        db.collection(COLLECTION_CATEGORIES)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Category> categoryList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Category category = doc.toObject(Category.class);
                        if (category != null) {
                            category.setId(doc.getId());
                            categoryList.add(category);
                        }
                    }
                    callback.onCallback(categoryList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting categories", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Lấy tất cả danh mục có trong sản phẩm
     */
    public void getAllUniqueProductCategories(final CategoryNamesCallback callback) {
        db.collection(COLLECTION_PRODUCTS)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> categoryNames = new ArrayList<>();

                    // Extract unique categories from all products
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String category = doc.getString("category");
                        if (category != null && !category.isEmpty() && !categoryNames.contains(category)) {
                            categoryNames.add(category);
                        }
                    }

                    // Sort categories alphabetically
                    java.util.Collections.sort(categoryNames);

                    callback.onCallback(categoryNames);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting product categories", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Đồng bộ danh mục từ sản phẩm vào collection danh mục
     * Tạo danh mục mới từ tất cả danh mục có trong sản phẩm
     */
    public void syncProductCategoriesToCollection(final ActionCallback callback) {
        getAllUniqueProductCategories(new CategoryNamesCallback() {
            @Override
            public void onCallback(List<String> categoryNames) {
                // Đếm số danh mục cần tạo/cập nhật
                final int totalCategories = categoryNames.size();
                if (totalCategories == 0) {
                    callback.onSuccess();
                    return;
                }

                final int[] completedCount = {0};
                final boolean[] hasError = {false};

                // Lấy danh sách danh mục hiện có
                db.collection(COLLECTION_CATEGORIES)
                        .get()
                        .addOnSuccessListener(snapshots -> {
                            // Tạo map các danh mục hiện có theo tên
                            Map<String, String> existingCategoryIds = new HashMap<>();
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Category category = doc.toObject(Category.class);
                                if (category != null) {
                                    existingCategoryIds.put(category.getName(), doc.getId());
                                }
                            }

                            // Tạo hoặc cập nhật từng danh mục
                            for (String categoryName : categoryNames) {
                                // Nếu danh mục đã tồn tại, bỏ qua
                                if (existingCategoryIds.containsKey(categoryName)) {
                                    completedCount[0]++;

                                    // Kiểm tra nếu tất cả đã xử lý xong
                                    if (completedCount[0] >= totalCategories && !hasError[0]) {
                                        callback.onSuccess();
                                    }
                                    continue;
                                }

                                // Tạo danh mục mới
                                Category newCategory = new Category();
                                newCategory.setName(categoryName);
                                newCategory.setIconUrl(""); // Có thể thiết lập icon mặc định sau

                                db.collection(COLLECTION_CATEGORIES)
                                        .add(newCategory)
                                        .addOnSuccessListener(documentReference -> {
                                            completedCount[0]++;

                                            // Kiểm tra nếu tất cả đã xử lý xong
                                            if (completedCount[0] >= totalCategories && !hasError[0]) {
                                                callback.onSuccess();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (!hasError[0]) {
                                                hasError[0] = true;
                                                callback.onError("Lỗi: " + e.getMessage());
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            callback.onError("Lỗi: " + e.getMessage());
                        });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Thêm danh mục mới
     */
    public void addCategory(String categoryName, final ActionCallback callback) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            callback.onError("Tên danh mục không được để trống");
            return;
        }

        // Kiểm tra danh mục đã tồn tại chưa
        db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("name", categoryName.trim())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        callback.onError("Danh mục này đã tồn tại");
                        return;
                    }

                    // Tạo danh mục mới
                    Category category = new Category();
                    category.setName(categoryName.trim());
                    category.setIconUrl("");

                    db.collection(COLLECTION_CATEGORIES)
                            .add(category)
                            .addOnSuccessListener(documentReference -> {
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Lỗi: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError("Lỗi: " + e.getMessage());
                });
    }

    /**
     * Lấy tất cả đơn hàng
     */
    public void getAllOrders(final OrdersCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .orderBy("orderDate", Query.Direction.DESCENDING) // Sắp xếp theo ngày đặt hàng mới nhất
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Order> orderList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setId(doc.getId()); // Ensure ID is set
                            orderList.add(order);
                        }
                    }
                    callback.onCallback(orderList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting orders", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    public void getOrderById(String orderId, final OrderCallback callback) {
        if (orderId == null || orderId.isEmpty()) {
            callback.onError("Order ID is null or empty");
            return;
        }

        db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            order.setId(documentSnapshot.getId());
                            callback.onCallback(order);
                        } else {
                            callback.onError("Failed to parse order data");
                        }
                    } else {
                        callback.onError("Order not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting order", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    public void updateOrderStatus(String orderId, String newStatus, final ActionCallback callback) {
        if (orderId == null || orderId.isEmpty()) {
            callback.onError("Order ID is null or empty");
            return;
        }

        db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order status successfully updated");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating order status", e);
                    callback.onError(e.getMessage());
                });
    }
}