package repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import models.Banner;
import models.Category;
import models.Product;

public class FirebaseRepository {
    private final FirebaseFirestore db;

    public FirebaseRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Lấy danh sách sản phẩm Flash Sale
    public void getFlashSaleProducts(ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("isFlashSale", true)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        products.add(product);
                    }
                    callback.onCallback(products);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lấy danh sách sản phẩm nổi bật
    public void getFeaturedProducts(ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("isFeatured", true)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        products.add(product);
                    }
                    callback.onCallback(products);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lấy danh sách danh mục
    public void getCategories(CategoriesCallback callback) {
        db.collection("categories")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categories = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        categories.add(category);
                    }
                    callback.onCallback(categories);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lấy banner theo thứ tự ưu tiên
    public void getBanners(BannersCallback callback) {
        db.collection("banners")
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Banner> banners = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Banner banner = document.toObject(Banner.class);
                        banners.add(banner);
                    }
                    callback.onCallback(banners);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Interfaces callback
    public interface ProductsCallback {
        void onCallback(List<Product> products);
        void onError(String errorMessage);
    }

    public interface CategoriesCallback {
        void onCallback(List<Category> categories);
        void onError(String errorMessage);
    }

    public interface BannersCallback {
        void onCallback(List<Banner> banners);
        void onError(String errorMessage);
    }
}
