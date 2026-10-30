package Service;

import Models.Enrollment;
import Models.InstructorEarning;
import Repository.DataStore;
import Repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EarningService {
    private final PaymentRepository paymentRepository;

    public EarningService(PaymentRepository paymentRepository) {
        if (paymentRepository == null) {
            throw new IllegalArgumentException("PaymentRepository cannot be null.");
        }
        this.paymentRepository = paymentRepository;
    }

    public InstructorEarning createPendingEarning(String enrollmentId, String courseId, String instructorId,
                                                 double coursePrice, LocalDateTime enrollmentDate) {
        if (enrollmentId == null || courseId == null || instructorId == null) {
            throw new IllegalArgumentException("IDs cannot be null.");
        }
        if (coursePrice <= 0.0) {
            // Free course, no earning generated
            return null;
        }
        // Verify no duplicate earning for this enrollment
        InstructorEarning existing = paymentRepository.findEarningByEnrollment(enrollmentId);
        if (existing != null) {
            return existing;
        }

        InstructorEarning earning = new InstructorEarning(
                DataStore.generateEarningId(),
                instructorId,
                courseId,
                enrollmentId,
                coursePrice,
                enrollmentDate
        );
        return paymentRepository.saveEarning(earning);
    }

    public int processEligibleEarnings(ArrayList<Enrollment> currentEnrollments) {
        LocalDateTime now = DataStore.getCurrentTime();
        ArrayList<InstructorEarning> allEarnings = paymentRepository.findAllEarnings();
        int creditedCount = 0;

        for (int i = 0; i < allEarnings.size(); i++) {
            InstructorEarning earning = allEarnings.get(i);
            if (!earning.isCredited()) {
                // Rule: Must be on or after 7-day eligibleCreditDate
                if (!now.isBefore(earning.getEligibleCreditDate())) {
                    // Rule: Do not credit refunded/cancelled enrollments
                    boolean isCancelledOrRefunded = false;
                    for (int j = 0; j < currentEnrollments.size(); j++) {
                        Enrollment enr = currentEnrollments.get(j);
                        if (enr.getEnrollmentId().equals(earning.getEnrollmentId())) {
                            if (enr.getStatus() == Enrollment.EnrollmentStatus.CANCELLED ||
                                enr.getStatus() == Enrollment.EnrollmentStatus.EXPIRED) {
                                isCancelledOrRefunded = true;
                            }
                            break;
                        }
                    }

                    if (!isCancelledOrRefunded) {
                        earning.setCredited(true);
                        paymentRepository.updateEarning(earning);
                        creditedCount++;
                    }
                }
            }
        }
        return creditedCount;
    }

    public void cancelEarningForEnrollment(String enrollmentId) {
        InstructorEarning earning = paymentRepository.findEarningByEnrollment(enrollmentId);
        if (earning != null) {
            paymentRepository.removeEarning(earning.getEarningId());
        }
    }

    public ArrayList<InstructorEarning> getEarningsByInstructor(String instructorId) {
        return paymentRepository.findEarningsByInstructor(instructorId);
    }

    public ArrayList<InstructorEarning> getAllEarnings() {
        return paymentRepository.findAllEarnings();
    }

    public double getTotalCreditedEarnings(String instructorId) {
        double total = 0.0;
        ArrayList<InstructorEarning> list = getEarningsByInstructor(instructorId);
        for (int i = 0; i < list.size(); i++) {
            InstructorEarning e = list.get(i);
            if (e.isCredited()) {
                total += e.getInstructorShare();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public double getTotalPendingEarnings(String instructorId) {
        double total = 0.0;
        ArrayList<InstructorEarning> list = getEarningsByInstructor(instructorId);
        for (int i = 0; i < list.size(); i++) {
            InstructorEarning e = list.get(i);
            if (!e.isCredited()) {
                total += e.getInstructorShare();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
