package CLI;

import Models.*;
import Repository.UserRepository;
import Service.*;
import java.util.ArrayList;
import java.util.Scanner;

public class AdminCLI {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final EarningService earningService;
    private final UserRepository userRepository;
    private final Scanner scanner;
    private final User admin;

    public AdminCLI(CourseService courseService,
                    EnrollmentService enrollmentService,
                    PaymentService paymentService,
                    RefundService refundService,
                    EarningService earningService,
                    UserRepository userRepository,
                    Scanner scanner,
                    User admin) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.earningService = earningService;
        this.userRepository = userRepository;
        this.scanner = scanner;
        this.admin = admin;
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n==================================================");
            System.out.println("              ADMIN PORTAL - " + admin.getName());
            System.out.println("==================================================");
            System.out.println(" 1. View All Courses");
            System.out.println(" 2. View All Users");
            System.out.println(" 3. View All Enrollments");
            System.out.println(" 4. Create Course (Admin Privilege)");
            System.out.println(" 5. Withdraw a Course (triggers automatic refunds)");
            System.out.println(" 6. View Failed / Pending Payments");
            System.out.println(" 7. View Courses Flagged for Review (< 2.5★ with >= 20 ratings)");
            System.out.println(" 8. View All Platform Instructor Earnings");
            System.out.println(" 0. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                viewAllCourses();
            } else if (input.equals("2")) {
                viewAllUsers();
            } else if (input.equals("3")) {
                viewAllEnrollments();
            } else if (input.equals("4")) {
                createAdminCourse();
            } else if (input.equals("5")) {
                withdrawCourse();
            } else if (input.equals("6")) {
                viewFailedPayments();
            } else if (input.equals("7")) {
                viewFlaggedCourses();
            } else if (input.equals("8")) {
                viewAllEarnings();
            } else if (input.equals("0")) {
                exit = true;
                System.out.println("Returning to main menu...");
            } else {
                System.out.println("Invalid choice. Please select from the menu.");
            }
        }
    }

    private void viewAllCourses() {
        System.out.println("\n--- All Courses on Platform ---");
        ArrayList<Course> list = courseService.getAllCourses();
        if (list.isEmpty()) {
            System.out.println("No courses on the platform.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            System.out.printf("[%s] %s | Instructor: %s | Price: $%.2f | Status: %s | Rating: %.1f★ (%d reviews)%s\n",
                    c.getCourseId(), c.getTitle(), c.getInstructorId(), c.getPrice(), c.getStatus(),
                    c.getAverageRating(), c.getTotalRatings(),
                    c.isFlaggedForReview() ? " [FLAGGED FOR REVIEW]" : "");
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- All Registered Users ---");
        ArrayList<User> list = userRepository.findAll();
        for (int i = 0; i < list.size(); i++) {
            User u = list.get(i);
            System.out.printf("[%s] %s | Email: %s | Role: %s\n",
                    u.getUserId(), u.getName(), u.getEmail(), u.getRole());
        }
    }

    private void viewAllEnrollments() {
        System.out.println("\n--- All Platform Enrollments ---");
        ArrayList<Enrollment> list = enrollmentService.getAllEnrollments();
        if (list.isEmpty()) {
            System.out.println("No enrollments found.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Enrollment e = list.get(i);
            System.out.printf("[%s] Student: %s | Course: %s | Price: $%.2f | Status: %s | Progress: %.1f%% | Enrolled: %s\n",
                    e.getEnrollmentId(), e.getStudentId(), e.getCourseId(), e.getEnrolledPrice(),
                    e.getStatus(), e.getProgressPercentage(), e.getEnrollmentDate());
        }
    }

    private void createAdminCourse() {
        System.out.println("\n--- Create Course as Admin ---");
        System.out.print("Enter Course Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter Course Description: ");
        String desc = scanner.nextLine().trim();

        System.out.println("Select Category: 1.PROGRAMMING 2.DESIGN 3.BUSINESS 4.DATA_SCIENCE 5.LANGUAGE");
        System.out.print("Choice: ");
        String c = scanner.nextLine().trim();
        Course.Category cat = Course.Category.PROGRAMMING;
        if (c.equals("2")) cat = Course.Category.DESIGN;
        else if (c.equals("3")) cat = Course.Category.BUSINESS;
        else if (c.equals("4")) cat = Course.Category.DATA_SCIENCE;
        else if (c.equals("5")) cat = Course.Category.LANGUAGE;

        System.out.println("Select Level: 1.BEGINNER 2.INTERMEDIATE 3.ADVANCED");
        System.out.print("Choice: ");
        String l = scanner.nextLine().trim();
        Course.Level lvl = Course.Level.BEGINNER;
        if (l.equals("2")) lvl = Course.Level.INTERMEDIATE;
        else if (l.equals("3")) lvl = Course.Level.ADVANCED;

        System.out.print("Enter Price (0 for free): ");
        double price = 0.0;
        try {
            price = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return;
        }

        try {
            Course course = courseService.createCourse(admin.getUserId(), title, desc, cat, lvl, price);
            System.out.println("Course created! Course ID: " + course.getCourseId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void withdrawCourse() {
        viewAllCourses();
        System.out.print("\nEnter Course ID to WITHDRAW: ");
        String courseId = scanner.nextLine().trim();

        System.out.print("Are you sure you want to withdraw this course? This triggers full refunds for all paid students! (y/n): ");
        String conf = scanner.nextLine().trim();
        if (!conf.equalsIgnoreCase("y") && !conf.equalsIgnoreCase("yes")) {
            System.out.println("Withdrawal cancelled.");
            return;
        }

        try {
            Course c = courseService.withdrawCourse(admin.getUserId(), courseId);
            System.out.println("\nCourse '" + c.getTitle() + "' has been WITHDRAWN.");
            System.out.println("All active/pending enrollments have been cancelled and eligible payments have been fully REFUNDED.");
        } catch (Exception e) {
            System.out.println("Course withdrawal failed: " + e.getMessage());
        }
    }

    private void viewFailedPayments() {
        System.out.println("\n--- Failed Payments ---");
        ArrayList<Payment> list = paymentService.getFailedOrPendingPayments();
        if (list.isEmpty()) {
            System.out.println("No failed payments recorded.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Payment p = list.get(i);
            System.out.printf("[%s] Enrollment ID: %s | Amount: $%.2f | Method: %s | Status: %s | Date: %s\n",
                    p.getPaymentId(), p.getEnrollmentId(), p.getAmount(), p.getMethod(), p.getStatus(), p.getPaymentDate());
        }
    }

    private void viewFlaggedCourses() {
        System.out.println("\n--- Courses Flagged for Admin Review (Avg Rating < 2.5 with >= 20 ratings) ---");
        ArrayList<Course> flagged = courseService.getFlaggedCourses();
        if (flagged.isEmpty()) {
            System.out.println("No courses currently flagged for review.");
            return;
        }
        for (int i = 0; i < flagged.size(); i++) {
            Course c = flagged.get(i);
            System.out.printf("CRITICAL ALERT: Course [%s] '%s' has rating %.2f★ over %d reviews! Status: %s\n",
                    c.getCourseId(), c.getTitle(), c.getAverageRating(), c.getTotalRatings(), c.getStatus());
        }
    }

    private void viewAllEarnings() {
        System.out.println("\n--- All Platform Instructor Earnings ---");
        // Update any earnings that have matured
        earningService.processEligibleEarnings(enrollmentService.getAllEnrollments());

        ArrayList<InstructorEarning> list = earningService.getAllEarnings();
        if (list.isEmpty()) {
            System.out.println("No earnings records found.");
            return;
        }

        double totalPlatformShare = 0.0;
        double totalInstructorCredited = 0.0;
        double totalInstructorPending = 0.0;

        for (int i = 0; i < list.size(); i++) {
            InstructorEarning e = list.get(i);
            totalPlatformShare += e.getPlatformShare();
            if (e.isCredited()) {
                totalInstructorCredited += e.getInstructorShare();
            } else {
                totalInstructorPending += e.getInstructorShare();
            }
            System.out.printf("[%s] Instructor: %s | Course: %s | Total: $%.2f | Inst. Share (70%%): $%.2f | Plat. Share (30%%): $%.2f | Credited: %s\n",
                    e.getEarningId(), e.getInstructorId(), e.getCourseId(), e.getCoursePrice(),
                    e.getInstructorShare(), e.getPlatformShare(), e.isCredited() ? "YES" : "PENDING (Escrow)");
        }

        System.out.println("--------------------------------------------------");
        System.out.printf("Total Platform Revenue (30%%): $%.2f\n", totalPlatformShare);
        System.out.printf("Total Instructor Earnings Credited: $%.2f\n", totalInstructorCredited);
        System.out.printf("Total Instructor Earnings in Escrow (7-day pending): $%.2f\n", totalInstructorPending);
        System.out.println("--------------------------------------------------");
    }
}
