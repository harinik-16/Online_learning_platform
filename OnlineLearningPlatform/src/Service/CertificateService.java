package Service;

import Models.Certificate;
import Models.Enrollment;
import Repository.CertificateRepository;
import Repository.DataStore;
import Repository.EnrollmentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CertificateService(CertificateRepository certificateRepository,
                              EnrollmentRepository enrollmentRepository) {
        if (certificateRepository == null || enrollmentRepository == null) {
            throw new IllegalArgumentException("Repositories cannot be null.");
        }
        this.certificateRepository = certificateRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public Certificate issueCertificate(String enrollmentId) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment not found: " + enrollmentId);
        }

        // Rule 1: Issue a certificate only for a COMPLETED enrollment
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Certificate cannot be issued: Enrollment is not COMPLETED (Current status: " +
                    enrollment.getStatus() + ", progress: " + enrollment.getProgressPercentage() + "%).");
        }

        // Rule 2: Issue only one certificate per enrollment
        Certificate existing = certificateRepository.findByEnrollment(enrollmentId);
        if (existing != null) {
            return existing;
        }

        // Rule 3: Unique certificate ID
        String certificateId = DataStore.generateCertificateId();
        LocalDateTime issueDate = DataStore.getCurrentTime();

        Certificate certificate = new Certificate(
                certificateId,
                enrollmentId,
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                issueDate
        );

        return certificateRepository.save(certificate);
    }

    public Certificate getCertificateByEnrollment(String enrollmentId) {
        return certificateRepository.findByEnrollment(enrollmentId);
    }

    public ArrayList<Certificate> getCertificatesByStudent(String studentId) {
        return certificateRepository.findByStudent(studentId);
    }

    public ArrayList<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }
}
