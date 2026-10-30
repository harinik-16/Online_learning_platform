package Service;

import Models.Course;
import Models.Enrollment;
import Models.Review;
import Repository.CourseRepository;
import Repository.DataStore;
import Repository.EnrollmentRepository;
import Repository.ReviewRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         EnrollmentRepository enrollmentRepository,
                         CourseRepository courseRepository) {
        if (reviewRepository == null || enrollmentRepository == null || courseRepository == null) {
            throw new IllegalArgumentException("Repositories cannot be null.");
        }
        this.reviewRepository = reviewRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    public Review submitReview(String studentId, String courseId, int rating, String comment) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }

        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }

        // Rule 1: Only students with at least 25% progress in a course may rate it
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(studentId, courseId);
        if (enrollment == null) {
            throw new IllegalStateException("Cannot review: Student is not enrolled in this course.");
        }
        if (enrollment.getProgressPercentage() < 25.0) {
            throw new IllegalStateException("Cannot review: Minimum 25% course progress required to submit a review (Current progress: " +
                    enrollment.getProgressPercentage() + "%).");
        }

        // Rule 2: Rating must be an integer from 1 to 5
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be an integer from 1 to 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty.");
        }

        // Rule 3: Only one review per student per course is allowed
        Review existing = reviewRepository.findByStudentAndCourse(studentId, courseId);
        if (existing != null) {
            throw new IllegalStateException("Duplicate review: You have already reviewed this course. Use edit review instead.");
        }

        String reviewId = DataStore.generateReviewId();
        LocalDateTime now = DataStore.getCurrentTime();
        Review review = new Review(reviewId, studentId, courseId, rating, comment, now);
        reviewRepository.save(review);

        // Rule 6, 7 & 8: Recalculate average rating, maintain total count, flag if >= 20 ratings & avg < 2.5
        recalculateCourseRating(course);

        return review;
    }

    public Review editReview(String studentId, String reviewId, int newRating, String newComment) {
        Review review = reviewRepository.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found: " + reviewId);
        }

        if (!review.getStudentId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalStateException("Unauthorized: You cannot edit another student's review.");
        }

        LocalDateTime now = DataStore.getCurrentTime();

        // Rule 4: The student may edit the rating within 30 days of the original review date
        if (now.isAfter(review.getCreatedDate().plusDays(30))) {
            throw new IllegalStateException("Review edit period has expired: Editing is only permitted within 30 days of the original review date.");
        }

        // Rule 2: Rating must be 1 to 5
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be an integer from 1 to 5.");
        }

        review.setRating(newRating);
        if (newComment != null && !newComment.trim().isEmpty()) {
            review.setComment(newComment.trim());
        }

        // Rule 5: Editing must update the last-updated timestamp
        review.setLastUpdatedDate(now);
        reviewRepository.update(review);

        // Rule 6: Recalculate course average rating
        Course course = courseRepository.findById(review.getCourseId());
        if (course != null) {
            recalculateCourseRating(course);
        }

        return review;
    }

    private void recalculateCourseRating(Course course) {
        ArrayList<Review> reviews = reviewRepository.findByCourse(course.getCourseId());
        int total = reviews.size();
        if (total == 0) {
            course.setAverageRating(0.0);
            course.setTotalRatings(0);
        } else {
            int sum = 0;
            for (int i = 0; i < total; i++) {
                sum += reviews.get(i).getRating();
            }
            double avg = (double) sum / total;
            course.setTotalRatings(total);
            course.setAverageRating(avg);
        }
        courseRepository.update(course);
    }

    public ArrayList<Review> getReviewsForCourse(String courseId) {
        return reviewRepository.findByCourse(courseId);
    }

    public Review getReviewByStudentAndCourse(String studentId, String courseId) {
        return reviewRepository.findByStudentAndCourse(studentId, courseId);
    }

    public ArrayList<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}
