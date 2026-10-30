package Models;

import java.time.LocalDateTime;

public class Enrollment {
    public enum EnrollmentStatus {
        PENDING_PAYMENT,
        ACTIVE,
        COMPLETED,
        CANCELLED,
        EXPIRED
    }

    private String enrollmentId;
    private String studentId;
    private String courseId;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;
    private double progressPercentage;
    private LocalDateTime completionDate;
    private double enrolledPrice;
    private int completedLessonsCount;

    public Enrollment(String enrollmentId, String studentId, String courseId, double enrolledPrice, LocalDateTime enrollmentDate) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        if (enrolledPrice < 0.0) {
            throw new IllegalArgumentException("Enrolled price cannot be negative.");
        }

        this.enrollmentId = enrollmentId.trim();
        this.studentId = studentId.trim();
        this.courseId = courseId.trim();
        this.enrolledPrice = Math.round(enrolledPrice * 100.0) / 100.0;
        this.enrollmentDate = enrollmentDate != null ? enrollmentDate : LocalDateTime.now();
        this.progressPercentage = 0.0;
        this.completionDate = null;
        this.completedLessonsCount = 0;

        // Module 2 Rule 4 & 5: Free courses become ACTIVE immediately; paid courses begin with PENDING_PAYMENT
        if (this.enrolledPrice == 0.0) {
            this.status = EnrollmentStatus.ACTIVE;
        } else {
            this.status = EnrollmentStatus.PENDING_PAYMENT;
        }
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        this.enrollmentId = enrollmentId.trim();
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

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Enrollment status cannot be null.");
        }
        this.status = status;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        if (progressPercentage < 0.0) {
            this.progressPercentage = 0.0;
        } else if (progressPercentage > 100.0) {
            this.progressPercentage = 100.0;
        } else {
            this.progressPercentage = Math.round(progressPercentage * 10.0) / 10.0;
        }
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }

    public double getEnrolledPrice() {
        return enrolledPrice;
    }

    public void setEnrolledPrice(double enrolledPrice) {
        if (enrolledPrice < 0.0) {
            throw new IllegalArgumentException("Enrolled price cannot be negative.");
        }
        this.enrolledPrice = Math.round(enrolledPrice * 100.0) / 100.0;
    }

    public int getCompletedLessonsCount() {
        return completedLessonsCount;
    }

    public void setCompletedLessonsCount(int completedLessonsCount) {
        this.completedLessonsCount = completedLessonsCount;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id='" + enrollmentId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", price=" + enrolledPrice +
                ", status=" + status +
                ", progress=" + progressPercentage + "%" +
                ", date=" + enrollmentDate +
                (completionDate != null ? ", completed=" + completionDate : "") +
                '}';
    }
}
