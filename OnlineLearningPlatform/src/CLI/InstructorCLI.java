package CLI;

import Models.*;
import Service.*;
import java.util.ArrayList;
import java.util.Scanner;

public class InstructorCLI {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final EarningService earningService;
    private final Scanner scanner;
    private final User instructor;

    public InstructorCLI(CourseService courseService,
                         EnrollmentService enrollmentService,
                         ProgressService progressService,
                         EarningService earningService,
                         Scanner scanner,
                         User instructor) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.earningService = earningService;
        this.scanner = scanner;
        this.instructor = instructor;
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n==================================================");
            System.out.println("          INSTRUCTOR PORTAL - " + instructor.getName());
            System.out.println("==================================================");
            System.out.println(" 1. Create a Course");
            System.out.println(" 2. Add Section to Course");
            System.out.println(" 3. Add Lesson to Section");
            System.out.println(" 4. View My Courses & Curriculum");
            System.out.println(" 5. Publish Course");
            System.out.println(" 6. Edit Course Title & Description");
            System.out.println(" 7. Change Course Price");
            System.out.println(" 8. Delete Lesson (allowed only before enrollment)");
            System.out.println(" 9. View Student Enrollments & Progress");
            System.out.println("10. View Earnings & Pending Credits");
            System.out.println(" 0. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                createCourse();
            } else if (input.equals("2")) {
                addSection();
            } else if (input.equals("3")) {
                addLesson();
            } else if (input.equals("4")) {
                viewMyCourses();
            } else if (input.equals("5")) {
                publishCourse();
            } else if (input.equals("6")) {
                editCourseDetails();
            } else if (input.equals("7")) {
                changeCoursePrice();
            } else if (input.equals("8")) {
                deleteLesson();
            } else if (input.equals("9")) {
                viewStudentEnrollments();
            } else if (input.equals("10")) {
                viewEarnings();
            } else if (input.equals("0")) {
                exit = true;
                System.out.println("Returning to main menu...");
            } else {
                System.out.println("Invalid choice. Please select from the menu.");
            }
        }
    }

    private void createCourse() {
        System.out.println("\n--- Create New Course ---");
        System.out.print("Enter Course Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter Course Description: ");
        String description = scanner.nextLine().trim();

        System.out.println("Select Category:");
        System.out.println("1. PROGRAMMING");
        System.out.println("2. DESIGN");
        System.out.println("3. BUSINESS");
        System.out.println("4. DATA_SCIENCE");
        System.out.println("5. LANGUAGE");
        System.out.print("Choice: ");
        String catChoice = scanner.nextLine().trim();
        Course.Category category = Course.Category.PROGRAMMING;
        if (catChoice.equals("2")) category = Course.Category.DESIGN;
        else if (catChoice.equals("3")) category = Course.Category.BUSINESS;
        else if (catChoice.equals("4")) category = Course.Category.DATA_SCIENCE;
        else if (catChoice.equals("5")) category = Course.Category.LANGUAGE;

        System.out.println("Select Level:");
        System.out.println("1. BEGINNER");
        System.out.println("2. INTERMEDIATE");
        System.out.println("3. ADVANCED");
        System.out.print("Choice: ");
        String lvlChoice = scanner.nextLine().trim();
        Course.Level level = Course.Level.BEGINNER;
        if (lvlChoice.equals("2")) level = Course.Level.INTERMEDIATE;
        else if (lvlChoice.equals("3")) level = Course.Level.ADVANCED;

        System.out.print("Enter Course Price (0 for free): ");
        double price = 0.0;
        try {
            price = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format. Course creation cancelled.");
            return;
        }

        try {
            Course course = courseService.createCourse(instructor.getUserId(), title, description, category, level, price);
            System.out.println("\nCourse created successfully! Course ID: " + course.getCourseId() +
                    " [Status: " + course.getStatus() + "]");
            System.out.println("Note: Add at least 1 section and 3 lessons across all sections before publishing.");
        } catch (Exception e) {
            System.out.println("Error creating course: " + e.getMessage());
        }
    }

    private void addSection() {
        viewMyCourses();
        System.out.print("\nEnter Course ID to add section to: ");
        String courseId = scanner.nextLine().trim();
        System.out.print("Enter Section Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter Section Order Number (e.g. 1, 2): ");
        int order = 1;
        try {
            order = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid order number.");
            return;
        }

        try {
            Section section = courseService.addSection(instructor.getUserId(), courseId, title, order);
            System.out.println("Section added successfully! Section ID: " + section.getSectionId());
        } catch (Exception e) {
            System.out.println("Error adding section: " + e.getMessage());
        }
    }

    private void addLesson() {
        viewMyCourses();
        System.out.print("\nEnter Course ID: ");
        String courseId = scanner.nextLine().trim();
        System.out.print("Enter Section ID: ");
        String sectionId = scanner.nextLine().trim();
        System.out.print("Enter Lesson Title: ");
        String title = scanner.nextLine().trim();

        System.out.println("Select Lesson Type:");
        System.out.println("1. VIDEO");
        System.out.println("2. READING");
        System.out.println("3. QUIZ");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        Lesson.LessonType type = Lesson.LessonType.VIDEO;
        if (typeChoice.equals("2")) type = Lesson.LessonType.READING;
        else if (typeChoice.equals("3")) type = Lesson.LessonType.QUIZ;

        System.out.print("Enter Lesson Duration in Minutes: ");
        int duration = 10;
        try {
            duration = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid duration.");
            return;
        }

        System.out.print("Enter Lesson Order Number in this section (e.g. 1, 2): ");
        int order = 1;
        try {
            order = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid order number.");
            return;
        }

        try {
            Lesson lesson = courseService.addLesson(instructor.getUserId(), courseId, sectionId, title, type, duration, order);
            System.out.println("Lesson added successfully! Lesson ID: " + lesson.getLessonId());
        } catch (Exception e) {
            System.out.println("Error adding lesson: " + e.getMessage());
        }
    }

    private void viewMyCourses() {
        System.out.println("\n--- My Courses ---");
        ArrayList<Course> list = courseService.getCoursesByInstructor(instructor.getUserId());
        if (list.isEmpty()) {
            System.out.println("You have not created any courses yet.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            System.out.printf("[%s] %s | Category: %s | Level: %s | Price: $%.2f | Status: %s | Rating: %.1f★ (%d reviews)\n",
                    c.getCourseId(), c.getTitle(), c.getCategory(), c.getLevel(), c.getPrice(), c.getStatus(),
                    c.getAverageRating(), c.getTotalRatings());

            ArrayList<Section> sections = c.getSections();
            for (int j = 0; j < sections.size(); j++) {
                Section s = sections.get(j);
                System.out.println("    Section " + s.getOrderNumber() + ": " + s.getTitle() + " [ID: " + s.getSectionId() + "]");
                ArrayList<Lesson> lessons = s.getLessons();
                for (int k = 0; k < lessons.size(); k++) {
                    Lesson l = lessons.get(k);
                    System.out.printf("      Lesson %d: [%s] %s (%s, %d mins)\n",
                            l.getOrderNumber(), l.getLessonId(), l.getTitle(), l.getType(), l.getDurationMinutes());
                }
            }
        }
    }

    private void publishCourse() {
        viewMyCourses();
        System.out.print("\nEnter Course ID to publish: ");
        String courseId = scanner.nextLine().trim();

        try {
            Course c = courseService.publishCourse(instructor.getUserId(), courseId);
            System.out.println("\nSuccess! Course '" + c.getTitle() + "' is now PUBLISHED and open for student enrollment!");
        } catch (Exception e) {
            System.out.println("Publish failed: " + e.getMessage());
        }
    }

    private void editCourseDetails() {
        viewMyCourses();
        System.out.print("\nEnter Course ID to edit: ");
        String courseId = scanner.nextLine().trim();
        System.out.print("Enter New Title (leave blank to keep current): ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter New Description (leave blank to keep current): ");
        String desc = scanner.nextLine().trim();

        try {
            courseService.editCourseDetails(instructor.getUserId(), courseId, title, desc);
            System.out.println("Course details updated successfully.");
        } catch (Exception e) {
            System.out.println("Error updating course: " + e.getMessage());
        }
    }

    private void changeCoursePrice() {
        viewMyCourses();
        System.out.print("\nEnter Course ID to change price: ");
        String courseId = scanner.nextLine().trim();
        System.out.print("Enter New Price: ");
        double price = 0.0;
        try {
            price = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format.");
            return;
        }

        try {
            courseService.updatePrice(instructor.getUserId(), courseId, price);
            System.out.println("Course price updated to $" + price + ". Existing enrollments will retain their original enrolled price.");
        } catch (Exception e) {
            System.out.println("Error updating price: " + e.getMessage());
        }
    }

    private void deleteLesson() {
        System.out.print("\nEnter Course ID: ");
        String courseId = scanner.nextLine().trim();
        System.out.print("Enter Section ID: ");
        String sectionId = scanner.nextLine().trim();
        System.out.print("Enter Lesson ID to delete: ");
        String lessonId = scanner.nextLine().trim();

        try {
            boolean deleted = courseService.deleteLesson(instructor.getUserId(), courseId, sectionId, lessonId);
            if (deleted) {
                System.out.println("Lesson deleted successfully.");
            } else {
                System.out.println("Lesson not found.");
            }
        } catch (Exception e) {
            System.out.println("Lesson deletion blocked: " + e.getMessage());
        }
    }

    private void viewStudentEnrollments() {
        System.out.print("\nEnter Course ID to view student progress: ");
        String courseId = scanner.nextLine().trim();
        ArrayList<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);

        if (enrollments.isEmpty()) {
            System.out.println("No students enrolled in this course yet.");
            return;
        }

        System.out.println("\n--- Student Enrollments for Course " + courseId + " ---");
        for (int i = 0; i < enrollments.size(); i++) {
            Enrollment e = enrollments.get(i);
            System.out.printf("Student ID: %s | Enrollment ID: %s | Status: %s | Progress: %.1f%% | Enrolled: %s\n",
                    e.getStudentId(), e.getEnrollmentId(), e.getStatus(), e.getProgressPercentage(), e.getEnrollmentDate());
        }
    }

    private void viewEarnings() {
        System.out.println("\n--- Instructor Financial Earnings (70% Share) ---");
        ArrayList<InstructorEarning> list = earningService.getEarningsByInstructor(instructor.getUserId());
        if (list.isEmpty()) {
            System.out.println("No earnings records found.");
            return;
        }

        // Process any earnings that have matured past the 7-day refund window
        ArrayList<Enrollment> currentEnrollments = enrollmentService.getAllEnrollments();
        int maturedCount = earningService.processEligibleEarnings(currentEnrollments);
        if (maturedCount > 0) {
            System.out.println("[Notice: " + maturedCount + " earnings have matured past the 7-day refund window and were credited!]");
        }

        for (int i = 0; i < list.size(); i++) {
            InstructorEarning e = list.get(i);
            System.out.printf("[%s] Course ID: %s | Course Price: $%.2f | My Share (70%%): $%.2f | Credited: %s | Eligible Date: %s\n",
                    e.getEarningId(), e.getCourseId(), e.getCoursePrice(), e.getInstructorShare(),
                    e.isCredited() ? "YES (Deposited)" : "PENDING (within 7-day window)", e.getEligibleCreditDate());
        }

        double creditedTotal = earningService.getTotalCreditedEarnings(instructor.getUserId());
        double pendingTotal = earningService.getTotalPendingEarnings(instructor.getUserId());
        System.out.println("--------------------------------------------------");
        System.out.printf("Total Credited Earnings: $%.2f\n", creditedTotal);
        System.out.printf("Total Pending (Escrow) Earnings: $%.2f\n", pendingTotal);
        System.out.println("--------------------------------------------------");
    }
}
