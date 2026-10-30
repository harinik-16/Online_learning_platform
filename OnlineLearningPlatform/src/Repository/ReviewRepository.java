package Repository;

import Models.Review;
import java.util.ArrayList;

public class ReviewRepository {

    public Review save(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null.");
        }
        if (review.getReviewId() == null || review.getReviewId().trim().isEmpty()) {
            review.setReviewId(DataStore.generateReviewId());
        }
        DataStore.reviews.add(review);
        return review;
    }

    public Review findById(String reviewId) {
        if (reviewId == null) return null;
        for (int i = 0; i < DataStore.reviews.size(); i++) {
            Review r = DataStore.reviews.get(i);
            if (r.getReviewId().equalsIgnoreCase(reviewId.trim())) {
                return r;
            }
        }
        return null;
    }

    public ArrayList<Review> findByCourse(String courseId) {
        ArrayList<Review> result = new ArrayList<>();
        if (courseId == null) return result;
        for (int i = 0; i < DataStore.reviews.size(); i++) {
            Review r = DataStore.reviews.get(i);
            if (r.getCourseId().equalsIgnoreCase(courseId.trim())) {
                result.add(r);
            }
        }
        return result;
    }

    public Review findByStudentAndCourse(String studentId, String courseId) {
        if (studentId == null || courseId == null) return null;
        for (int i = 0; i < DataStore.reviews.size(); i++) {
            Review r = DataStore.reviews.get(i);
            if (r.getStudentId().equalsIgnoreCase(studentId.trim()) &&
                r.getCourseId().equalsIgnoreCase(courseId.trim())) {
                return r;
            }
        }
        return null;
    }

    public ArrayList<Review> findAll() {
        return new ArrayList<>(DataStore.reviews);
    }

    public boolean update(Review review) {
        if (review == null || review.getReviewId() == null) return false;
        for (int i = 0; i < DataStore.reviews.size(); i++) {
            if (DataStore.reviews.get(i).getReviewId().equals(review.getReviewId())) {
                DataStore.reviews.set(i, review);
                return true;
            }
        }
        return false;
    }
}
