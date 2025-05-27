package repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import models.Review;

public class ReviewRepository {
    private final FirebaseFirestore db;

    public ReviewRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Interface for review callback
    public interface ReviewCallback {
        void onSuccess(Review review);

        void onError(String errorMessage);
    }

    // Interface for reviews list callback
    public interface ReviewsCallback {
        void onSuccess(List<Review> reviews);

        void onError(String errorMessage);
    }

    // Create a new review
    public void createReview(Review review, ReviewCallback callback) {
        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId());
                    documentReference.update("id", documentReference.getId())
                            .addOnSuccessListener(aVoid -> callback.onSuccess(review))
                            .addOnFailureListener(e -> callback.onError("Failed to update review ID: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Failed to create review: " + e.getMessage()));
    }

    // Get all reviews for a product
    public void getReviewsByProductId(String productId, ReviewsCallback callback) {
        db.collection("reviews")
                .whereEqualTo("productId", productId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Review review = queryDocumentSnapshots.getDocuments().get(i).toObject(Review.class);
                        reviews.add(review);
                    }
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> callback.onError("Failed to get reviews: " + e.getMessage()));
    }

    // Get all recent reviews
    public void getRecentReviews(ReviewsCallback callback) {
        db.collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Review review = queryDocumentSnapshots.getDocuments().get(i).toObject(Review.class);
                        reviews.add(review);
                    }
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> callback.onError("Failed to get recent reviews: " + e.getMessage()));
    }

    // Get all reviews by user ID
    public void getReviewsByUserId(String userId, ReviewsCallback callback) {
        db.collection("reviews")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Review review = queryDocumentSnapshots.getDocuments().get(i).toObject(Review.class);
                        reviews.add(review);
                    }
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> callback.onError("Failed to get user reviews: " + e.getMessage()));
    }

    // Delete a review
    public void deleteReview(String reviewId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection("reviews")
                .document(reviewId)
                .delete()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}