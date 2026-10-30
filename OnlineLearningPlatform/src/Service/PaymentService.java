package Service;

import Models.Course;
import Models.Enrollment;
import Models.Payment;
import Repository.CourseRepository;
import Repository.DataStore;
import Repository.EnrollmentRepository;
import Repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EarningService earningService;

    public PaymentService(PaymentRepository paymentRepository,
                          EnrollmentRepository enrollmentRepository,
                          CourseRepository courseRepository,
                          EarningService earningService) {
        if (paymentRepository == null || enrollmentRepository == null ||
            courseRepository == null || earningService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.earningService = earningService;
    }

    public Payment processPayment(String studentId, String enrollmentId, Payment.PaymentMethod method, boolean simulateSuccess) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment not found: " + enrollmentId);
        }
        if (!enrollment.getStudentId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalStateException("Student " + studentId + " is not authorized for enrollment " + enrollmentId);
        }

        // Check if already active or completed
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException("Enrollment is already ACTIVE.");
        }
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Enrollment is already COMPLETED.");
        }
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("Enrollment has been CANCELLED.");
        }

        LocalDateTime now = DataStore.getCurrentTime();

        // Rule 7 & 8: Pending payment expires 24 hours after enrollment. Retries must not reset expiry time.
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.EXPIRED ||
            now.isAfter(enrollment.getEnrollmentDate().plusHours(24))) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.EXPIRED);
            enrollmentRepository.update(enrollment);
            throw new IllegalStateException("Payment window expired: 24 hours have elapsed since enrollment on " + enrollment.getEnrollmentDate());
        }

        Course course = courseRepository.findById(enrollment.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + enrollment.getCourseId());
        }

        double amount = enrollment.getEnrolledPrice();
        String paymentId = DataStore.generatePaymentId();

        if (simulateSuccess) {
            // Rule 5: Check 5 active courses limit before activating
            int activeCount = 0;
            ArrayList<Enrollment> studentEnrollments = enrollmentRepository.findByStudent(studentId);
            for (int i = 0; i < studentEnrollments.size(); i++) {
                Enrollment e = studentEnrollments.get(i);
                if (e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE && e.getProgressPercentage() < 100.0) {
                    activeCount++;
                }
            }
            if (activeCount >= 5) {
                throw new IllegalStateException("Cannot activate enrollment: Student already has 5 active courses in progress.");
            }

            // Create PAID payment
            Payment payment = new Payment(paymentId, enrollmentId, amount, now, method, Payment.PaymentStatus.PAID);
            paymentRepository.save(payment);

            // Activate enrollment
            enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
            enrollmentRepository.update(enrollment);

            // Create pending instructor earning
            earningService.createPendingEarning(
                    enrollmentId,
                    course.getCourseId(),
                    course.getInstructorId(),
                    amount,
                    enrollment.getEnrollmentDate()
            );

            return payment;
        } else {
            // Rule 6: Failed payments must not activate enrollments; leaves it pending payment
            Payment payment = new Payment(paymentId, enrollmentId, amount, now, method, Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            // Enrollment remains PENDING_PAYMENT
            return payment;
        }
    }

    public ArrayList<Payment> getPaymentsByEnrollment(String enrollmentId) {
        return paymentRepository.findByEnrollment(enrollmentId);
    }

    public ArrayList<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public ArrayList<Payment> getFailedOrPendingPayments() {
        ArrayList<Payment> all = paymentRepository.findAll();
        ArrayList<Payment> result = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            Payment p = all.get(i);
            if (p.getStatus() == Payment.PaymentStatus.FAILED) {
                result.add(p);
            }
        }
        return result;
    }
}
