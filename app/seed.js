// seed.js
const admin   = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');
const products       = require('./products.json');

// Khởi tạo app với service account
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seed() {
  const batch = db.batch();

  products.forEach(p => {
    // Lưu vào collection "products", document ID = p.id
    const ref = db.collection('products').doc(p.id);
    batch.set(ref, {
      name:        p.name,
      description: p.description,
      imageUrl:    p.imageUrl,
      category:    p.category,
      price:       p.price,
      rating:      p.rating,
      isFlashSale: p.popular,     // hoặc map đúng field bạn query
      isFeatured:  p.newProduct   // tuỳ theo logic của bạn
    });
  });

  await batch.commit();
  console.log(`✅ Đã seed ${products.length} products lên Firestore!`);
}

seed().catch(err => {
  console.error('❌ Lỗi khi seed dữ liệu:', err);
  process.exit(1);
});
