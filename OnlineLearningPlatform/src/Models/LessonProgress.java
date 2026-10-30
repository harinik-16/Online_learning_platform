package Models;

import java.time.LocalDateTime;

public class LessonProgress {
    private String progressId;
    private String enrollmentId;
    private String lessonId;
    private boolean completed;
    private double videoWatchPercentage;
    private double quizScore;
    private LocalDateTime completionDate;

    public LessonProgress(String progressId, String enrollmentId, String lessonId) {
        if (progressId == null || progressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Progress ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (lessonId == null || lessonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID cannot be empty.");
        }
        this.progressId = progressId.trim();
        this.enrollmentId = enrollmentId.trim();
        this.lessonId = lessonId.trim();
        this.completed = false;
        this.videoWatchPercentage = 0.0;
        this.quizScore = 0.0;
        this.completionDate = null;
    }

    public String getProgressId() {
        return progressId;
    }

    public void setProgressId(String progressId) {
        if (progressId == null || progressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Progress ID cannot be empty.");
        }
        this.progressId = progressId.trim();
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public double getVideoWatchPercentage() {
        return videoWatchPercentage;
    }

    public void setVideoWatchPercentage(double videoWatchPercentage) {
        if (videoWatchPercentage < 0.0) {
            this.videoWatchPercentage = 0.0;
        } else if (videoWatchPercentage > 100.0) {
            this.videoWatchPercentage = 100.0;
        } else {
            this.videoWatchPercentage = Math.round(videoWatchPercentage * 10.0) / 10.0;
        }
    }

    public double getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(double quizScore) {
        if (quizScore < 0.0) {
            this.quizScore = 0.0;
        } else if (quizScore > 100.0) {
            this.quizScore = 100.0;
        } else {
            this.quizScore = Math.round(quizScore * 10.0) / 10.0;
        }
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }

    @Override
    public String toString() {
        return "LessonProgress{" +
                "lessonId='" + lessonId + '\'' +
                ", completed=" + completed +
                ", videoWatch=" + videoWatchPercentage + "%" +
                ", quizScore=" + quizScore + "%" +
                (completionDate != null ? ", completedAt=" + completionDate : "") +
                '}';
    }
}
