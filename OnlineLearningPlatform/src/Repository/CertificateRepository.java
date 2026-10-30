package Repository;

import Models.Certificate;
import java.util.ArrayList;

public class CertificateRepository {

    public Certificate save(Certificate certificate) {
        if (certificate == null) {
            throw new IllegalArgumentException("Certificate cannot be null.");
        }
        if (certificate.getCertificateId() == null || certificate.getCertificateId().trim().isEmpty()) {
            certificate.setCertificateId(DataStore.generateCertificateId());
        }
        // Ensure certificate uniqueness
        if (findById(certificate.getCertificateId()) != null) {
            throw new IllegalArgumentException("Certificate ID already exists: " + certificate.getCertificateId());
        }
        DataStore.certificates.add(certificate);
        return certificate;
    }

    public Certificate findById(String certificateId) {
        if (certificateId == null) return null;
        for (int i = 0; i < DataStore.certificates.size(); i++) {
            Certificate c = DataStore.certificates.get(i);
            if (c.getCertificateId().equalsIgnoreCase(certificateId.trim())) {
                return c;
            }
        }
        return null;
    }

    public Certificate findByEnrollment(String enrollmentId) {
        if (enrollmentId == null) return null;
        for (int i = 0; i < DataStore.certificates.size(); i++) {
            Certificate c = DataStore.certificates.get(i);
            if (c.getEnrollmentId().equalsIgnoreCase(enrollmentId.trim())) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<Certificate> findByStudent(String studentId) {
        ArrayList<Certificate> result = new ArrayList<>();
        if (studentId == null) return result;
        for (int i = 0; i < DataStore.certificates.size(); i++) {
            Certificate c = DataStore.certificates.get(i);
            if (c.getStudentId().equalsIgnoreCase(studentId.trim())) {
                result.add(c);
            }
        }
        return result;
    }

    public ArrayList<Certificate> findAll() {
        return new ArrayList<>(DataStore.certificates);
    }
}
