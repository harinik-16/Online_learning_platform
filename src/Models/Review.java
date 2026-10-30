package Models;

import java.time.LocalDateTime;

public class Review {
    private String reviewId;
    private String studentId;
    private String courseId;
    private int rating;
    private String comment;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public Review(String reviewId, String studentId, String courseId, int rating, String comment, LocalDateTime createdDate) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("Review ID cannot be empty.");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be an integer between 1 and 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty.");
        }

        this.reviewId = reviewId.trim();
        this.studentId = studentId.trim();
        this.courseId = courseId.trim();
        this.rating = rating;
        this.comment = comment.trim();
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
        this.lastUpdatedDate = this.createdDate;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("Review ID cannot be empty.");
        }
        this.reviewId = reviewId.trim();
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        this.studentId = studentId.trim();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        this.courseId = courseId.trim();
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be an integer between 1 and 5.");
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty.");
        }
        this.comment = comment.trim();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + reviewId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", rating=" + rating + "★" +
                ", comment='" + comment + '\'' +
                ", created=" + createdDate +
                (lastUpdatedDate != null && !lastUpdatedDate.equals(createdDate) ? ", updated=" + lastUpdatedDate : "") +
                '}';
    }
}
