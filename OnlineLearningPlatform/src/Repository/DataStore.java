package Repository;

import Models.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DataStore {
    // Shared static final in-memory lists
    public static final ArrayList<User> users = new ArrayList<>();
    public static final ArrayList<Course> courses = new ArrayList<>();
    public static final ArrayList<Enrollment> enrollments = new ArrayList<>();
    public static final ArrayList<Payment> payments = new ArrayList<>();
    public static final ArrayList<Review> reviews = new ArrayList<>();
    public static final ArrayList<Certificate> certificates = new ArrayList<>();
    public static final ArrayList<InstructorEarning> instructorEarnings = new ArrayList<>();
    public static final ArrayList<LessonProgress> lessonProgresses = new ArrayList<>();
    public static final ArrayList<QuizAttempt> quizAttempts = new ArrayList<>();

    // Shared unique ID counters
    private static int userIdCounter = 100;
    private static int courseIdCounter = 100;
    private static int sectionIdCounter = 100;
    private static int lessonIdCounter = 100;
    private static int enrollmentIdCounter = 100;
    private static int paymentIdCounter = 100;
    private static int reviewIdCounter = 100;
    private static int certificateIdCounter = 100;
    private static int earningIdCounter = 100;
    private static int progressIdCounter = 100;
    private static int attemptIdCounter = 100;

    // Time simulation support for time-dependent business rules
    private static long simulatedMinutesOffset = 0;

    public static synchronized String generateUserId() {
        return "USR-" + (++userIdCounter);
    }

    public static synchronized String generateCourseId() {
        return "CRS-" + (++courseIdCounter);
    }

    public static synchronized String generateSectionId() {
        return "SEC-" + (++sectionIdCounter);
    }

    public static synchronized String generateLessonId() {
        return "LES-" + (++lessonIdCounter);
    }

    public static synchronized String generateEnrollmentId() {
        return "ENR-" + (++enrollmentIdCounter);
    }

    public static synchronized String generatePaymentId() {
        return "PAY-" + (++paymentIdCounter);
    }

    public static synchronized String generateReviewId() {
        return "REV-" + (++reviewIdCounter);
    }

    public static synchronized String generateCertificateId() {
        return "CERT-" + (++certificateIdCounter);
    }

    public static synchronized String generateEarningId() {
        return "EARN-" + (++earningIdCounter);
    }

    public static synchronized String generateProgressId() {
        return "PRG-" + (++progressIdCounter);
    }

    public static synchronized String generateAttemptId() {
        return "ATT-" + (++attemptIdCounter);
    }

    // Time management methods
    public static LocalDateTime getCurrentTime() {
        return LocalDateTime.now().plusMinutes(simulatedMinutesOffset);
    }

    public static void advanceHours(long hours) {
        simulatedMinutesOffset += (hours * 60);
    }

    public static void advanceDays(long days) {
        simulatedMinutesOffset += (days * 24 * 60);
    }

    public static void advanceMinutes(long minutes) {
        simulatedMinutesOffset += minutes;
    }

    public static void resetTimeOffset() {
        simulatedMinutesOffset = 0;
    }

    public static long getSimulatedMinutesOffset() {
        return simulatedMinutesOffset;
    }

    // Helper to clear all data (useful for test resets)
    public static void clearAllData() {
        users.clear();
        courses.clear();
        enrollments.clear();
        payments.clear();
        reviews.clear();
        certificates.clear();
        instructorEarnings.clear();
        lessonProgresses.clear();
        quizAttempts.clear();
        simulatedMinutesOffset = 0;
    }
}
