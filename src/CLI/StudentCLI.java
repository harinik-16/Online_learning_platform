package CLI;

import Models.*;
import Service.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class StudentCLI {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final ReviewService reviewService;
    private final CertificateService certificateService;
    private final Scanner scanner;
    private final User student;

    public StudentCLI(CourseService courseService,
                      EnrollmentService enrollmentService,
                      ProgressService progressService,
                      PaymentService paymentService,
                      RefundService refundService,
                      ReviewService reviewService,
                      CertificateService certificateService,
                      Scanner scanner,
                      User student) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.reviewService = reviewService;
        this.certificateService = certificateService;
        this.scanner = scanner;
        this.student = student;
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n==================================================");
            System.out.println("            STUDENT PORTAL - " + student.getName());
            System.out.println("==================================================");
            System.out.println(" 1. View Published Courses");
            System.out.println(" 2. View Course Sections & Lessons");
            System.out.println(" 3. Enroll in a Course");
            System.out.println(" 4. Make or Retry Payment");
            System.out.println(" 5. View My Enrollments");
            System.out.println(" 6. View Course Progress");
            System.out.println(" 7. Complete Video Lesson");
            System.out.println(" 8. Complete Reading Lesson");
            System.out.println(" 9. Attempt Quiz");
            System.out.println("10. View Quiz Attempts & Cooldown");
            System.out.println("11. Request Refund");
            System.out.println("12. Submit Course Review");
            System.out.println("13. Edit Review");
            System.out.println("14. View My Certificates");
            System.out.println(" 0. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                viewPublishedCourses();
            } else if (input.equals("2")) {
                viewCourseCurriculum();
            } else if (input.equals("3")) {
                enrollInCourse();
            } else if (input.equals("4")) {
                makePayment();
            } else if (input.equals("5")) {
                viewMyEnrollments();
            } else if (input.equals("6")) {
                viewCourseProgress();
            } else if (input.equals("7")) {
                completeVideoLesson();
            } else if (input.equals("8")) {
                completeReadingLesson();
            } else if (input.equals("9")) {
                attemptQuiz();
            } else if (input.equals("10")) {
                viewQuizAttempts();
            } else if (input.equals("11")) {
                requestRefund();
            } else if (input.equals("12")) {
                submitReview();
            } else if (input.equals("13")) {
                editReview();
            } else if (input.equals("14")) {
                viewCertificates();
            } else if (input.equals("0")) {
                exit = true;
                System.out.println("Returning to main menu...");
            } else {
                System.out.println("Invalid choice. Please select from the menu.");
            }
        }
    }

    private void viewPublishedCourses() {
        System.out.println("\n--- Published Courses ---");
        ArrayList<Course> list = courseService.getPublishedCourses();
        if (list.isEmpty()) {
            System.out.println("No published courses available at this time.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            System.out.printf("[%s] %s | Category: %s | Level: %s | Price: $%.2f | Rating: %.1f★ (%d reviews)\n",
                    c.getCourseId(), c.getTitle(), c.getCategory(), c.getLevel(), c.getPrice(),
                    c.getAverageRating(), c.getTotalRatings());
            System.out.println("    Description: " + c.getDescription());
        }
    }

    private void viewCourseCurriculum() {
        System.out.print("\nEnter Course ID to view curriculum: ");
        String courseId = scanner.nextLine().trim();
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            System.out.println("Course not found with ID: " + courseId);
            return;
        }

        System.out.println("\nCurriculum for: " + course.getTitle() + " (" + course.getStatus() + ")");
        ArrayList<Section> sections = course.getSections();
        if (sections.isEmpty()) {
            System.out.println("No sections added yet.");
            return;
        }

        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            System.out.println("\nSection " + s.getOrderNumber() + ": " + s.getTitle() + " [ID: " + s.getSectionId() + "]");
            ArrayList<Lesson> lessons = s.getLessons();
            if (lessons.isEmpty()) {
                System.out.println("  (No lessons in this section)");
            } else {
                for (int j = 0; j < lessons.size(); j++) {
                    Lesson l = lessons.get(j);
                    System.out.printf("  %d.%d [%s] %s (Type: %s, Duration: %d mins)\n",
                            s.getOrderNumber(), l.getOrderNumber(), l.getLessonId(), l.getTitle(), l.getType(), l.getDurationMinutes());
                }
            }
        }
    }

    private void enrollInCourse() {
        viewPublishedCourses();
        System.out.print("\nEnter Course ID to enroll: ");
        String courseId = scanner.nextLine().trim();
        try {
            Enrollment enr = enrollmentService.enrollStudent(student.getUserId(), courseId);
            System.out.println("\nSuccess! Enrollment created with ID: " + enr.getEnrollmentId());
            System.out.println("Status: " + enr.getStatus() + " | Price: $" + enr.getEnrolledPrice());
            if (enr.getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT) {
                System.out.println("Note: Course is paid. Please complete payment within 24 hours to activate.");
            } else {
                System.out.println("Free course! Enrollment is immediately ACTIVE.");
            }
        } catch (Exception e) {
            System.out.println("Enrollment failed: " + e.getMessage());
        }
    }

    private void makePayment() {
        ArrayList<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(student.getUserId());
        ArrayList<Enrollment> pending = new ArrayList<>();
        for (int i = 0; i < enrollments.size(); i++) {
            if (enrollments.get(i).getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT) {
                pending.add(enrollments.get(i));
            }
        }

        if (pending.isEmpty()) {
            System.out.println("\nYou have no enrollments pending payment.");
            return;
        }

        System.out.println("\n--- Enrollments Pending Payment ---");
        for (int i = 0; i < pending.size(); i++) {
            Enrollment e = pending.get(i);
            System.out.printf("[%s] Course ID: %s | Price: $%.2f | Enrolled: %s\n",
                    e.getEnrollmentId(), e.getCourseId(), e.getEnrolledPrice(), e.getEnrollmentDate());
        }

        System.out.print("\nEnter Enrollment ID to pay for: ");
        String enrollmentId = scanner.nextLine().trim();

        System.out.println("Select Payment Method:");
        System.out.println("1. UPI");
        System.out.println("2. CARD");
        System.out.println("3. NET_BANKING");
        System.out.print("Choice: ");
        String mChoice = scanner.nextLine().trim();
        Payment.PaymentMethod method = Payment.PaymentMethod.UPI;
        if (mChoice.equals("2")) {
            method = Payment.PaymentMethod.CARD;
        } else if (mChoice.equals("3")) {
            method = Payment.PaymentMethod.NET_BANKING;
        }

        System.out.println("Simulate Payment Result:");
        System.out.println("1. Success (Simulate Successful Payment)");
        System.out.println("2. Failure (Simulate Failed Payment)");
        System.out.print("Choice: ");
        String sChoice = scanner.nextLine().trim();
        boolean success = sChoice.equals("1");

        try {
            Payment payment = paymentService.processPayment(student.getUserId(), enrollmentId, method, success);
            System.out.println("\nPayment processed! Payment ID: " + payment.getPaymentId() +
                    " | Status: " + payment.getStatus() + " | Amount: $" + payment.getAmount());
            if (payment.getStatus() == Payment.PaymentStatus.PAID) {
                System.out.println("Enrollment is now ACTIVE! You can begin learning.");
            } else {
                System.out.println("Payment failed. Enrollment remains PENDING_PAYMENT. You can retry within 24 hours of enrollment.");
            }
        } catch (Exception e) {
            System.out.println("Payment error: " + e.getMessage());
        }
    }

    private void viewMyEnrollments() {
        System.out.println("\n--- My Enrollments ---");
        ArrayList<Enrollment> list = enrollmentService.getEnrollmentsByStudent(student.getUserId());
        if (list.isEmpty()) {
            System.out.println("You are not enrolled in any courses.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Enrollment e = list.get(i);
            Course c = courseService.getCourseById(e.getCourseId());
            String title = c != null ? c.getTitle() : "Unknown";
            System.out.printf("[%s] %s (Course ID: %s) | Status: %s | Progress: %.1f%% | Price: $%.2f\n",
                    e.getEnrollmentId(), title, e.getCourseId(), e.getStatus(), e.getProgressPercentage(), e.getEnrolledPrice());
        }
    }

    private void viewCourseProgress() {
        System.out.print("\nEnter Enrollment ID to view progress: ");
        String enrollmentId = scanner.nextLine().trim();
        Enrollment enr = enrollmentService.getEnrollmentById(enrollmentId);
        if (enr == null || !enr.getStudentId().equalsIgnoreCase(student.getUserId())) {
            System.out.println("Enrollment not found.");
            return;
        }

        Course course = courseService.getCourseById(enr.getCourseId());
        System.out.printf("\nProgress for '%s': %.1f%% [Status: %s]\n",
                course != null ? course.getTitle() : "Course", enr.getProgressPercentage(), enr.getStatus());

        ArrayList<LessonProgress> progresses = progressService.getProgressForEnrollment(enrollmentId);
        ArrayList<Lesson> lessons = course != null ? course.getAllLessonsInOrder() : new ArrayList<>();

        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);
            LessonProgress lp = null;
            for (int j = 0; j < progresses.size(); j++) {
                if (progresses.get(j).getLessonId().equals(l.getLessonId())) {
                    lp = progresses.get(j);
                    break;
                }
            }
            String statusStr = "NOT STARTED";
            if (lp != null && lp.isCompleted()) {
                statusStr = "COMPLETED (" + lp.getCompletionDate() + ")";
            } else if (lp != null) {
                if (l.getType() == Lesson.LessonType.VIDEO) statusStr = "WATCHED " + lp.getVideoWatchPercentage() + "%";
                if (l.getType() == Lesson.LessonType.QUIZ) statusStr = "LAST SCORE " + lp.getQuizScore() + "%";
            }
            System.out.printf("  %d. [%s] %s (%s) -> %s\n", (i + 1), l.getLessonId(), l.getTitle(), l.getType(), statusStr);
        }
    }

    private void completeVideoLesson() {
        System.out.print("\nEnter Enrollment ID: ");
        String enrollmentId = scanner.nextLine().trim();
        System.out.print("Enter Video Lesson ID: ");
        String lessonId = scanner.nextLine().trim();
        System.out.print("Enter Video Watch Percentage (e.g. 95 for 95%): ");
        double watch = 0.0;
        try {
            watch = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid percentage format.");
            return;
        }

        try {
            LessonProgress lp = progressService.completeVideoLesson(student.getUserId(), enrollmentId, lessonId, watch);
            System.out.println("\nLesson updated: Completed = " + lp.isCompleted() +
                    " | Watch Percentage = " + lp.getVideoWatchPercentage() + "%");
            if (lp.isCompleted()) {
                System.out.println("Congratulations! Video lesson marked complete.");
            }
        } catch (Exception e) {
            System.out.println("Error completing video lesson: " + e.getMessage());
        }
    }

    private void completeReadingLesson() {
        System.out.print("\nEnter Enrollment ID: ");
        String enrollmentId = scanner.nextLine().trim();
        System.out.print("Enter Reading Lesson ID: ");
        String lessonId = scanner.nextLine().trim();
        System.out.print("Do you confirm you have completed reading this lesson? (y/n): ");
        String conf = scanner.nextLine().trim();
        boolean confirmed = conf.equalsIgnoreCase("y") || conf.equalsIgnoreCase("yes");

        try {
            LessonProgress lp = progressService.completeReadingLesson(student.getUserId(), enrollmentId, lessonId, confirmed);
            System.out.println("\nReading lesson marked complete: " + lp.isCompleted());
        } catch (Exception e) {
            System.out.println("Error completing reading lesson: " + e.getMessage());
        }
    }

    private void attemptQuiz() {
        System.out.print("\nEnter Enrollment ID: ");
        String enrollmentId = scanner.nextLine().trim();
        System.out.print("Enter Quiz Lesson ID: ");
        String lessonId = scanner.nextLine().trim();
        System.out.print("Enter your Quiz Score (0 - 100): ");
        double score = 0.0;
        try {
            score = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid score format.");
            return;
        }

        try {
            QuizAttempt attempt = progressService.attemptQuizLesson(student.getUserId(), enrollmentId, lessonId, score);
            System.out.println("\nQuiz Attempt Recorded!");
            System.out.println("Attempt ID: " + attempt.getAttemptId() + " | Score: " + attempt.getScore() + "%");
            System.out.println("Result: " + (attempt.isPassed() ? "PASSED (>= 60%)" : "FAILED (< 60%)"));
        } catch (Exception e) {
            System.out.println("Quiz attempt blocked / failed: " + e.getMessage());
        }
    }

    private void viewQuizAttempts() {
        System.out.print("\nEnter Enrollment ID: ");
        String enrollmentId = scanner.nextLine().trim();
        System.out.print("Enter Quiz Lesson ID: ");
        String lessonId = scanner.nextLine().trim();

        ArrayList<QuizAttempt> attempts = progressService.getQuizAttemptsForLesson(enrollmentId, lessonId);
        if (attempts.isEmpty()) {
            System.out.println("No attempts found for this quiz.");
            return;
        }

        System.out.println("\n--- Quiz Attempts History ---");
        for (int i = 0; i < attempts.size(); i++) {
            QuizAttempt a = attempts.get(i);
            System.out.printf("Attempt %d: Score = %.1f%% | Passed = %s | Date = %s\n",
                    (i + 1), a.getScore(), a.isPassed() ? "YES" : "NO", a.getAttemptDate());
        }

        LocalDateTime cooldownEnd = progressService.getQuizCooldownEndTime(enrollmentId, lessonId);
        if (cooldownEnd != null) {
            System.out.println("\nALERT: Quiz is locked due to 3 failed attempts.");
            System.out.println("Mandatory 24-hour cooldown ends at: " + cooldownEnd);
        } else {
            System.out.println("\nQuiz Status: Ready for attempts.");
        }
    }

    private void requestRefund() {
        System.out.print("\nEnter Enrollment ID to request refund: ");
        String enrollmentId = scanner.nextLine().trim();
        try {
            Payment payment = refundService.requestStudentRefund(student.getUserId(), enrollmentId);
            System.out.println("\nRefund approved! Payment ID " + payment.getPaymentId() +
                    " has been marked REFUNDED for amount $" + payment.getAmount());
            System.out.println("Enrollment status has been updated to CANCELLED.");
        } catch (Exception e) {
            System.out.println("Refund rejected: " + e.getMessage());
        }
    }

    private void submitReview() {
        System.out.print("\nEnter Course ID to review: ");
        String courseId = scanner.nextLine().trim();
        System.out.print("Enter Rating (1 to 5): ");
        int rating = 0;
        try {
            rating = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid rating number.");
            return;
        }
        System.out.print("Enter Review Comment: ");
        String comment = scanner.nextLine().trim();

        try {
            Review review = reviewService.submitReview(student.getUserId(), courseId, rating, comment);
            System.out.println("\nReview submitted successfully! Review ID: " + review.getReviewId());
        } catch (Exception e) {
            System.out.println("Review submission failed: " + e.getMessage());
        }
    }

    private void editReview() {
        System.out.print("\nEnter Review ID to edit: ");
        String reviewId = scanner.nextLine().trim();
        System.out.print("Enter New Rating (1 to 5): ");
        int rating = 0;
        try {
            rating = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid rating number.");
            return;
        }
        System.out.print("Enter New Review Comment: ");
        String comment = scanner.nextLine().trim();

        try {
            Review review = reviewService.editReview(student.getUserId(), reviewId, rating, comment);
            System.out.println("\nReview updated successfully! Last updated at: " + review.getLastUpdatedDate());
        } catch (Exception e) {
            System.out.println("Review edit failed: " + e.getMessage());
        }
    }

    private void viewCertificates() {
        System.out.println("\n--- My Certificates ---");
        ArrayList<Certificate> list = certificateService.getCertificatesByStudent(student.getUserId());
        if (list.isEmpty()) {
            System.out.println("No certificates issued yet. Complete 100% of a course to earn a certificate!");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Certificate cert = list.get(i);
            Course c = courseService.getCourseById(cert.getCourseId());
            System.out.printf("★ Certificate ID: %s | Course: %s | Issued: %s\n",
                    cert.getCertificateId(), (c != null ? c.getTitle() : cert.getCourseId()), cert.getIssueDate());
        }
    }
}
