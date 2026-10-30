package Models;

import java.time.LocalDateTime;

public class InstructorEarning {
    private String earningId;
    private String instructorId;
    private String courseId;
    private String enrollmentId;
    private double coursePrice;
    private double instructorShare; // 70%
    private double platformShare;   // 30%
    private LocalDateTime eligibleCreditDate;
    private boolean credited;

    public InstructorEarning(String earningId, String instructorId, String courseId, String enrollmentId,
                             double coursePrice, LocalDateTime enrollmentDate) {
        if (earningId == null || earningId.trim().isEmpty()) {
            throw new IllegalArgumentException("Earning ID cannot be empty.");
        }
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (coursePrice < 0.0) {
            throw new IllegalArgumentException("Course price cannot be negative.");
        }

        this.earningId = earningId.trim();
        this.instructorId = instructorId.trim();
        this.courseId = courseId.trim();
        this.enrollmentId = enrollmentId.trim();
        this.coursePrice = Math.round(coursePrice * 100.0) / 100.0;
        // 70% instructor share, 30% platform share rounded to 2 decimal places
        this.instructorShare = Math.round(this.coursePrice * 0.70 * 100.0) / 100.0;
        this.platformShare = Math.round((this.coursePrice - this.instructorShare) * 100.0) / 100.0;
        // Eligible credit date: 7 days after enrollment
        LocalDateTime baseDate = enrollmentDate != null ? enrollmentDate : LocalDateTime.now();
        this.eligibleCreditDate = baseDate.plusDays(7);
        this.credited = false;
    }

    public String getEarningId() {
        return earningId;
    }

    public void setEarningId(String earningId) {
        if (earningId == null || earningId.trim().isEmpty()) {
            throw new IllegalArgumentException("Earning ID cannot be empty.");
        }
        this.earningId = earningId.trim();
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor ID cannot be empty.");
        }
        this.instructorId = instructorId.trim();
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

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        this.enrollmentId = enrollmentId.trim();
    }

    public double getCoursePrice() {
        return coursePrice;
    }

    public void setCoursePrice(double coursePrice) {
        if (coursePrice < 0.0) {
            throw new IllegalArgumentException("Course price cannot be negative.");
        }
        this.coursePrice = Math.round(coursePrice * 100.0) / 100.0;
    }

    public double getInstructorShare() {
        return instructorShare;
    }

    public void setInstructorShare(double instructorShare) {
        this.instructorShare = Math.round(instructorShare * 100.0) / 100.0;
    }

    public double getPlatformShare() {
        return platformShare;
    }

    public void setPlatformShare(double platformShare) {
        this.platformShare = Math.round(platformShare * 100.0) / 100.0;
    }

    public LocalDateTime getEligibleCreditDate() {
        return eligibleCreditDate;
    }

    public void setEligibleCreditDate(LocalDateTime eligibleCreditDate) {
        this.eligibleCreditDate = eligibleCreditDate;
    }

    public boolean isCredited() {
        return credited;
    }

    public void setCredited(boolean credited) {
        this.credited = credited;
    }

    @Override
    public String toString() {
        return "InstructorEarning{" +
                "id='" + earningId + '\'' +
                ", instructorId='" + instructorId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", price=" + coursePrice +
                ", instructorShare=" + instructorShare + " (70%)" +
                ", platformShare=" + platformShare + " (30%)" +
                ", eligibleDate=" + eligibleCreditDate +
                ", credited=" + credited +
                '}';
    }
}
