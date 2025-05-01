FULL stack mobile app Develop ECOMMERCE 

# E-commerce Java App

Đây là một ứng dụng thương mại điện tử Android được phát triển bằng Java, sử dụng Firebase Firestore làm cơ sở dữ liệu backend.

## Tính năng

- Hiển thị danh mục, sản phẩm flash sale và sản phẩm nổi bật trên màn hình chính.
- Banner slideshow tự động chuyển đổi sau mỗi 3 giây.
- Hỗ trợ nhấn vào sản phẩm để xem chi tiết (chưa triển khai).
- Hỗ trợ nhấn vào danh mục để xem sản phẩm theo danh mục (chưa triển khai).
- Nút thêm vào giỏ hàng với thông báo cơ bản.

## Kiến trúc

- **Lớp dữ liệu**: `FirebaseRepository` xử lý việc lấy dữ liệu từ Firestore.
- **Lớp giao diện**: `HomeMainActivity` thiết lập UI và xử lý tương tác người dùng.
- **Adapters**: `CategoryAdapter` và `ProductAdapter` để hiển thị danh sách trong RecyclerView.

## Quá trình phát triển

1. **Thiết lập Firebase**:
    - Tạo dự án Firebase trên Firebase Console.
    - Thêm `google-services.json` vào thư mục `app/` và cấu hình Gradle để tích hợp Firebase.

2. **Thiết kế mô hình dữ liệu**:
    - Tạo các lớp `Product`, `Category`, và `Banner` với các thuộc tính như `name`, `price`, `imageUrl`, v.v.
    - Lưu trữ dữ liệu trong Firestore với các collection: `products`, `categories`, `banners`.

3. **Xây dựng lớp Repository**:
    - Tạo `FirebaseRepository.java` để lấy dữ liệu từ Firestore (sản phẩm flash sale, sản phẩm nổi bật, danh mục, banner).
    - Sử dụng callback để xử lý dữ liệu bất đồng bộ.

4. **Thiết kế giao diện**:
    - Tạo `activity_home_main.xml` với các RecyclerView cho danh mục, sản phẩm, và ImageView cho banner.
    - Tạo `item_product.xml` cho layout của từng sản phẩm.

5. **Triển khai Adapters**:
    - Viết `ProductAdapter.java` để hiển thị danh sách sản phẩm với hình ảnh, tên, giá và nút "Thêm vào giỏ".
    - Viết `CategoryAdapter.java` để hiển thị danh sách danh mục (tương tự ProductAdapter nhưng cho danh mục).

6. **Xây dựng HomeMainActivity**:
    - Khởi tạo các RecyclerView và ImageView trong `initViews()`.
    - Thiết lập adapter và layout manager trong `setupRecyclerViews()`.
    - Tải dữ liệu từ Firestore trong `loadData()`.

7. **Triển khai banner tự động**:
    - Sử dụng `postDelayed` để chuyển đổi banner sau mỗi 3 giây.

8. **Xử lý tương tác**:
    - Thêm sự kiện nhấn cho sản phẩm, danh mục và nú "“Thêm vào giỏ hàng” (hiện chỉ hiển thị Toast).

## Cài đặt

1. Clone repository:   git clone https://github.com/Toann-nguyen/ecommoerce-JAVA-App.git

2. Mở dự án trong Android Studio.
3. Thêm tệp `google-services.json` từ Firebase vào `app/`.
4. Build và chạy ứng dụng trên emulator hoặc thiết bị thật.

## Sử dụng

- Màn hình chính hiển thị danh mục, sản phẩm flash sale, sản phẩm nổi bật và banner.
- Nhấn vào sản phẩm hoặc danh mục để xem chi tiết (cần triển khai thêm).
- Nhấn nút "Thêm" để thêm sản phẩm vào giỏ hàng (hiện chỉ hiển thị thông báo).

## Cấu trúc code

- `FirebaseRepository.java`: Xử lý dữ liệu từ Firestore.
- `ProductAdapter.java`: Adapter cho sản phẩm.
- `CategoryAdapter.java`: Adapter cho danh mục, hiển thị biểu tượng và tên danh mục.
- `HomeMainActivity.java`: Activity chính của màn hình chính.
- `activity_home_main.xml`: Layout màn hình chính.
- `item_product.xml`: Layout cho mỗi sản phẩm.
- `item_category.xml`: Layout cho mỗi danh mục.

## Quá trình phát triển

5. **Triển khai Adapters**:
    - Viết `ProductAdapter.java` để hiển thị danh sách sản phẩm với hình ảnh, tên, giá và nút "Thêm vào giỏ".
    - Viết `CategoryAdapter.java` để hiển thị danh sách danh mục với biểu tượng và tên, hỗ trợ sự kiện nhấn để chuyển đến danh sách sản phẩm theo danh mục.

## Lưu ý

- Đảm bảo tất cả các lớp nằm trong package `com.example.ecommerce` để tránh lỗi import.
- Triển khai thêm màn hình chi tiết sản phẩm và danh mục nếu cần.
- Cải thiện chức năng giỏ hàng với hệ thống lưu trữ thực tế (ví dụ: Firestore hoặc Room).

