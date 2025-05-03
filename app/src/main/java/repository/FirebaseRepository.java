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
     * Callback for loading ProductModel lists
     */
    public interface OnProductsLoadListener {
        void onProductsLoaded(List<ProductModel> products);
        void onError(String errorMessage);
    }

    /**
     * Retrieve all products
     * Matches Firebase documentation: uses addOnSuccessListener and addOnFailureListener
     */
    public void getAllProducts(final OnProductsLoadListener listener) {
        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<ProductModel> productList = snapshots.toObjects(ProductModel.class);
                        listener.onProductsLoaded(productList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Retrieve new products (field newProduct == true)
     */
    public void getNewProducts(final OnProductsLoadListener listener) {
        db.collection("products")
                .whereEqualTo("newProduct", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<ProductModel> productList = snapshots.toObjects(ProductModel.class);
                        listener.onProductsLoaded(productList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Retrieve popular products (field popular == true)
     */
    public void getPopularProducts(final OnProductsLoadListener listener) {
        db.collection("products")
                .whereEqualTo("popular", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<ProductModel> productList = snapshots.toObjects(ProductModel.class);
                        listener.onProductsLoaded(productList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Callback for domain Product lists
     */
    public interface ProductsCallback {
        void onCallback(List<Product> products);
        void onError(String errorMessage);
    }

    /**
     * Retrieve flash sale products
     */
    public void getFlashSaleProducts(final ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("isFlashSale", true)
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Product> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            list.add(doc.toObject(Product.class));
                        }
                        callback.onCallback(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Retrieve featured products
     */
    public void getFeaturedProducts(final ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("isFeatured", true)
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Product> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            list.add(doc.toObject(Product.class));
                        }
                        callback.onCallback(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Callback for categories
     */
    public interface CategoriesCallback {
        void onCallback(List<Category> categories);
        void onError(String errorMessage);
    }

    /**
     * Retrieve categories
     */
    public void getCategories(final CategoriesCallback callback) {
        db.collection("categories")
                .orderBy("name")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Category> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            list.add(doc.toObject(Category.class));
                        }
                        callback.onCallback(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Callback for banners
     */
    public interface BannersCallback {
        void onCallback(List<Banner> banners);
        void onError(String errorMessage);
    }

    /**
     * Retrieve banners with priority ordering
     */
    public void getBanners(final BannersCallback callback) {
        db.collection("banners")
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Banner> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            list.add(doc.toObject(Banner.class));
                        }
                        callback.onCallback(list);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }
}
