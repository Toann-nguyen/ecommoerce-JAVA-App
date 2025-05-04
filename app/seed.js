const path = require('path');
const admin = require('firebase-admin');
const fs = require('fs');

// Đường dẫn tương đối tới file serviceAccountKey.json và products.json
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
const productsPath = path.join(__dirname, 'products.json');

try {
  // Debug: Kiểm tra file serviceAccountKey.json
  console.log('Loading serviceAccountKey from:', serviceAccountPath);

  // Kiểm tra xem file có tồn tại không
  if (!fs.existsSync(serviceAccountPath)) {
    throw new Error(`serviceAccountKey.json không tồn tại ở đường dẫn: ${serviceAccountPath}`);
  }

  // Đọc file serviceAccountKey.json thay vì require để tránh cache
  const serviceAccountContent = fs.readFileSync(serviceAccountPath, 'utf8');
  const serviceAccount = JSON.parse(serviceAccountContent);
  console.log('Service account loaded successfully:', serviceAccount.project_id);

  // Đảm bảo dùng đúng databaseURL của Firebase project
  const databaseURL = `https://${serviceAccount.project_id}.firebaseio.com`;
  console.log('Using database URL:', databaseURL);

  // Khởi tạo Firebase với các thông số chi tiết
  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      databaseURL: databaseURL
    });
    console.log('Firebase app initialized successfully');
  }

  // Tạo instance Firestore
  const db = admin.firestore();
  console.log('Firestore instance created');

  // Load products data
  console.log('Loading products from:', productsPath);

  // Kiểm tra xem file có tồn tại không
  if (!fs.existsSync(productsPath)) {
    throw new Error(`products.json không tồn tại ở đường dẫn: ${productsPath}`);
  }

  const products = JSON.parse(fs.readFileSync(productsPath, 'utf8'));
  console.log('Products loaded successfully:', products.length, 'items');

  async function seed() {
    try {
      console.log('Bắt đầu seeding dữ liệu...');

      // Kiểm tra quyền truy cập
      try {
        await db.collection('products').limit(1).get();
        console.log('Firestore access verified successfully');
      } catch (accessError) {
        console.error('Failed to access Firestore:', accessError.message);
        console.log('Kiểm tra xem service account có quyền truy cập Firestore không');
        throw accessError;
      }

      // Sử dụng Promise.all với các operations riêng lẻ thay vì batch
      const promises = products.map(product => {
        return db.collection('products').doc(product.id).set(product)
          .then(() => console.log(`Added product: ${product.id}`))
          .catch(err => console.error(`Error adding product ${product.id}:`, err));
      });

      await Promise.all(promises);
      console.log(`✅ Seeded ${products.length} products to Firestore.`);
    } catch (error) {
      console.error('❌ Error during seeding:', error);
      throw error;
    }
  }

  // Chạy hàm seed
  seed().catch(err => {
    console.error('❌ Error seeding products:', err);
    process.exit(1);
  });

} catch (error) {
  console.error('❌ Error initializing script:', error);
  process.exit(1);
}