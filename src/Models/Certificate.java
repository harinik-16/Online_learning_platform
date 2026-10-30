package Models;

import java.time.LocalDateTime;

public class Certificate {
    private String certificateId;
    private String enrollmentId;
    private String studentId;
    private String courseId;
    private LocalDateTime issueDate;

    public Certificate(String certificateId, String enrollmentId, String studentId, String courseId, LocalDateTime issueDate) {
        if (certificateId == null || certificateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        this.certificateId = certificateId.trim();
        this.enrollmentId = enrollmentId.trim();
        this.studentId = studentId.trim();
        this.courseId = courseId.trim();
        this.issueDate = issueDate != null ? issueDate : LocalDateTime.now();
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        if (certificateId == null || certificateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate ID cannot be empty.");
        }
        this.certificateId = certificateId.trim();
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

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "certificateId='" + certificateId + '\'' +
                ", enrollmentId='" + enrollmentId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", issueDate=" + issueDate +
                '}';
    }
}
