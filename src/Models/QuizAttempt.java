package Models;

import java.time.LocalDateTime;

public class QuizAttempt {
    private String attemptId;
    private String enrollmentId;
    private String lessonId;
    private double score;
    private LocalDateTime attemptDate;
    private boolean passed;

    public QuizAttempt(String attemptId, String enrollmentId, String lessonId, double score, LocalDateTime attemptDate, boolean passed) {
        if (attemptId == null || attemptId.trim().isEmpty()) {
            throw new IllegalArgumentException("Attempt ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (lessonId == null || lessonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID cannot be empty.");
        }
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
        this.attemptId = attemptId.trim();
        this.enrollmentId = enrollmentId.trim();
        this.lessonId = lessonId.trim();
        this.score = Math.round(score * 10.0) / 10.0;
        this.attemptDate = attemptDate != null ? attemptDate : LocalDateTime.now();
        this.passed = passed;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        if (attemptId == null || attemptId.trim().isEmpty()) {
            throw new IllegalArgumentException("Attempt ID cannot be empty.");
        }
        this.attemptId = attemptId.trim();
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

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        if (lessonId == null || lessonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID cannot be empty.");
        }
        this.lessonId = lessonId.trim();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
        this.score = Math.round(score * 10.0) / 10.0;
    }

    public LocalDateTime getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(LocalDateTime attemptDate) {
        this.attemptDate = attemptDate;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    @Override
    public String toString() {
        return "QuizAttempt{" +
                "attemptId='" + attemptId + '\'' +
                ", lessonId='" + lessonId + '\'' +
                ", score=" + score + "%" +
                ", passed=" + passed +
                ", date=" + attemptDate +
                '}';
    }
}
