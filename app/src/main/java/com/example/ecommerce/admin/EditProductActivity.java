package com.example.ecommerce.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.Category;
import models.Product;
import repository.AdminFirebaseRepository;
import utils.PermissionManager;

public class EditProductActivity extends AppCompatActivity {
    private static final String TAG = "EditProductActivity";

    // UI components
    private MaterialToolbar topAppBar;
    private ImageView imageProduct;
    private Button btnSelectImage, btnSaveProduct, btnAddCategory;
    private TextInputEditText edtProductName, edtPrice, edtStock, edtDiscount, edtRating, edtDescription;
    private AutoCompleteTextView edtCategory;
    private CheckBox chkFlashSale, chkFeatured, chkPopular, chkNewProduct;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private AdminFirebaseRepository repository;

    // Data
    private Product currentProduct;
    private String productId;
    private Uri selectedImageUri;
    private List<String> categoryNames = new ArrayList<>();
    private String imageUrl = "";
    private PermissionManager permissionManager;

    // Activity result launcher for selecting images
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(imageProduct);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền truy cập
        permissionManager = PermissionManager.getInstance();
        if (!permissionManager.hasPermission(PermissionManager.PERMISSION_MANAGE_PRODUCTS)) {
            Toast.makeText(this, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_product);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        repository = new AdminFirebaseRepository();

        // Initialize views
        initViews();
        setupToolbar();

        // Load categories for dropdown
        loadCategories();

        // Get product ID from intent if editing
        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId != null) {
            // Editing existing product
            loadProduct(productId);
            topAppBar.setTitle("Chỉnh sửa sản phẩm");
        } else {
            // Adding new product
            topAppBar.setTitle("Thêm sản phẩm mới");
        }

        // Load categories for dropdown
        loadCategories();

        // Setup button listeners
        btnSelectImage.setOnClickListener(v -> getContent.launch("image/*"));
        btnSaveProduct.setOnClickListener(v -> validateAndSaveProduct());
        btnAddCategory.setOnClickListener(v -> addNewCategory());

        // Đồng bộ categories từ products vào bảng categories
        syncCategories();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        imageProduct = findViewById(R.id.imageProduct);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        edtProductName = findViewById(R.id.edtProductName);
        edtPrice = findViewById(R.id.edtPrice);
        edtCategory = findViewById(R.id.edtCategory);
        edtStock = findViewById(R.id.edtStock);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtRating = findViewById(R.id.edtRating);
        edtDescription = findViewById(R.id.edtDescription);
        chkFlashSale = findViewById(R.id.chkFlashSale);
        chkFeatured = findViewById(R.id.chkFeatured);
        chkPopular = findViewById(R.id.chkPopular);
        chkNewProduct = findViewById(R.id.chkNewProduct);
        progressBar = findViewById(R.id.progressBar);
        btnAddCategory = findViewById(R.id.btnAddCategory);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadProduct(String productId) {
        showLoading(true);

        repository.getProductById(productId, new AdminFirebaseRepository.ProductCallback() {
            @Override
            public void onCallback(Product product) {
                if (product != null) {
                    currentProduct = product;
                    // Wait for categories to load before displaying product details
                    if (categoryNames.size() > 0) {
                        displayProductDetails();
                    } else {
                        loadCategories();
                    }
                }
                showLoading(false);
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(EditProductActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product: " + errorMessage);
            }
        });
    }

    private void displayProductDetails() {
        edtProductName.setText(currentProduct.getName());
        edtPrice.setText(String.valueOf(currentProduct.getPrice()));
        edtCategory.setText(currentProduct.getCategory());
        edtStock.setText(String.valueOf(currentProduct.getStock()));
        edtDiscount.setText(String.valueOf(currentProduct.getDiscount()));
        edtRating.setText(String.valueOf(currentProduct.getRating()));
        edtDescription.setText(currentProduct.getDescription());

        chkFlashSale.setChecked(currentProduct.isFlashSale());
        chkFeatured.setChecked(currentProduct.isFeatured());
        chkPopular.setChecked(currentProduct.isPopular());
        chkNewProduct.setChecked(currentProduct.isNewProduct());

        // Load image
        imageUrl = currentProduct.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imageProduct);
        }
    }

    private void loadCategories() {
        repository.getCategories(new AdminFirebaseRepository.CategoriesCallback() {
            @Override
            public void onCallback(List<Category> categoryList) {
                // Extract category names for the dropdown
                categoryNames.clear();
                for (Category category : categoryList) {
                    categoryNames.add(category.getName());
                }

                // If we're editing a product, make sure the current category is in the list
                if (currentProduct != null && currentProduct.getCategory() != null
                        && !currentProduct.getCategory().isEmpty()
                        && !categoryNames.contains(currentProduct.getCategory())) {
                    categoryNames.add(currentProduct.getCategory());
                }

                updateCategoryAdapter();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EditProductActivity.this,
                        "Lỗi tải danh mục: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading categories: " + errorMessage);

                // Fallback: sử dụng danh mục từ sản phẩm nếu không tải được từ categories
                repository.getAllUniqueProductCategories(new AdminFirebaseRepository.CategoryNamesCallback() {
                    @Override
                    public void onCallback(List<String> uniqueCategoryNames) {
                        categoryNames.clear();
                        categoryNames.addAll(uniqueCategoryNames);
                        updateCategoryAdapter();
                    }

                    @Override
                    public void onError(String errMsg) {
                        // Nếu cả hai cách đều thất bại thì tải từ bộ nhớ cục bộ
                        loadCategoriesFromLocalStorage();
                    }
                });
            }
        });
    }

    /**
     * Tải danh mục từ bộ nhớ cục bộ như một giải pháp dự phòng
     */
    private void loadCategoriesFromLocalStorage() {
        categoryNames.clear();

        // Đọc từ SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("product_cache", MODE_PRIVATE);
        String categories = prefs.getString("known_categories", "");

        if (!categories.isEmpty()) {
            String[] categoryArray = categories.split(",");
            for (String category : categoryArray) {
                if (!category.trim().isEmpty() && !categoryNames.contains(category.trim())) {
                    categoryNames.add(category.trim());
                }
            }
        }

        updateCategoryAdapter();
    }

    /**
     * Cập nhật adapter cho dropdown danh mục
     */
    private void updateCategoryAdapter() {
        // Thêm category hiện tại của sản phẩm vào danh sách nếu chưa có
        if (currentProduct != null && currentProduct.getCategory() != null
                && !currentProduct.getCategory().isEmpty()
                && !categoryNames.contains(currentProduct.getCategory())) {
            categoryNames.add(currentProduct.getCategory());
        }

        // Cập nhật adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                EditProductActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                categoryNames);

        edtCategory.setAdapter(adapter);

        // Cấu hình AutoCompleteTextView để hiển thị danh mục và cho phép chỉnh sửa
        edtCategory.setThreshold(0);
        edtCategory.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        // Luôn hiển thị dropdown khi click
        edtCategory.setOnClickListener(v -> {
            edtCategory.showDropDown();
        });

        // Hiển thị chi tiết sản phẩm sau khi tải danh mục
        if (currentProduct != null) {
            displayProductDetails();
        }
    }

    private void validateAndSaveProduct() {
        // Validate input fields
        if (edtProductName.getText() == null || edtProductName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtPrice.getText() == null || edtPrice.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtCategory.getText() == null || edtCategory.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        showLoading(true);

        // Handle image upload first if a new image was selected
        if (selectedImageUri != null) {
            uploadImageAndSaveProduct();
        } else {
            // No new image, save product with existing image URL
            saveProduct(imageUrl);
        }
    }

    private void uploadImageAndSaveProduct() {
        // Create a unique filename
        String filename = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("product_images/" + filename);

        // Upload the image
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Save product with the image URL
                                saveProduct(uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(EditProductActivity.this,
                                        "Lỗi tải hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error getting download URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(EditProductActivity.this,
                            "Lỗi tải hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error uploading image: " + e.getMessage());
                });
    }

    private void saveProduct(String imageUrl) {
        // Create a product object from input fields
        Product product = new Product();

        // Set ID if editing existing product
        if (productId != null) {
            product.setId(productId);
        }

        // Set basic info
        product.setName(edtProductName.getText().toString().trim());
        product.setPrice(Double.parseDouble(edtPrice.getText().toString().trim()));
        product.setCategory(edtCategory.getText().toString().trim());
        product.setImageUrl(imageUrl);
        product.setDescription(edtDescription.getText() != null ?
                edtDescription.getText().toString().trim() : "");

        // Set numeric fields
        try {
            product.setStock(Integer.parseInt(edtStock.getText().toString().trim()));
        } catch (NumberFormatException e) {
            product.setStock(0);
        }

        try {
            product.setDiscount(Integer.parseInt(edtDiscount.getText().toString().trim()));
        } catch (NumberFormatException e) {
            product.setDiscount(0);
        }

        try {
            double rating = Double.parseDouble(edtRating.getText().toString().trim());
            // Validate rating between 0 and 5
            if (rating < 0) rating = 0;
            if (rating > 5) rating = 5;
            product.setRating(rating);
        } catch (NumberFormatException e) {
            product.setRating(0);
        }

        // Set boolean fields
        product.setFlashSale(chkFlashSale.isChecked());
        product.setFeatured(chkFeatured.isChecked());
        product.setPopular(chkPopular.isChecked());
        product.setNewProduct(chkNewProduct.isChecked());

        // Save to Firestore
        if (productId != null) {
            // Update existing product
            repository.updateProduct(product, new AdminFirebaseRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(EditProductActivity.this,
                            "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();

                    // Đồng thời lưu cục bộ để dự phòng
                    saveProductToLocalStorage(product);

                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(EditProductActivity.this,
                            "Lỗi cập nhật: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating product: " + errorMessage);
                }
            });
        } else {
            // Add new product
            repository.addProduct(product, new AdminFirebaseRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(EditProductActivity.this,
                            "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();

                    // Đồng thời lưu cục bộ để dự phòng
                    saveProductToLocalStorage(product);

                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(EditProductActivity.this,
                            "Lỗi khi thêm: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding product: " + errorMessage);
                }
            });
        }
    }

    /**
     * Lưu sản phẩm vào bộ nhớ cục bộ
     */
    private void saveProductToLocalStorage(Product product) {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("product_cache", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();

            // Lưu thông tin sản phẩm
            String productId = product.getId() != null ? product.getId() : "new_product_" + System.currentTimeMillis();
            editor.putString("product_" + productId + "_name", product.getName());
            editor.putString("product_" + productId + "_category", product.getCategory());
            editor.putFloat("product_" + productId + "_price", (float) product.getPrice());

            // Lưu các thông tin khác nếu cần

            editor.apply();

            // Lưu danh mục mới vào danh sách các danh mục đã biết
            String categories = prefs.getString("known_categories", "");
            if (!categories.contains(product.getCategory())) {
                categories += product.getCategory() + ",";
                editor.putString("known_categories", categories);
                editor.apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving product locally: " + e.getMessage());
        }
    }

    private void addNewCategory() {
        String categoryName = edtCategory.getText().toString().trim();

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem danh mục đã tồn tại chưa
        if (categoryNames.contains(categoryName)) {
            Toast.makeText(this, "Danh mục này đã tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        showLoading(true);

        // Thêm danh mục mới vào Firebase
        repository.addCategory(categoryName, new AdminFirebaseRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                // Cập nhật danh sách danh mục hiện tại
                categoryNames.add(categoryName);

                // Cập nhật adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        EditProductActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames);

                edtCategory.setAdapter(adapter);
                edtCategory.setText(categoryName);

                // Đồng thời lưu cục bộ để dự phòng
                saveCategoryToLocalStorage(categoryName);

                showLoading(false);
                Toast.makeText(EditProductActivity.this,
                        "Đã thêm danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(EditProductActivity.this,
                        "Lỗi thêm danh mục: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error adding category: " + errorMessage);
            }
        });
    }

    /**
     * Lưu danh mục vào bộ nhớ cục bộ
     */
    private void saveCategoryToLocalStorage(String categoryName) {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("product_cache", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();

            // Lưu danh mục vào danh sách các danh mục đã biết
            String categories = prefs.getString("known_categories", "");
            if (!categories.contains(categoryName)) {
                categories += categoryName + ",";
                editor.putString("known_categories", categories);
                editor.apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving category locally: " + e.getMessage());
        }
    }

    private void syncCategories() {
        showLoading(true);

        repository.syncProductCategoriesToCollection(new AdminFirebaseRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                // Sau khi đồng bộ xong, tải danh mục
                loadCategories();
                showLoading(false);
                Toast.makeText(EditProductActivity.this,
                        "Đồng bộ danh mục thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(EditProductActivity.this,
                        "Lỗi đồng bộ danh mục: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error syncing categories: " + errorMessage);

                // Vẫn tải danh sách danh mục từ sản phẩm
                repository.getAllUniqueProductCategories(new AdminFirebaseRepository.CategoryNamesCallback() {
                    @Override
                    public void onCallback(List<String> uniqueCategoryNames) {
                        categoryNames.clear();
                        categoryNames.addAll(uniqueCategoryNames);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                EditProductActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                categoryNames);

                        edtCategory.setAdapter(adapter);

                        if (currentProduct != null) {
                            displayProductDetails();
                        }
                    }

                    @Override
                    public void onError(String err) {
                        // Nếu không thể tải danh mục từ sản phẩm
                        if (currentProduct != null) {
                            displayProductDetails();
                        }
                    }
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveProduct.setEnabled(!isLoading);
        btnSelectImage.setEnabled(!isLoading);
    }
}