package Service;

import Models.Course;
import Models.Enrollment;
import Models.User;
import Repository.CourseRepository;
import Repository.DataStore;
import Repository.EnrollmentRepository;
import Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository) {
        if (enrollmentRepository == null || courseRepository == null || userRepository == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Enrollment enrollStudent(String studentId, String courseId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }

        User student = userRepository.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }

        // Rule 1: A student can enroll only in a PUBLISHED course
        if (course.getStatus() != Course.Status.PUBLISHED) {
            throw new IllegalStateException("Cannot enroll: Course is not published (Current status: " + course.getStatus() + ").");
        }

        // Rule 3: An instructor cannot enroll in their own course
        if (course.getInstructorId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalStateException("Instructors cannot enroll in their own course.");
        }

        // Rule 2: A student can enroll only once in the same course. Prevent duplicate enrollments.
        Enrollment existing = enrollmentRepository.findByStudentAndCourse(studentId, courseId);
        if (existing != null) {
            throw new IllegalStateException("Duplicate enrollment: Student is already enrolled in this course with status " + existing.getStatus());
        }

        // Rule 9 & 10: At most five courses in progress. Count only ACTIVE enrollments not completed.
        // If course is free, it will immediately become ACTIVE, so check now.
        // For paid courses, also check to prevent enrolling if already at limit.
        int activeCount = countActiveInProgressEnrollments(studentId);
        if (activeCount >= 5) {
            throw new IllegalStateException("Enrollment limit reached: Student already has 5 active courses in progress.");
        }

        // Rule 11: Store the course price at enrollment time
        double enrolledPrice = course.getPrice();
        LocalDateTime now = DataStore.getCurrentTime();
        String enrollmentId = DataStore.generateEnrollmentId();

        // Rule 4 & 5: Free courses become ACTIVE immediately; paid courses begin with PENDING_PAYMENT
        Enrollment enrollment = new Enrollment(enrollmentId, studentId, courseId, enrolledPrice, now);
        return enrollmentRepository.save(enrollment);
    }

    public int countActiveInProgressEnrollments(String studentId) {
        ArrayList<Enrollment> list = enrollmentRepository.findByStudent(studentId);
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            Enrollment e = list.get(i);
            // Count only ACTIVE enrollments that are not completed
            if (e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE && e.getProgressPercentage() < 100.0) {
                count++;
            }
        }
        return count;
    }

    public boolean checkAndExpirePendingEnrollment(Enrollment enrollment) {
        if (enrollment == null) return false;
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT) {
            LocalDateTime now = DataStore.getCurrentTime();
            // Rule 7: Pending payment expires 24 hours after enrollment
            if (now.isAfter(enrollment.getEnrollmentDate().plusHours(24))) {
                enrollment.setStatus(Enrollment.EnrollmentStatus.EXPIRED);
                enrollmentRepository.update(enrollment);
                return true;
            }
        }
        return false;
    }

    public void checkAndExpireAllPendingEnrollments() {
        ArrayList<Enrollment> all = enrollmentRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            checkAndExpirePendingEnrollment(all.get(i));
        }
    }

    public Enrollment getEnrollmentById(String enrollmentId) {
        Enrollment enr = enrollmentRepository.findById(enrollmentId);
        if (enr != null) {
            checkAndExpirePendingEnrollment(enr);
        }
        return enr;
    }

    public ArrayList<Enrollment> getEnrollmentsByStudent(String studentId) {
        checkAndExpireAllPendingEnrollments();
        return enrollmentRepository.findByStudent(studentId);
    }

    public ArrayList<Enrollment> getEnrollmentsByCourse(String courseId) {
        checkAndExpireAllPendingEnrollments();
        return enrollmentRepository.findByCourse(courseId);
    }

    public ArrayList<Enrollment> getAllEnrollments() {
        checkAndExpireAllPendingEnrollments();
        return enrollmentRepository.findAll();
    }
}
