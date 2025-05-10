# E-commerce Android App

## Quá trình phát triển

### 1. Thiết lập dự án ban đầu

- Khởi tạo dự án Android sử dụng Android Studio
- Cấu hình Gradle và thêm các thư viện cần thiết
- Thiết lập Firebase (Authentication, Firestore, Storage)
- Tạo cấu trúc thư mục và các package chính

### 2. Phát triển tính năng người dùng

- Xây dựng màn hình đăng nhập và đăng ký
- Tích hợp Firebase Authentication
- Thiết kế và triển khai màn hình chính (HomeActivity)
- Xây dựng Navigation Drawer cho điều hướng
- Tạo các model dữ liệu cơ bản (User, Product, Category)

### 3. Phát triển tính năng sản phẩm

- Thiết kế và triển khai hiển thị danh sách sản phẩm
- Xây dựng trang chi tiết sản phẩm
- Triển khai tính năng tìm kiếm và lọc sản phẩm
- Phát triển hiển thị sản phẩm theo danh mục
- Hiển thị sản phẩm nổi bật, mới, và Flash Sale

### 4. Phát triển tính năng giỏ hàng và thanh toán

- Thiết kế và triển khai trang giỏ hàng
- Xây dựng luồng xử lý đơn hàng
- Tạo trang thanh toán và quy trình thanh toán
- Lưu trữ lịch sử đơn hàng

### 5. Phát triển tính năng quản trị (Admin)

- Phát triển trang quản lý sản phẩm (CRUD) cho admin
- Triển khai tính năng thêm, sửa, xóa sản phẩm
- Tích hợp upload và quản lý hình ảnh sản phẩm
- Tạo giao diện quản trị tập trung (Admin Panel)
- Lưu trữ và truy xuất dữ liệu sản phẩm từ Firebase

### 6. Phát triển hệ thống phân quyền

- Thiết kế mô hình phân quyền (USER, SELLER, ADMIN)
- Triển khai Permission Manager để quản lý quyền
- Thêm kiểm tra quyền cho các tính năng quản trị
- Hiển thị menu và tính năng dựa trên vai trò người dùng
- Bảo mật các tính năng admin khỏi người dùng không có quyền

## Tính năng chính

### Tính năng người dùng

- Đăng nhập/đăng ký tài khoản
- Xem danh sách sản phẩm, danh mục
- Tìm kiếm và lọc sản phẩm
- Xem chi tiết sản phẩm
- Thêm sản phẩm vào giỏ hàng
- Quản lý giỏ hàng
- Đặt hàng và thanh toán

### Tính năng quản trị (Admin)

- Quản lý sản phẩm (thêm, sửa, xóa, hiển thị)
- Quản lý danh mục
- Xem và xử lý đơn hàng
- Xem báo cáo doanh số
- Quản lý người dùng và phân quyền

### Hệ thống phân quyền

- **USER**: Người dùng thông thường, có quyền mua sắm
- **SELLER**: Người bán hàng, có quyền quản lý sản phẩm và xem báo cáo
- **ADMIN**: Quản trị viên, có đầy đủ quyền quản lý hệ thống

## Cấu trúc dự án

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/ecommerce/
│   │   │   ├── admin/               # Tính năng quản trị
│   │   │   │   ├── AdminPanelActivity.java
│   │   │   │   ├── AdminProductsActivity.java
│   │   │   │   └── EditProductActivity.java
│   │   │   ├── adapters/            # Các adapter
│   │   │   │   ├── ProductAdapter.java
│   │   │   │   ├── AdminProductAdapter.java
│   │   │   │   └── ...
│   │   │   ├── models/              # Các model dữ liệu
│   │   │   │   ├── User.java
│   │   │   │   ├── UserRole.java
│   │   │   │   ├── Product.java
│   │   │   │   └── ...
│   │   │   ├── repository/          # Truy xuất dữ liệu
│   │   │   │   ├── FirebaseRepository.java
│   │   │   │   ├── AdminFirebaseRepository.java
│   │   │   │   └── ...
│   │   │   ├── utils/               # Các tiện ích
│   │   │   │   ├── PermissionManager.java
│   │   │   │   └── ...
│   │   │   ├── MainActivity.java    # Màn hình đăng nhập
│   │   │   ├── HomeMainActivity.java # Màn hình chính
│   │   │   ├── ProductDetailActivity.java
│   │   │   ├── CartActivity.java
│   │   │   └── ...
│   │   └── res/                     # Tài nguyên
│   │       ├── layout/              # Layouts
│   │       ├── drawable/            # Hình ảnh, icons
│   │       ├── values/              # Strings, colors, styles
│   │       └── ...
│   └── ...
└── ...
```

## Công nghệ sử dụng

- **Ngôn ngữ**: Java
- **Database**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **Storage**: Firebase Storage
- **UI Components**: Material Design, RecyclerView, CardView, NavigationView
- **Thư viện hình ảnh**: Glide
- **Kiến trúc**: Repository Pattern, Singleton Pattern

## Quy trình phát triển chi tiết

### CRUD sản phẩm cho Admin

1. **Thiết kế giao diện**:
    - Tạo layout cho danh sách sản phẩm (activity_admin_products.xml)
    - Thiết kế item sản phẩm trong RecyclerView (item_admin_product.xml)
    - Tạo layout thêm/sửa sản phẩm (activity_edit_product.xml)

2. **Tạo các Activity**:
    - AdminProductsActivity: Hiển thị danh sách sản phẩm cho admin
    - EditProductActivity: Form thêm/sửa sản phẩm

3. **Xử lý dữ liệu**:
    - AdminFirebaseRepository: Thực hiện các thao tác CRUD với Firestore
    - AdminProductAdapter: Hiển thị dữ liệu trong RecyclerView

4. **Chức năng chính**:
    - Hiển thị danh sách sản phẩm
    - Thêm sản phẩm mới
    - Chỉnh sửa sản phẩm hiện có
    - Xóa sản phẩm
    - Upload và hiển thị hình ảnh sản phẩm

### Hệ thống phân quyền

1. **Thiết kế mô hình phân quyền**:
    - Tạo enum UserRole định nghĩa các vai trò: USER, SELLER, ADMIN
    - Mở rộng lớp User để hỗ trợ vai trò và quyền

2. **Quản lý quyền**:
    - Tạo lớp PermissionManager để xử lý kiểm tra quyền
    - Định nghĩa hằng số cho các quyền cụ thể (PERMISSION_MANAGE_PRODUCTS, v.v.)
    - Cài đặt methods kiểm tra quyền dựa trên vai trò và permission list

3. **Áp dụng kiểm tra quyền**:
    - Kiểm tra quyền trước khi truy cập các tính năng admin
    - Ẩn/hiện các menu và tính năng dựa vào quyền người dùng
    - Hiển thị thông báo khi người dùng không có quyền truy cập

4. **Giao diện quản trị tập trung**:
    - AdminPanelActivity: Trang quản trị tập trung với các card cho từng tính năng
    - Hiển thị chỉ các tính năng mà người dùng có quyền truy cập

### Tài khoản Admin và Giao diện Riêng

1. **Tạo tài khoản Admin tự động**:
   - Tạo lớp EcommerceApp kế thừa Application để khởi tạo tài khoản admin khi ứng dụng chạy lần đầu
   - Phương thức ensureAdminExists() kiểm tra và tạo tài khoản admin nếu chưa có
   - Thông tin tài khoản: admin@example.com / admin123
   - Tự động gán các quyền cho admin

2. **Giao diện Admin Dashboard**:
   - AdminDashboardActivity: Giao diện quản lý sản phẩm chuyên dụng cho admin
   - Hiển thị sản phẩm dạng bảng với các cột: ID, Hình ảnh, Tên, Danh mục, Giá, Đánh giá
   - Tích hợp tìm kiếm sản phẩm theo tên và danh mục
   - Nút tạo sản phẩm mới

3. **Điều hướng dựa trên vai trò**:
   - Sau đăng nhập, kiểm tra vai trò người dùng và chuyển đến giao diện phù hợp
   - Admin được chuyển đến AdminDashboardActivity
   - Người dùng thông thường được chuyển đến HomeMainActivity
   - Khi đăng xuất, chuyển về màn hình đăng nhập chung

4. **Bảo mật**:
   - Kiểm tra quyền tại mỗi Activity admin
   - Nếu người dùng không có quyền, tự động đăng xuất và chuyển về màn hình đăng nhập

## Thách thức và giải pháp

1. **Bảo mật dữ liệu**:
    - Thách thức: Đảm bảo người dùng chỉ truy cập được dữ liệu họ được phép
    - Giải pháp: Sử dụng hệ thống phân quyền và Firebase Security Rules

2. **Quản lý hình ảnh**:
    - Thách thức: Upload, lưu trữ và hiển thị hình ảnh hiệu quả
    - Giải pháp: Sử dụng Firebase Storage và thư viện Glide

3. **Trải nghiệm người dùng**:
    - Thách thức: Tạo giao diện đẹp và dễ sử dụng
    - Giải pháp: Áp dụng Material Design và UI patterns phổ biến

4. **Hiệu suất**:
    - Thách thức: Tối ưu hiệu suất khi làm việc với dữ liệu lớn
    - Giải pháp: Phân trang, lazy loading và caching

## Kết luận

Dự án e-commerce Android đã được triển khai với đầy đủ tính năng cơ bản của một ứng dụng bán hàng.
Đặc biệt, hệ thống quản trị và phân quyền đã được thiết kế để đảm bảo tính bảo mật cao và dễ dàng mở
rộng. Ứng dụng kết nối với Firebase để lưu trữ và đồng bộ dữ liệu thời gian thực.