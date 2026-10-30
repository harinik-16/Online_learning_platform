import Models.*;
import Repository.*;
import Service.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestVerification {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     RUNNING COMPREHENSIVE BUSINESS RULE VERIFICATION SUITE   ");
        System.out.println("===============================================================");

        testModule1CourseCreation();
        testModule2Enrollment();
        testModule3LearningProgress();
        testModule4PaymentsAndRefunds();
        testModule5ReviewsAndCertificates();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("===============================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> " + message);
            testsFailed++;
        }
    }

    private static void testModule1CourseCreation() {
        System.out.println("\n--- Testing Module 1: Course Creation ---");
        DataStore.clearAllData();

        UserRepository userRepo = new UserRepository();
        CourseRepository courseRepo = new CourseRepository();
        EnrollmentRepository enrRepo = new EnrollmentRepository();
        PaymentRepository payRepo = new PaymentRepository();
        EarningService earnSvc = new EarningService(payRepo);
        RefundService refundSvc = new RefundService(payRepo, enrRepo, courseRepo, earnSvc);
        CourseService courseSvc = new CourseService(courseRepo, enrRepo, userRepo, refundSvc);

        User inst = userRepo.save(new User(DataStore.generateUserId(), "Inst 1", "inst1@lms.com", "pass", User.Role.INSTRUCTOR));
        User stud = userRepo.save(new User(DataStore.generateUserId(), "Stud 1", "stud1@lms.com", "pass", User.Role.STUDENT));

        // 1. Student cannot create course
        boolean studentBlocked = false;
        try {
            courseSvc.createCourse(stud.getUserId(), "Java", "Desc", Course.Category.PROGRAMMING, Course.Level.BEGINNER, 50.0);
        } catch (IllegalStateException e) {
            studentBlocked = true;
        }
        assertTrue("Rule 1: Student cannot create course", studentBlocked, "Expected IllegalStateException for student creating course");

        // 2. Instructor can create course with default DRAFT status
        Course c1 = courseSvc.createCourse(inst.getUserId(), "Java Basics", "Desc", Course.Category.PROGRAMMING, Course.Level.BEGINNER, 100.0);
        assertTrue("Rule 2: Course default status is DRAFT", c1.getStatus() == Course.Status.DRAFT, "Status should be DRAFT");

        // 3. Cannot publish without at least 1 section and 3 lessons
        boolean pubBlocked1 = false;
        try {
            courseSvc.publishCourse(inst.getUserId(), c1.getCourseId());
        } catch (IllegalStateException e) {
            pubBlocked1 = true;
        }
        assertTrue("Rule 6: Cannot publish without section and 3 lessons", pubBlocked1, "Expected publication block");

        Section s1 = courseSvc.addSection(inst.getUserId(), c1.getCourseId(), "Section 1", 1);
        courseSvc.addLesson(inst.getUserId(), c1.getCourseId(), s1.getSectionId(), "L1", Lesson.LessonType.VIDEO, 10, 1);
        courseSvc.addLesson(inst.getUserId(), c1.getCourseId(), s1.getSectionId(), "L2", Lesson.LessonType.READING, 10, 2);

        boolean pubBlocked2 = false;
        try {
            courseSvc.publishCourse(inst.getUserId(), c1.getCourseId());
        } catch (IllegalStateException e) {
            pubBlocked2 = true;
        }
        assertTrue("Rule 6: Cannot publish with only 2 lessons", pubBlocked2, "Must have at least 3 lessons");

        courseSvc.addLesson(inst.getUserId(), c1.getCourseId(), s1.getSectionId(), "L3", Lesson.LessonType.QUIZ, 15, 3);
        courseSvc.publishCourse(inst.getUserId(), c1.getCourseId());
        assertTrue("Rule 6: Course published with 1 section and 3 lessons", c1.getStatus() == Course.Status.PUBLISHED, "Course should be PUBLISHED");

        // 4. Category and level cannot be changed after publication
        boolean catLevelBlocked = false;
        try {
            courseSvc.updateCategoryAndLevel(inst.getUserId(), c1.getCourseId(), Course.Category.BUSINESS, Course.Level.ADVANCED);
        } catch (IllegalStateException e) {
            catLevelBlocked = true;
        }
        assertTrue("Rule 7: Category and Level cannot change after publication", catLevelBlocked, "Expected IllegalStateException");

        // 5. Title and description can be edited after publication
        courseSvc.editCourseDetails(inst.getUserId(), c1.getCourseId(), "Java Advanced & Basics", "New Desc");
        assertTrue("Rule 8: Title & Description editable after publication",
                c1.getTitle().equals("Java Advanced & Basics") && c1.getDescription().equals("New Desc"), "Title not updated");
    }

    private static void testModule2Enrollment() {
        System.out.println("\n--- Testing Module 2: Enrollment ---");
        DataStore.clearAllData();

        UserRepository userRepo = new UserRepository();
        CourseRepository courseRepo = new CourseRepository();
        EnrollmentRepository enrRepo = new EnrollmentRepository();
        PaymentRepository payRepo = new PaymentRepository();
        EarningService earnSvc = new EarningService(payRepo);
        RefundService refundSvc = new RefundService(payRepo, enrRepo, courseRepo, earnSvc);
        CourseService courseSvc = new CourseService(courseRepo, enrRepo, userRepo, refundSvc);
        EnrollmentService enrSvc = new EnrollmentService(enrRepo, courseRepo, userRepo);

        User inst = userRepo.save(new User(DataStore.generateUserId(), "Inst", "inst@lms.com", "pass", User.Role.INSTRUCTOR));
        User stud = userRepo.save(new User(DataStore.generateUserId(), "Stud", "stud@lms.com", "pass", User.Role.STUDENT));

        // Draft course cannot be enrolled
        Course draftCourse = courseSvc.createCourse(inst.getUserId(), "Draft", "Desc", Course.Category.DESIGN, Course.Level.BEGINNER, 50.0);
        boolean draftEnrollBlocked = false;
        try {
            enrSvc.enrollStudent(stud.getUserId(), draftCourse.getCourseId());
        } catch (IllegalStateException e) {
            draftEnrollBlocked = true;
        }
        assertTrue("Rule 1: Cannot enroll in unpublished draft course", draftEnrollBlocked, "Expected block for draft course");

        // Setup published course
        Section sec = courseSvc.addSection(inst.getUserId(), draftCourse.getCourseId(), "Sec 1", 1);
        courseSvc.addLesson(inst.getUserId(), draftCourse.getCourseId(), sec.getSectionId(), "L1", Lesson.LessonType.VIDEO, 10, 1);
        courseSvc.addLesson(inst.getUserId(), draftCourse.getCourseId(), sec.getSectionId(), "L2", Lesson.LessonType.READING, 10, 2);
        courseSvc.addLesson(inst.getUserId(), draftCourse.getCourseId(), sec.getSectionId(), "L3", Lesson.LessonType.QUIZ, 10, 3);
        courseSvc.publishCourse(inst.getUserId(), draftCourse.getCourseId());

        // Instructor cannot enroll in their own course
        boolean instSelfEnrollBlocked = false;
        try {
            enrSvc.enrollStudent(inst.getUserId(), draftCourse.getCourseId());
        } catch (IllegalStateException e) {
            instSelfEnrollBlocked = true;
        }
        assertTrue("Rule 3: Instructor cannot enroll in own course", instSelfEnrollBlocked, "Expected block for instructor self-enrollment");

        // Paid course starts with PENDING_PAYMENT and stores enrolledPrice
        Enrollment paidEnr = enrSvc.enrollStudent(stud.getUserId(), draftCourse.getCourseId());
        assertTrue("Rule 5 & 11: Paid course starts as PENDING_PAYMENT with price",
                paidEnr.getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT && paidEnr.getEnrolledPrice() == 50.0,
                "Status should be PENDING_PAYMENT");

        // Duplicate enrollment prevented
        boolean dupBlocked = false;
        try {
            enrSvc.enrollStudent(stud.getUserId(), draftCourse.getCourseId());
        } catch (IllegalStateException e) {
            dupBlocked = true;
        }
        assertTrue("Rule 2: Duplicate enrollment blocked", dupBlocked, "Expected block for duplicate enrollment");

        // Free course becomes ACTIVE immediately
        Course freeCourse = courseSvc.createCourse(inst.getUserId(), "Free Course", "Desc", Course.Category.BUSINESS, Course.Level.BEGINNER, 0.0);
        Section fSec = courseSvc.addSection(inst.getUserId(), freeCourse.getCourseId(), "Sec 1", 1);
        courseSvc.addLesson(inst.getUserId(), freeCourse.getCourseId(), fSec.getSectionId(), "FL1", Lesson.LessonType.VIDEO, 10, 1);
        courseSvc.addLesson(inst.getUserId(), freeCourse.getCourseId(), fSec.getSectionId(), "FL2", Lesson.LessonType.READING, 10, 2);
        courseSvc.addLesson(inst.getUserId(), freeCourse.getCourseId(), fSec.getSectionId(), "FL3", Lesson.LessonType.QUIZ, 10, 3);
        courseSvc.publishCourse(inst.getUserId(), freeCourse.getCourseId());

        Enrollment freeEnr = enrSvc.enrollStudent(stud.getUserId(), freeCourse.getCourseId());
        assertTrue("Rule 4: Free course becomes ACTIVE immediately", freeEnr.getStatus() == Enrollment.EnrollmentStatus.ACTIVE, "Expected ACTIVE");

        // Test 5 active course limit
        for (int i = 1; i <= 4; i++) {
            Course c = courseSvc.createCourse(inst.getUserId(), "Extra Course " + i, "Desc", Course.Category.LANGUAGE, Course.Level.BEGINNER, 0.0);
            Section s = courseSvc.addSection(inst.getUserId(), c.getCourseId(), "S1", 1);
            courseSvc.addLesson(inst.getUserId(), c.getCourseId(), s.getSectionId(), "L1", Lesson.LessonType.VIDEO, 10, 1);
            courseSvc.addLesson(inst.getUserId(), c.getCourseId(), s.getSectionId(), "L2", Lesson.LessonType.READING, 10, 2);
            courseSvc.addLesson(inst.getUserId(), c.getCourseId(), s.getSectionId(), "L3", Lesson.LessonType.QUIZ, 10, 3);
            courseSvc.publishCourse(inst.getUserId(), c.getCourseId());
            enrSvc.enrollStudent(stud.getUserId(), c.getCourseId());
        }

        // Student now has 5 ACTIVE courses (freeEnr + 4 extra free courses). The 6th should be rejected.
        Course c6 = courseSvc.createCourse(inst.getUserId(), "Course 6", "Desc", Course.Category.BUSINESS, Course.Level.BEGINNER, 0.0);
        Section s6 = courseSvc.addSection(inst.getUserId(), c6.getCourseId(), "S1", 1);
        courseSvc.addLesson(inst.getUserId(), c6.getCourseId(), s6.getSectionId(), "L1", Lesson.LessonType.VIDEO, 10, 1);
        courseSvc.addLesson(inst.getUserId(), c6.getCourseId(), s6.getSectionId(), "L2", Lesson.LessonType.READING, 10, 2);
        courseSvc.addLesson(inst.getUserId(), c6.getCourseId(), s6.getSectionId(), "L3", Lesson.LessonType.QUIZ, 10, 3);
        courseSvc.publishCourse(inst.getUserId(), c6.getCourseId());

        boolean limitBlocked = false;
        try {
            enrSvc.enrollStudent(stud.getUserId(), c6.getCourseId());
        } catch (IllegalStateException e) {
            limitBlocked = true;
        }
        assertTrue("Rule 9: Maximum 5 active courses in progress limit enforced", limitBlocked, "Expected block at 5 active courses");

        // Price change does not affect existing enrollment
        draftCourse.setPrice(199.99);
        assertTrue("Rule 10 & 11: Price change affects only new enrollments", paidEnr.getEnrolledPrice() == 50.0, "Enrolled price was altered!");

        // Pending payment expiration after 24 hours
        DataStore.advanceHours(25);
        boolean expired = enrSvc.checkAndExpirePendingEnrollment(paidEnr);
        assertTrue("Rule 7: Pending payment expires after 24 hours",
                expired && paidEnr.getStatus() == Enrollment.EnrollmentStatus.EXPIRED, "Enrollment did not expire");
    }

    private static void testModule3LearningProgress() {
        System.out.println("\n--- Testing Module 3: Learning Progress ---");
        DataStore.clearAllData();

        UserRepository userRepo = new UserRepository();
        CourseRepository courseRepo = new CourseRepository();
        EnrollmentRepository enrRepo = new EnrollmentRepository();
        PaymentRepository payRepo = new PaymentRepository();
        EarningService earnSvc = new EarningService(payRepo);
        RefundService refundSvc = new RefundService(payRepo, enrRepo, courseRepo, earnSvc);
        CourseService courseSvc = new CourseService(courseRepo, enrRepo, userRepo, refundSvc);
        EnrollmentService enrSvc = new EnrollmentService(enrRepo, courseRepo, userRepo);
        CertificateRepository certRepo = new CertificateRepository();
        CertificateService certSvc = new CertificateService(certRepo, enrRepo);
        ProgressService progSvc = new ProgressService(enrRepo, courseRepo, certSvc);

        User inst = userRepo.save(new User(DataStore.generateUserId(), "Inst", "inst@lms.com", "pass", User.Role.INSTRUCTOR));
        User stud = userRepo.save(new User(DataStore.generateUserId(), "Stud", "stud@lms.com", "pass", User.Role.STUDENT));

        Course course = courseSvc.createCourse(inst.getUserId(), "Full Course", "Desc", Course.Category.PROGRAMMING, Course.Level.BEGINNER, 0.0);
        Section s1 = courseSvc.addSection(inst.getUserId(), course.getCourseId(), "Sec 1", 1);
        Lesson l1 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "Video 1", Lesson.LessonType.VIDEO, 10, 1);
        Lesson l2 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "Reading 1", Lesson.LessonType.READING, 10, 2);
        Lesson l3 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "Quiz 1", Lesson.LessonType.QUIZ, 10, 3);
        courseSvc.publishCourse(inst.getUserId(), course.getCourseId());

        Enrollment enr = enrSvc.enrollStudent(stud.getUserId(), course.getCourseId());

        // 1. Sequential ordering: cannot complete L2 before L1
        boolean seqBlocked = false;
        try {
            progSvc.completeReadingLesson(stud.getUserId(), enr.getEnrollmentId(), l2.getLessonId(), true);
        } catch (IllegalStateException e) {
            seqBlocked = true;
        }
        assertTrue("Rule 2 & 3: Sequential ordering enforced (cannot skip L1)", seqBlocked, "Expected block for out of order lesson");

        // 2. Video lesson requires at least 90% watch
        boolean videoBlocked = false;
        try {
            progSvc.completeVideoLesson(stud.getUserId(), enr.getEnrollmentId(), l1.getLessonId(), 85.0);
        } catch (IllegalArgumentException e) {
            videoBlocked = true;
        }
        assertTrue("Rule 4: Video requires >= 90% watch percentage", videoBlocked, "Expected block for 85% watch");

        progSvc.completeVideoLesson(stud.getUserId(), enr.getEnrollmentId(), l1.getLessonId(), 95.0);
        assertTrue("Rule 4 & 10: Video completes at 95% and progress recalculated",
                enr.getProgressPercentage() == 33.3, "Progress should be 33.3%");

        // 3. Complete reading lesson
        progSvc.completeReadingLesson(stud.getUserId(), enr.getEnrollmentId(), l2.getLessonId(), true);
        assertTrue("Rule 5 & 10: Reading lesson completes with confirmation",
                enr.getProgressPercentage() == 66.7, "Progress should be 66.7%");

        // 4. Quiz requires >= 60% to pass
        QuizAttempt failAttempt = progSvc.attemptQuizLesson(stud.getUserId(), enr.getEnrollmentId(), l3.getLessonId(), 50.0);
        assertTrue("Rule 6: Quiz score 50% fails", !failAttempt.isPassed(), "50% should fail");

        // Attempt 2 and Attempt 3 fails
        progSvc.attemptQuizLesson(stud.getUserId(), enr.getEnrollmentId(), l3.getLessonId(), 40.0);
        progSvc.attemptQuizLesson(stud.getUserId(), enr.getEnrollmentId(), l3.getLessonId(), 30.0);

        // Rule 7 & 9: 4th attempt blocked due to 24h cooldown after 3 failed attempts
        boolean cooldownBlocked = false;
        try {
            progSvc.attemptQuizLesson(stud.getUserId(), enr.getEnrollmentId(), l3.getLessonId(), 80.0);
        } catch (IllegalStateException e) {
            cooldownBlocked = true;
        }
        assertTrue("Rule 7 & 9: 4th quiz attempt blocked before 24-hour cooldown ends", cooldownBlocked, "Expected 24-hour cooldown lock");

        // Advance 25 hours
        DataStore.advanceHours(25);

        // After cooldown, attempt cycle resumes and passing with 90% completes enrollment!
        QuizAttempt passAttempt = progSvc.attemptQuizLesson(stud.getUserId(), enr.getEnrollmentId(), l3.getLessonId(), 90.0);
        assertTrue("Rule 9: Attempt allowed after cooldown ends", passAttempt.isPassed(), "Expected pass after cooldown");
        assertTrue("Rule 10 & 12: Enrollment marked COMPLETED upon 100% progress",
                enr.getStatus() == Enrollment.EnrollmentStatus.COMPLETED && enr.getProgressPercentage() == 100.0,
                "Expected status COMPLETED and 100% progress");

        // Rule 13: Adding new lessons after completion preserves existing COMPLETED enrollment
        courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "Extra Lesson", Lesson.LessonType.READING, 5, 4);
        assertTrue("Rule 13: Adding lesson after completion preserves COMPLETED status",
                enr.getStatus() == Enrollment.EnrollmentStatus.COMPLETED && enr.getProgressPercentage() == 100.0,
                "Completed status was downgraded");
    }

    private static void testModule4PaymentsAndRefunds() {
        System.out.println("\n--- Testing Module 4: Payments and Refunds ---");
        DataStore.clearAllData();

        UserRepository userRepo = new UserRepository();
        CourseRepository courseRepo = new CourseRepository();
        EnrollmentRepository enrRepo = new EnrollmentRepository();
        PaymentRepository payRepo = new PaymentRepository();
        EarningService earnSvc = new EarningService(payRepo);
        RefundService refundSvc = new RefundService(payRepo, enrRepo, courseRepo, earnSvc);
        CourseService courseSvc = new CourseService(courseRepo, enrRepo, userRepo, refundSvc);
        EnrollmentService enrSvc = new EnrollmentService(enrRepo, courseRepo, userRepo);
        PaymentService paySvc = new PaymentService(payRepo, enrRepo, courseRepo, earnSvc);
        CertificateRepository certRepo = new CertificateRepository();
        CertificateService certSvc = new CertificateService(certRepo, enrRepo);
        ProgressService progSvc = new ProgressService(enrRepo, courseRepo, certSvc);

        User inst = userRepo.save(new User(DataStore.generateUserId(), "Inst", "inst@lms.com", "pass", User.Role.INSTRUCTOR));
        User admin = userRepo.save(new User(DataStore.generateUserId(), "Admin", "admin@lms.com", "pass", User.Role.ADMIN));
        User stud = userRepo.save(new User(DataStore.generateUserId(), "Stud", "stud@lms.com", "pass", User.Role.STUDENT));

        Course course = courseSvc.createCourse(inst.getUserId(), "Paid Course", "Desc", Course.Category.BUSINESS, Course.Level.BEGINNER, 100.0);
        Section s1 = courseSvc.addSection(inst.getUserId(), course.getCourseId(), "Sec 1", 1);
        Lesson l1 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "L1", Lesson.LessonType.VIDEO, 10, 1);
        courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "L2", Lesson.LessonType.READING, 10, 2);
        courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "L3", Lesson.LessonType.QUIZ, 10, 3);
        courseSvc.publishCourse(inst.getUserId(), course.getCourseId());

        Enrollment enr = enrSvc.enrollStudent(stud.getUserId(), course.getCourseId());

        // Failed payment does not activate enrollment
        Payment failPay = paySvc.processPayment(stud.getUserId(), enr.getEnrollmentId(), Payment.PaymentMethod.UPI, false);
        assertTrue("Rule 6: Failed payment leaves enrollment PENDING_PAYMENT",
                failPay.getStatus() == Payment.PaymentStatus.FAILED && enr.getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT,
                "Expected FAILED payment and PENDING enrollment");

        // Successful payment activates enrollment and creates 70/30 instructor earning
        Payment successPay = paySvc.processPayment(stud.getUserId(), enr.getEnrollmentId(), Payment.PaymentMethod.CARD, true);
        assertTrue("Rule 5: Successful payment activates enrollment",
                successPay.getStatus() == Payment.PaymentStatus.PAID && enr.getStatus() == Enrollment.EnrollmentStatus.ACTIVE,
                "Expected PAID payment and ACTIVE enrollment");

        InstructorEarning earning = payRepo.findEarningByEnrollment(enr.getEnrollmentId());
        assertTrue("Rule 13 & 14: Instructor share is 70% and remains pending",
                earning != null && earning.getInstructorShare() == 70.0 && earning.getPlatformShare() == 30.0 && !earning.isCredited(),
                "Expected 70/30 split and pending status");

        // Refund rejected if progress >= 20%
        progSvc.completeVideoLesson(stud.getUserId(), enr.getEnrollmentId(), l1.getLessonId(), 95.0); // 33.3% progress
        boolean refundProgressBlocked = false;
        try {
            refundSvc.requestStudentRefund(stud.getUserId(), enr.getEnrollmentId());
        } catch (IllegalStateException e) {
            refundProgressBlocked = true;
        }
        assertTrue("Rule 7 & 8: Refund rejected when progress is >= 20%", refundProgressBlocked, "Refund should be rejected at 33.3% progress");

        // Reset for valid refund test: new student with < 20% progress
        User stud2 = userRepo.save(new User(DataStore.generateUserId(), "Stud 2", "stud2@lms.com", "pass", User.Role.STUDENT));
        Enrollment enr2 = enrSvc.enrollStudent(stud2.getUserId(), course.getCourseId());
        paySvc.processPayment(stud2.getUserId(), enr2.getEnrollmentId(), Payment.PaymentMethod.NET_BANKING, true);

        // Within 7 days and 0% progress -> refund succeeds!
        Payment refundedPay = refundSvc.requestStudentRefund(stud2.getUserId(), enr2.getEnrollmentId());
        assertTrue("Rule 7, 11 & 12: Refund succeeds within 7 days & < 20% progress",
                refundedPay.getStatus() == Payment.PaymentStatus.REFUNDED && enr2.getStatus() == Enrollment.EnrollmentStatus.CANCELLED,
                "Payment not refunded or enrollment not cancelled");

        // Admin withdrawal triggers full refunds
        User stud3 = userRepo.save(new User(DataStore.generateUserId(), "Stud 3", "stud3@lms.com", "pass", User.Role.STUDENT));
        Enrollment enr3 = enrSvc.enrollStudent(stud3.getUserId(), course.getCourseId());
        paySvc.processPayment(stud3.getUserId(), enr3.getEnrollmentId(), Payment.PaymentMethod.UPI, true);

        // Advance 10 days (past normal 7-day refund window)
        DataStore.advanceDays(10);

        // Admin withdraws course -> triggers full refund even past 7 days!
        courseSvc.withdrawCourse(admin.getUserId(), course.getCourseId());
        Payment withdrawnPay = payRepo.findSuccessfulPaymentByEnrollment(enr3.getEnrollmentId());
        ArrayList<Payment> allEnr3Pays = payRepo.findByEnrollment(enr3.getEnrollmentId());
        boolean hasRefunded = false;
        for (Payment p : allEnr3Pays) {
            if (p.getStatus() == Payment.PaymentStatus.REFUNDED) hasRefunded = true;
        }
        assertTrue("Rule 10: Course withdrawal triggers full refund regardless of 7-day limit",
                hasRefunded && enr3.getStatus() == Enrollment.EnrollmentStatus.CANCELLED,
                "Expected course withdrawal refund");
    }

    private static void testModule5ReviewsAndCertificates() {
        System.out.println("\n--- Testing Module 5: Reviews and Certificates ---");
        DataStore.clearAllData();

        UserRepository userRepo = new UserRepository();
        CourseRepository courseRepo = new CourseRepository();
        EnrollmentRepository enrRepo = new EnrollmentRepository();
        PaymentRepository payRepo = new PaymentRepository();
        EarningService earnSvc = new EarningService(payRepo);
        RefundService refundSvc = new RefundService(payRepo, enrRepo, courseRepo, earnSvc);
        CourseService courseSvc = new CourseService(courseRepo, enrRepo, userRepo, refundSvc);
        EnrollmentService enrSvc = new EnrollmentService(enrRepo, courseRepo, userRepo);
        CertificateRepository certRepo = new CertificateRepository();
        CertificateService certSvc = new CertificateService(certRepo, enrRepo);
        ProgressService progSvc = new ProgressService(enrRepo, courseRepo, certSvc);
        ReviewRepository revRepo = new ReviewRepository();
        ReviewService revSvc = new ReviewService(revRepo, enrRepo, courseRepo);

        User inst = userRepo.save(new User(DataStore.generateUserId(), "Inst", "inst@lms.com", "pass", User.Role.INSTRUCTOR));
        User stud = userRepo.save(new User(DataStore.generateUserId(), "Stud", "stud@lms.com", "pass", User.Role.STUDENT));

        Course course = courseSvc.createCourse(inst.getUserId(), "Review Course", "Desc", Course.Category.LANGUAGE, Course.Level.BEGINNER, 0.0);
        Section s1 = courseSvc.addSection(inst.getUserId(), course.getCourseId(), "Sec 1", 1);
        Lesson l1 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "L1", Lesson.LessonType.VIDEO, 10, 1);
        Lesson l2 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "L2", Lesson.LessonType.READING, 10, 2);
        Lesson l3 = courseSvc.addLesson(inst.getUserId(), course.getCourseId(), s1.getSectionId(), "L3", Lesson.LessonType.QUIZ, 10, 3);
        courseSvc.publishCourse(inst.getUserId(), course.getCourseId());

        Enrollment enr = enrSvc.enrollStudent(stud.getUserId(), course.getCourseId());

        // 1. Review rejected if progress < 25%
        boolean revBlocked = false;
        try {
            revSvc.submitReview(stud.getUserId(), course.getCourseId(), 5, "Great!");
        } catch (IllegalStateException e) {
            revBlocked = true;
        }
        assertTrue("Rule 1: Review rejected if progress < 25%", revBlocked, "Expected review block at 0% progress");

        // Complete lesson 1 (gives 33.3% progress)
        progSvc.completeVideoLesson(stud.getUserId(), enr.getEnrollmentId(), l1.getLessonId(), 95.0);

        // 2. Review succeeds with rating 5
        Review rev = revSvc.submitReview(stud.getUserId(), course.getCourseId(), 5, "Great course!");
        assertTrue("Rule 1 & 6: Review submitted and course rating updated",
                rev != null && course.getAverageRating() == 5.0 && course.getTotalRatings() == 1,
                "Expected 5.0 average rating");

        // 3. Edit review within 30 days
        Review editedRev = revSvc.editReview(stud.getUserId(), rev.getReviewId(), 4, "Updated comment");
        assertTrue("Rule 4 & 5: Review edited and course rating recalculated",
                editedRev.getRating() == 4 && course.getAverageRating() == 4.0,
                "Expected updated rating of 4.0");

        // 4. Edit review rejected after 30 days
        DataStore.advanceDays(31);
        boolean editBlocked = false;
        try {
            revSvc.editReview(stud.getUserId(), rev.getReviewId(), 3, "Too late");
        } catch (IllegalStateException e) {
            editBlocked = true;
        }
        assertTrue("Rule 4: Review editing rejected after 30 days", editBlocked, "Expected block after 30 days");

        // 5. Test flagging when >= 20 reviews and avg < 2.5
        for (int i = 1; i <= 20; i++) {
            User reviewer = userRepo.save(new User(DataStore.generateUserId(), "Rev " + i, "rev" + i + "@lms.com", "pass", User.Role.STUDENT));
            Enrollment rEnr = enrSvc.enrollStudent(reviewer.getUserId(), course.getCourseId());
            progSvc.completeVideoLesson(reviewer.getUserId(), rEnr.getEnrollmentId(), l1.getLessonId(), 95.0);
            revSvc.submitReview(reviewer.getUserId(), course.getCourseId(), 2, "Poor quality");
        }
        assertTrue("Rule 8: Course flagged for review when >= 20 ratings & avg < 2.5",
                course.isFlaggedForReview() && course.getAverageRating() < 2.5,
                "Course was not flagged for review");

        // 6. Certificate issued upon completion
        progSvc.completeReadingLesson(stud.getUserId(), enr.getEnrollmentId(), l2.getLessonId(), true);
        progSvc.attemptQuizLesson(stud.getUserId(), enr.getEnrollmentId(), l3.getLessonId(), 100.0);

        Certificate cert = certSvc.getCertificateByEnrollment(enr.getEnrollmentId());
        assertTrue("Certificate Rule 1, 2 & 3: Certificate issued for completed enrollment with unique ID",
                cert != null && cert.getStudentId().equals(stud.getUserId()) && cert.getCertificateId() != null,
                "Certificate was not issued");
    }
}
