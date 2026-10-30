package Models;

import java.time.LocalDateTime;

public class Payment {
    public enum PaymentMethod {
        UPI,
        CARD,
        NET_BANKING
    }

    public enum PaymentStatus {
        PAID,
        FAILED,
        REFUNDED
    }

    private String paymentId;
    private String enrollmentId;
    private double amount;
    private LocalDateTime paymentDate;
    private PaymentMethod method;
    private PaymentStatus status;

    public Payment(String paymentId, String enrollmentId, double amount, LocalDateTime paymentDate,
                   PaymentMethod method, PaymentStatus status) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty.");
        }
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (amount < 0.0) {
            throw new IllegalArgumentException("Payment amount cannot be negative.");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Payment status cannot be null.");
        }
        this.paymentId = paymentId.trim();
        this.enrollmentId = enrollmentId.trim();
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
        this.method = method;
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty.");
        }
        this.paymentId = paymentId.trim();
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        this.enrollmentId = enrollmentId.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0.0) {
            throw new IllegalArgumentException("Payment amount cannot be negative.");
        }
        this.amount = Math.round(amount * 100.0) / 100.0;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Payment status cannot be null.");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + paymentId + '\'' +
                ", enrollmentId='" + enrollmentId + '\'' +
                ", amount=" + amount +
                ", method=" + method +
                ", status=" + status +
                ", date=" + paymentDate +
                '}';
    }
}
