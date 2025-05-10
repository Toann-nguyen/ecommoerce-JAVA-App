package repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import models.Banner;
import models.Category;
import models.Product;
import models.ProductModel;

public class FirebaseRepository {
    private final FirebaseFirestore db;

    public FirebaseRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback chung cho danh sách sản phẩm
     */
    public interface ProductsCallback {
        void onCallback(List<Product> products);
        void onError(String errorMessage);
    }

    /**
     * Lấy tất cả sản phẩm
     */
    public void getAllProducts(final ProductsCallback callback) {
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        list.add(doc.toObject(Product.class));
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Lấy sản phẩm Flash Sale
     */
    public void getFlashSaleProducts(final ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("isFlashSale", true)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        list.add(doc.toObject(Product.class));
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Lấy sản phẩm nổi bật
     */
    public void getFeaturedProducts(final ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("isFeatured", true)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        list.add(doc.toObject(Product.class));
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Lấy các sản phẩm theo danh mục
     */
    public void getProductsByCategory(String category, final ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            list.add(product);
                        }
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Callback cho danh mục
     */
    public interface CategoriesCallback {
        void onCallback(List<Category> categories);
        void onError(String errorMessage);
    }

    /**
     * Lấy danh sách danh mục
     */
    public void getCategories(final CategoriesCallback callback) {
        db.collection("categories")
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Category> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        list.add(doc.toObject(Category.class));
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Callback cho banners
     */
    public interface BannersCallback {
        void onCallback(List<Banner> banners);
        void onError(String errorMessage);
    }

    /**
     * Lấy danh sách banner
     */
    public void getBanners(final BannersCallback callback) {
        db.collection("banners")
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Banner> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        list.add(doc.toObject(Banner.class));
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Callback cho sản phẩm
     */
    public interface ProductCallback {
        void onCallback(Product product);

        void onError(String errorMessage);
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    public void getProductById(String productId, final ProductCallback callback) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        callback.onCallback(product);
                    } else {
                        callback.onError("Không tìm thấy sản phẩm");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}