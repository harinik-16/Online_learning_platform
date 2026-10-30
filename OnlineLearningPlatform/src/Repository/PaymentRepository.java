package Repository;

import Models.InstructorEarning;
import Models.Payment;
import java.util.ArrayList;

public class PaymentRepository {

    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null.");
        }
        if (payment.getPaymentId() == null || payment.getPaymentId().trim().isEmpty()) {
            payment.setPaymentId(DataStore.generatePaymentId());
        }
        DataStore.payments.add(payment);
        return payment;
    }

    public Payment findById(String paymentId) {
        if (paymentId == null) return null;
        for (int i = 0; i < DataStore.payments.size(); i++) {
            Payment p = DataStore.payments.get(i);
            if (p.getPaymentId().equalsIgnoreCase(paymentId.trim())) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Payment> findAll() {
        return new ArrayList<>(DataStore.payments);
    }

    public ArrayList<Payment> findByEnrollment(String enrollmentId) {
        ArrayList<Payment> result = new ArrayList<>();
        if (enrollmentId == null) return result;
        for (int i = 0; i < DataStore.payments.size(); i++) {
            Payment p = DataStore.payments.get(i);
            if (p.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim())) {
                result.add(p);
            }
        }
        return result;
    }

    public Payment findSuccessfulPaymentByEnrollment(String enrollmentId) {
        if (enrollmentId == null) return null;
        for (int i = 0; i < DataStore.payments.size(); i++) {
            Payment p = DataStore.payments.get(i);
            if (p.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim()) &&
                p.getStatus() == Payment.PaymentStatus.PAID) {
                return p;
            }
        }
        return null;
    }

    public boolean update(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) return false;
        for (int i = 0; i < DataStore.payments.size(); i++) {
            if (DataStore.payments.get(i).getPaymentId().equals(payment.getPaymentId())) {
                DataStore.payments.set(i, payment);
                return true;
            }
        }
        return false;
    }

    // --- Instructor Earning Operations ---

    public InstructorEarning saveEarning(InstructorEarning earning) {
        if (earning == null) {
            throw new IllegalArgumentException("Earning cannot be null.");
        }
        if (earning.getEarningId() == null || earning.getEarningId().trim().isEmpty()) {
            earning.setEarningId(DataStore.generateEarningId());
        }
        DataStore.instructorEarnings.add(earning);
        return earning;
    }

    public InstructorEarning findEarningById(String earningId) {
        if (earningId == null) return null;
        for (int i = 0; i < DataStore.instructorEarnings.size(); i++) {
            InstructorEarning ie = DataStore.instructorEarnings.get(i);
            if (ie.getEarningId().equalsIgnoreCase(earningId.trim())) {
                return ie;
            }
        }
        return null;
    }

    public ArrayList<InstructorEarning> findEarningsByInstructor(String instructorId) {
        ArrayList<InstructorEarning> result = new ArrayList<>();
        if (instructorId == null) return result;
        for (int i = 0; i < DataStore.instructorEarnings.size(); i++) {
            InstructorEarning ie = DataStore.instructorEarnings.get(i);
            if (ie.getInstructorId().equalsIgnoreCase(instructorId.trim())) {
                result.add(ie);
            }
        }
        return result;
    }

    public InstructorEarning findEarningByEnrollment(String enrollmentId) {
        if (enrollmentId == null) return null;
        for (int i = 0; i < DataStore.instructorEarnings.size(); i++) {
            InstructorEarning ie = DataStore.instructorEarnings.get(i);
            if (ie.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim())) {
                return ie;
            }
        }
        return null;
    }

    public ArrayList<InstructorEarning> findAllEarnings() {
        return new ArrayList<>(DataStore.instructorEarnings);
    }

    public boolean updateEarning(InstructorEarning earning) {
        if (earning == null || earning.getEarningId() == null) return false;
        for (int i = 0; i < DataStore.instructorEarnings.size(); i++) {
            if (DataStore.instructorEarnings.get(i).getEarningId().equals(earning.getEarningId())) {
                DataStore.instructorEarnings.set(i, earning);
                return true;
            }
        }
        return false;
    }

    public boolean removeEarning(String earningId) {
        if (earningId == null) return false;
        for (int i = 0; i < DataStore.instructorEarnings.size(); i++) {
            if (DataStore.instructorEarnings.get(i).getEarningId().equalsIgnoreCase(earningId.trim())) {
                DataStore.instructorEarnings.remove(i);
                return true;
            }
        }
        return false;
    }
}
