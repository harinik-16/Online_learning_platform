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

public class RefundService {
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EarningService earningService;

    public RefundService(PaymentRepository paymentRepository,
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

    public Payment requestStudentRefund(String studentId, String enrollmentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment not found: " + enrollmentId);
        }
        if (!enrollment.getStudentId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalStateException("Student " + studentId + " is not authorized for enrollment " + enrollmentId);
        }

        // Must have paid
        Payment payment = paymentRepository.findSuccessfulPaymentByEnrollment(enrollmentId);
        if (payment == null) {
            throw new IllegalStateException("No successful payment record found to refund for enrollment: " + enrollmentId);
        }
        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Payment has already been refunded.");
        }

        LocalDateTime now = DataStore.getCurrentTime();

        // Rule 9: Reject normal refund requests made after seven days
        if (now.isAfter(enrollment.getEnrollmentDate().plusDays(7))) {
            throw new IllegalStateException("Refund request rejected: 7-day refund window has expired.");
        }

        // Rule 7 & 8: Refund within 7 days only if progress is strictly below 20%.
        // If progress is 20% or greater, reject the normal student refund request.
        if (enrollment.getProgressPercentage() >= 20.0) {
            throw new IllegalStateException("Refund request rejected: Progress is " +
                    enrollment.getProgressPercentage() + "%. Refunds are only allowed for progress strictly below 20%.");
        }

        // Process refund
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.update(payment);

        enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELLED);
        enrollmentRepository.update(enrollment);

        // Cancel pending earning for instructor
        earningService.cancelEarningForEnrollment(enrollmentId);

        return payment;
    }

    public int processWithdrawalRefunds(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }

        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }

        // Rule 10: Admin withdrawing a course must trigger full refunds for all enrolled students
        // with paid, unrefunded payments, regardless of the ordinary seven-day and progress restrictions.
        ArrayList<Enrollment> enrollments = enrollmentRepository.findByCourse(courseId);
        int refundedCount = 0;

        for (int i = 0; i < enrollments.size(); i++) {
            Enrollment enr = enrollments.get(i);
            Payment payment = paymentRepository.findSuccessfulPaymentByEnrollment(enr.getEnrollmentId());

            if (payment != null && payment.getStatus() == Payment.PaymentStatus.PAID) {
                // Prevent duplicate refunds
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                paymentRepository.update(payment);

                enr.setStatus(Enrollment.EnrollmentStatus.CANCELLED);
                enrollmentRepository.update(enr);

                earningService.cancelEarningForEnrollment(enr.getEnrollmentId());
                refundedCount++;
            } else if (enr.getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT) {
                // Cancel pending enrollments as well
                enr.setStatus(Enrollment.EnrollmentStatus.CANCELLED);
                enrollmentRepository.update(enr);
            }
        }

        return refundedCount;
    }
}
