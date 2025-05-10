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
   - Nút tạo sản phẩm mới``

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

## Phân tích giao diện HomeMainActivity

### Cấu trúc tổng thể

Màn hình HomeMainActivity là màn hình chính của ứng dụng, được thiết kế theo mô hình Material Design
với các thành phần hiện đại như ConstraintLayout, NestedScrollView, RecyclerView, ChipGroup và
ViewPager2. Giao diện được thiết kế theo hướng responsive và có khả năng cuộn để hiển thị nhiều nội
dung.

### Các thành phần chính

1. **TopAppBar (MaterialToolbar)**
   - Vị trí: Phía trên cùng của màn hình
   - Chức năng: Hiển thị tên ứng dụng "SHOP" và các nút menu (tìm kiếm, giỏ hàng)
   - Thiết kế: Nền màu tím (colorPrimary), chữ màu trắng
   - Tương tác: Menu dropdown với các lựa chọn

2. **Banner Flash Sale (ViewPager2)**
   - Vị trí: Ngay dưới TopAppBar
   - Chức năng: Hiển thị slider các sản phẩm Flash Sale nổi bật
   - Kích thước: Chiếm toàn bộ chiều rộng màn hình, cao 180dp
   - Tính năng: Auto-scroll tự động chuyển sản phẩm
   - Hiển thị: Hình ảnh sản phẩm, tên, giá gốc (gạch ngang), giá giảm và phần trăm giảm giá

3. **Danh mục sản phẩm (ChipGroup)**
   - Vị trí: Dưới banner Flash Sale
   - Chức năng: Cho phép lọc sản phẩm theo danh mục
   - Thiết kế: Các chip có thể chọn (selectable chips)
   - Tương tác: Single selection - chỉ chọn một danh mục tại một thời điểm
   - Tính năng đặc biệt: Chip "Tất cả" được chọn mặc định, khi chọn danh mục khác sẽ lọc sản phẩm
     tương ứng

4. **Phần Flash Sale**
   - Tiêu đề "Flash Sale" và đồng hồ đếm ngược
   - RecyclerView hiển thị sản phẩm Flash Sale theo chiều ngang
   - Sản phẩm hiển thị với giá gốc, giá giảm và phần trăm giảm giá
   - Tính năng: Cuộn ngang để xem thêm sản phẩm Flash Sale

5. **Phần Featured Products**
   - Tiêu đề "Featured Products"
   - RecyclerView hiển thị sản phẩm nổi bật theo dạng lưới 2 cột
   - Hiển thị hình ảnh, tên sản phẩm, giá và đánh giá
   - Tính năng: Nhấn để xem chi tiết sản phẩm, nút thêm vào giỏ hàng

6. **Phần All Products**
   - Tiêu đề "All Products"
   - RecyclerView hiển thị tất cả sản phẩm
   - Tính năng đặc biệt: Sản phẩm được lọc theo danh mục khi người dùng chọn chip

7. **Bottom Navigation**
   - Vị trí: Dưới cùng màn hình
   - Chức năng: Điều hướng giữa các màn hình chính của ứng dụng
   - Thiết kế: Các icon và text với màu tùy chỉnh

### Cách thức hoạt động

1. **Cuộn và hiển thị**
   - NestedScrollView quản lý việc cuộn toàn bộ nội dung
   - Tất cả RecyclerView có thuộc tính nestedScrollingEnabled để tương tác mượt mà với
     NestedScrollView
   - Thuộc tính clipChildren và clipToPadding được đặt false để cho phép hiệu ứng overflow

2. **Lọc sản phẩm theo danh mục**
   - Khi người dùng chọn một danh mục từ ChipGroup, sự kiện OnCheckedChangeListener được kích hoạt
   - HomeMainActivity gọi phương thức updateProductsByCategory() với tên danh mục
   - Repository truy vấn Firestore để lấy sản phẩm theo danh mục
   - RecyclerView hiển thị sản phẩm được lọc

3. **Hiển thị Banner Flash Sale**
   - ViewPager2 sử dụng FlashSaleBannerAdapter để hiển thị các sản phẩm Flash Sale
   - Auto-scroll được quản lý bởi Handler và Runnable
   - Khi được nhấp vào, chuyển đến trang chi tiết sản phẩm

### Tối ưu hóa hiệu suất

1. **Lazy loading**
   - RecyclerView sử dụng ViewHolder pattern để tái sử dụng view
   - Chỉ tạo và bind dữ liệu cho các item nhìn thấy trên màn hình

2. **Hiệu suất cuộn**
   - NestedScrollView với fillViewport=true để tận dụng toàn bộ không gian
   - RecyclerView có nestedScrollingEnabled=true để cuộn mượt mà

3. **Constraints tối ưu**
   - Sử dụng ConstraintLayout để giảm độ phức tạp của layout
   - Các view được neo vào nhau với constraints để tạo giao diện responsive

### Thiết kế Material

1. **Components Material**
   - MaterialToolbar thay cho ActionBar thông thường
   - ChipGroup cho việc lọc danh mục
   - CardView cho các item sản phẩm

2. **Phối màu**
   - Sử dụng màu chính (colorPrimary) cho TopAppBar và các element nhấn mạnh
   - Bottom navigation có màu tùy chỉnh để phù hợp với theme

### Khả năng tùy biến và mở rộng

1. **Thiết kế module**
   - Mỗi phần (Flash Sale, Featured, All Products) là một module riêng biệt
   - Dễ dàng thêm/bỏ các section khi cần

2. **Flexible layout**
   - ConstraintLayout cho phép điều chỉnh vị trí các thành phần một cách linh hoạt
   - RecyclerView có thể dễ dàng thay đổi LayoutManager để hiển thị theo grid, linear, staggered,
     v.v.

## Kết luận

Dự án e-commerce Android đã được triển khai với đầy đủ tính năng cơ bản của một ứng dụng bán hàng.
Đặc biệt, hệ thống quản trị và phân quyền đã được thiết kế để đảm bảo tính bảo mật cao và dễ dàng mở
rộng. Ứng dụng kết nối với Firebase để lưu trữ và đồng bộ dữ liệu thời gian thực.