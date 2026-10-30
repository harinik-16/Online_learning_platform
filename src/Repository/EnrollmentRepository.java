package Repository;

import Models.Enrollment;
import Models.LessonProgress;
import Models.QuizAttempt;
import java.util.ArrayList;

public class EnrollmentRepository {

    public Enrollment save(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null.");
        }
        if (enrollment.getEnrollmentId() == null || enrollment.getEnrollmentId().trim().isEmpty()) {
            enrollment.setEnrollmentId(DataStore.generateEnrollmentId());
        }
        DataStore.enrollments.add(enrollment);
        return enrollment;
    }

    public Enrollment findById(String enrollmentId) {
        if (enrollmentId == null) return null;
        for (int i = 0; i < DataStore.enrollments.size(); i++) {
            Enrollment e = DataStore.enrollments.get(i);
            if (e.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim())) {
                return e;
            }
        }
        return null;
    }

    public ArrayList<Enrollment> findAll() {
        return new ArrayList<>(DataStore.enrollments);
    }

    public ArrayList<Enrollment> findByStudent(String studentId) {
        ArrayList<Enrollment> result = new ArrayList<>();
        if (studentId == null) return result;
        for (int i = 0; i < DataStore.enrollments.size(); i++) {
            Enrollment e = DataStore.enrollments.get(i);
            if (e.getStudentId().equalsIgnoreCase(studentId.trim())) {
                result.add(e);
            }
        }
        return result;
    }

    public ArrayList<Enrollment> findByCourse(String courseId) {
        ArrayList<Enrollment> result = new ArrayList<>();
        if (courseId == null) return result;
        for (int i = 0; i < DataStore.enrollments.size(); i++) {
            Enrollment e = DataStore.enrollments.get(i);
            if (e.getCourseId().equalsIgnoreCase(courseId.trim())) {
                result.add(e);
            }
        }
        return result;
    }

    public Enrollment findByStudentAndCourse(String studentId, String courseId) {
        if (studentId == null || courseId == null) return null;
        for (int i = 0; i < DataStore.enrollments.size(); i++) {
            Enrollment e = DataStore.enrollments.get(i);
            if (e.getStudentId().equalsIgnoreCase(studentId.trim()) &&
                e.getCourseId().equalsIgnoreCase(courseId.trim())) {
                return e;
            }
        }
        return null;
    }

    public ArrayList<Enrollment> findActiveByStudent(String studentId) {
        ArrayList<Enrollment> result = new ArrayList<>();
        if (studentId == null) return result;
        for (int i = 0; i < DataStore.enrollments.size(); i++) {
            Enrollment e = DataStore.enrollments.get(i);
            if (e.getStudentId().equalsIgnoreCase(studentId.trim()) &&
                e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE) {
                result.add(e);
            }
        }
        return result;
    }

    public boolean update(Enrollment enrollment) {
        if (enrollment == null || enrollment.getEnrollmentId() == null) return false;
        for (int i = 0; i < DataStore.enrollments.size(); i++) {
            if (DataStore.enrollments.get(i).getEnrollmentId().equals(enrollment.getEnrollmentId())) {
                DataStore.enrollments.set(i, enrollment);
                return true;
            }
        }
        return false;
    }

    // --- Lesson Progress Operations ---

    public LessonProgress saveLessonProgress(LessonProgress progress) {
        if (progress == null) {
            throw new IllegalArgumentException("LessonProgress cannot be null.");
        }
        if (progress.getProgressId() == null || progress.getProgressId().trim().isEmpty()) {
            progress.setProgressId(DataStore.generateProgressId());
        }
        DataStore.lessonProgresses.add(progress);
        return progress;
    }

    public LessonProgress findLessonProgress(String enrollmentId, String lessonId) {
        if (enrollmentId == null || lessonId == null) return null;
        for (int i = 0; i < DataStore.lessonProgresses.size(); i++) {
            LessonProgress lp = DataStore.lessonProgresses.get(i);
            if (lp.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim()) &&
                lp.getLessonId().equalsIgnoreCase(lessonId.trim())) {
                return lp;
            }
        }
        return null;
    }

    public ArrayList<LessonProgress> findLessonProgressesByEnrollment(String enrollmentId) {
        ArrayList<LessonProgress> result = new ArrayList<>();
        if (enrollmentId == null) return result;
        for (int i = 0; i < DataStore.lessonProgresses.size(); i++) {
            LessonProgress lp = DataStore.lessonProgresses.get(i);
            if (lp.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim())) {
                result.add(lp);
            }
        }
        return result;
    }

    public boolean updateLessonProgress(LessonProgress progress) {
        if (progress == null || progress.getProgressId() == null) return false;
        for (int i = 0; i < DataStore.lessonProgresses.size(); i++) {
            if (DataStore.lessonProgresses.get(i).getProgressId().equals(progress.getProgressId())) {
                DataStore.lessonProgresses.set(i, progress);
                return true;
            }
        }
        return false;
    }

    // --- Quiz Attempt Operations ---

    public QuizAttempt saveQuizAttempt(QuizAttempt attempt) {
        if (attempt == null) {
            throw new IllegalArgumentException("QuizAttempt cannot be null.");
        }
        if (attempt.getAttemptId() == null || attempt.getAttemptId().trim().isEmpty()) {
            attempt.setAttemptId(DataStore.generateAttemptId());
        }
        DataStore.quizAttempts.add(attempt);
        return attempt;
    }

    public ArrayList<QuizAttempt> findQuizAttempts(String enrollmentId, String lessonId) {
        ArrayList<QuizAttempt> result = new ArrayList<>();
        if (enrollmentId == null || lessonId == null) return result;
        for (int i = 0; i < DataStore.quizAttempts.size(); i++) {
            QuizAttempt qa = DataStore.quizAttempts.get(i);
            if (qa.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim()) &&
                qa.getLessonId().equalsIgnoreCase(lessonId.trim())) {
                result.add(qa);
            }
        }
        return result;
    }
}
