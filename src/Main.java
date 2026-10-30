import CLI.*;
import Models.*;
import Repository.*;
import Service.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize Repositories (all sharing the static DataStore lists)
        UserRepository userRepository = new UserRepository();
        CourseRepository courseRepository = new CourseRepository();
        EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        ReviewRepository reviewRepository = new ReviewRepository();
        CertificateRepository certificateRepository = new CertificateRepository();

        // Initialize Services with Constructor Dependency Injection
        EarningService earningService = new EarningService(paymentRepository);
        RefundService refundService = new RefundService(paymentRepository, enrollmentRepository, courseRepository, earningService);
        CourseService courseService = new CourseService(courseRepository, enrollmentRepository, userRepository, refundService);
        EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepository, courseRepository, userRepository);
        CertificateService certificateService = new CertificateService(certificateRepository, enrollmentRepository);
        ProgressService progressService = new ProgressService(enrollmentRepository, courseRepository, certificateService);
        PaymentService paymentService = new PaymentService(paymentRepository, enrollmentRepository, courseRepository, earningService);
        ReviewService reviewService = new ReviewService(reviewRepository, enrollmentRepository, courseRepository);

        // Seed Initial Mock Data for seamless demonstration
        seedInitialData(userRepository, courseService);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("===============================================================");
        System.out.println("   WELCOME TO ONLINE LEARNING PLATFORM (CORE JAVA LLD)   ");
        System.out.println("===============================================================");

        while (running) {
            System.out.println("\n---------------------------------------------------------------");
            System.out.println("                       MAIN MENU                               ");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Current System Time: " + DataStore.getCurrentTime());
            if (DataStore.getSimulatedMinutesOffset() > 0) {
                System.out.println("[Time Travel Active: +" + (DataStore.getSimulatedMinutesOffset() / 60) + " hours ahead]");
            }
            System.out.println("1. Student Portal");
            System.out.println("2. Instructor Portal");
            System.out.println("3. Admin Portal");
            System.out.println("4. Time Machine (Advance Time to test 24h/7d/30d business rules)");
            System.out.println("0. Exit Application");
            System.out.println("---------------------------------------------------------------");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                User student = selectOrRegisterUser(scanner, userRepository, User.Role.STUDENT);
                if (student != null) {
                    StudentCLI studentCLI = new StudentCLI(
                            courseService, enrollmentService, progressService, paymentService,
                            refundService, reviewService, certificateService, scanner, student
                    );
                    studentCLI.start();
                }
            } else if (choice.equals("2")) {
                User instructor = selectOrRegisterUser(scanner, userRepository, User.Role.INSTRUCTOR);
                if (instructor != null) {
                    InstructorCLI instructorCLI = new InstructorCLI(
                            courseService, enrollmentService, progressService, earningService, scanner, instructor
                    );
                    instructorCLI.start();
                }
            } else if (choice.equals("3")) {
                User admin = selectOrRegisterUser(scanner, userRepository, User.Role.ADMIN);
                if (admin != null) {
                    AdminCLI adminCLI = new AdminCLI(
                            courseService, enrollmentService, paymentService, refundService,
                            earningService, userRepository, scanner, admin
                    );
                    adminCLI.start();
                }
            } else if (choice.equals("4")) {
                handleTimeTravel(scanner);
            } else if (choice.equals("0")) {
                running = false;
                System.out.println("\nThank you for using Online Learning Platform. Goodbye!");
            } else {
                System.out.println("Invalid choice. Please select 1, 2, 3, 4, or 0.");
            }
        }

        scanner.close();
    }

    private static User selectOrRegisterUser(Scanner scanner, UserRepository userRepository, User.Role requiredRole) {
        ArrayList<User> allUsers = userRepository.findAll();
        ArrayList<User> roleUsers = new ArrayList<>();
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getRole() == requiredRole) {
                roleUsers.add(allUsers.get(i));
            }
        }

        System.out.println("\nSelect " + requiredRole + " Account:");
        for (int i = 0; i < roleUsers.size(); i++) {
            User u = roleUsers.get(i);
            System.out.printf(" %d. %s (%s) [ID: %s]\n", (i + 1), u.getName(), u.getEmail(), u.getUserId());
        }
        System.out.println(" R. Register a New " + requiredRole);
        System.out.println(" 0. Cancel / Go Back");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        if (choice.equalsIgnoreCase("0")) {
            return null;
        } else if (choice.equalsIgnoreCase("R")) {
            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine().trim();
            try {
                User newUser = new User(DataStore.generateUserId(), name, email, password, requiredRole);
                userRepository.save(newUser);
                System.out.println("User registered successfully with ID: " + newUser.getUserId());
                return newUser;
            } catch (Exception e) {
                System.out.println("Registration failed: " + e.getMessage());
                return null;
            }
        } else {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < roleUsers.size()) {
                    return roleUsers.get(index);
                } else {
                    System.out.println("Invalid selection.");
                    return null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
                return null;
            }
        }
    }

    private static void handleTimeTravel(Scanner scanner) {
        System.out.println("\n=== TIME MACHINE / SIMULATE TIME ===");
        System.out.println("Current Simulated Time: " + DataStore.getCurrentTime());
        System.out.println("1. Advance 24 Hours (Test pending payment expiry / quiz 24h cooldown)");
        System.out.println("2. Advance 7 Days (Test refund expiry / instructor earning 7-day maturity)");
        System.out.println("3. Advance 30 Days (Test review 30-day edit window expiration)");
        System.out.println("4. Custom Hours to Advance");
        System.out.println("5. Reset Time to Real Clock");
        System.out.println("0. Cancel");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        if (choice.equals("1")) {
            DataStore.advanceHours(24);
            System.out.println("Time advanced by 24 hours! New Time: " + DataStore.getCurrentTime());
        } else if (choice.equals("2")) {
            DataStore.advanceDays(7);
            System.out.println("Time advanced by 7 days! New Time: " + DataStore.getCurrentTime());
        } else if (choice.equals("3")) {
            DataStore.advanceDays(30);
            System.out.println("Time advanced by 30 days! New Time: " + DataStore.getCurrentTime());
        } else if (choice.equals("4")) {
            System.out.print("Enter hours to advance: ");
            try {
                long h = Long.parseLong(scanner.nextLine().trim());
                DataStore.advanceHours(h);
                System.out.println("Time advanced by " + h + " hours! New Time: " + DataStore.getCurrentTime());
            } catch (NumberFormatException e) {
                System.out.println("Invalid hours.");
            }
        } else if (choice.equals("5")) {
            DataStore.resetTimeOffset();
            System.out.println("Time reset to system clock: " + DataStore.getCurrentTime());
        } else {
            System.out.println("Time travel cancelled.");
        }
    }

    private static void seedInitialData(UserRepository userRepository, CourseService courseService) {
        // Seed default users
        User admin = new User(DataStore.generateUserId(), "System Admin", "admin@lms.com", "admin123", User.Role.ADMIN);
        User instructor = new User(DataStore.generateUserId(), "Prof. Alan Turing", "alan@lms.com", "alan123", User.Role.INSTRUCTOR);
        User student1 = new User(DataStore.generateUserId(), "Alice Student", "alice@lms.com", "alice123", User.Role.STUDENT);
        User student2 = new User(DataStore.generateUserId(), "Bob Learner", "bob@lms.com", "bob123", User.Role.STUDENT);

        userRepository.save(admin);
        userRepository.save(instructor);
        userRepository.save(student1);
        userRepository.save(student2);

        // Seed a sample course with sections and lessons
        Course course1 = courseService.createCourse(
                instructor.getUserId(),
                "Mastering Java & System Design",
                "Complete guide from Core Java to Low Level Design",
                Course.Category.PROGRAMMING,
                Course.Level.BEGINNER,
                99.99
        );

        Section sec1 = courseService.addSection(instructor.getUserId(), course1.getCourseId(), "Module 1: Java Basics", 1);
        courseService.addLesson(instructor.getUserId(), course1.getCourseId(), sec1.getSectionId(),
                "Introduction to JVM & Bytecode", Lesson.LessonType.VIDEO, 15, 1);
        courseService.addLesson(instructor.getUserId(), course1.getCourseId(), sec1.getSectionId(),
                "OOP Principles and Encapsulation Reading", Lesson.LessonType.READING, 20, 2);

        Section sec2 = courseService.addSection(instructor.getUserId(), course1.getCourseId(), "Module 2: Object-Oriented Design", 2);
        courseService.addLesson(instructor.getUserId(), course1.getCourseId(), sec2.getSectionId(),
                "Java OOP Mastery Quiz", Lesson.LessonType.QUIZ, 30, 1);

        // Publish course1 (meets requirement: at least 1 section and at least 3 lessons across sections)
        courseService.publishCourse(instructor.getUserId(), course1.getCourseId());

        // Seed a free course
        Course freeCourse = courseService.createCourse(
                instructor.getUserId(),
                "Python for Data Science Fundamentals",
                "Learn Python basics and NumPy completely free.",
                Course.Category.DATA_SCIENCE,
                Course.Level.BEGINNER,
                0.0
        );
        Section freeSec = courseService.addSection(instructor.getUserId(), freeCourse.getCourseId(), "Getting Started", 1);
        courseService.addLesson(instructor.getUserId(), freeCourse.getCourseId(), freeSec.getSectionId(), "Python Setup", Lesson.LessonType.VIDEO, 10, 1);
        courseService.addLesson(instructor.getUserId(), freeCourse.getCourseId(), freeSec.getSectionId(), "Data Types & Loops", Lesson.LessonType.READING, 15, 2);
        courseService.addLesson(instructor.getUserId(), freeCourse.getCourseId(), freeSec.getSectionId(), "Basic Python Quiz", Lesson.LessonType.QUIZ, 20, 3);
        courseService.publishCourse(instructor.getUserId(), freeCourse.getCourseId());
    }
}
