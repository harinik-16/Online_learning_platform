package Service;

import Models.Course;
import Models.Enrollment;
import Models.Lesson;
import Models.LessonProgress;
import Models.QuizAttempt;
import Repository.CourseRepository;
import Repository.DataStore;
import Repository.EnrollmentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ProgressService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CertificateService certificateService;

    public ProgressService(EnrollmentRepository enrollmentRepository,
                           CourseRepository courseRepository,
                           CertificateService certificateService) {
        if (enrollmentRepository == null || courseRepository == null || certificateService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.certificateService = certificateService;
    }

    public LessonProgress completeVideoLesson(String studentId, String enrollmentId, String lessonId, double watchPercentage) {
        Enrollment enrollment = validateStudentAndEnrollment(studentId, enrollmentId);
        Course course = getCourseForEnrollment(enrollment);
        Lesson targetLesson = validateLessonSequence(course, enrollment, lessonId);

        if (targetLesson.getType() != Lesson.LessonType.VIDEO) {
            throw new IllegalArgumentException("Lesson " + lessonId + " is not a VIDEO lesson.");
        }

        LessonProgress progress = getOrCreateLessonProgress(enrollmentId, lessonId);
        progress.setVideoWatchPercentage(watchPercentage);

        // Rule 4: VIDEO lessons require at least 90% watch completion
        if (watchPercentage >= 90.0) {
            if (!progress.isCompleted()) {
                progress.setCompleted(true);
                progress.setCompletionDate(DataStore.getCurrentTime());
                enrollmentRepository.updateLessonProgress(progress);
                recalculateAndSaveEnrollmentProgress(enrollment, course);
            }
        } else {
            enrollmentRepository.updateLessonProgress(progress);
            throw new IllegalArgumentException("Video watch percentage is " + watchPercentage +
                    "%. At least 90% watch completion is required to complete this lesson.");
        }

        return progress;
    }

    public LessonProgress completeReadingLesson(String studentId, String enrollmentId, String lessonId, boolean confirmed) {
        Enrollment enrollment = validateStudentAndEnrollment(studentId, enrollmentId);
        Course course = getCourseForEnrollment(enrollment);
        Lesson targetLesson = validateLessonSequence(course, enrollment, lessonId);

        if (targetLesson.getType() != Lesson.LessonType.READING) {
            throw new IllegalArgumentException("Lesson " + lessonId + " is not a READING lesson.");
        }

        // Rule 5: READING lessons can be marked complete when the student confirms completion
        if (!confirmed) {
            throw new IllegalArgumentException("Student must confirm completion of the reading lesson.");
        }

        LessonProgress progress = getOrCreateLessonProgress(enrollmentId, lessonId);
        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletionDate(DataStore.getCurrentTime());
            enrollmentRepository.updateLessonProgress(progress);
            recalculateAndSaveEnrollmentProgress(enrollment, course);
        }

        return progress;
    }

    public QuizAttempt attemptQuizLesson(String studentId, String enrollmentId, String lessonId, double score) {
        Enrollment enrollment = validateStudentAndEnrollment(studentId, enrollmentId);
        Course course = getCourseForEnrollment(enrollment);
        Lesson targetLesson = validateLessonSequence(course, enrollment, lessonId);

        if (targetLesson.getType() != Lesson.LessonType.QUIZ) {
            throw new IllegalArgumentException("Lesson " + lessonId + " is not a QUIZ lesson.");
        }

        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Quiz score must be between 0 and 100.");
        }

        // Rule 7, 8 & 9: Quiz attempt tracking and cooldown
        ArrayList<QuizAttempt> attempts = enrollmentRepository.findQuizAttempts(enrollmentId, lessonId);
        checkQuizCooldown(attempts);

        LocalDateTime now = DataStore.getCurrentTime();
        // Rule 6: QUIZ lessons require a score of at least 60% to pass
        boolean passed = (score >= 60.0);

        String attemptId = DataStore.generateAttemptId();
        QuizAttempt newAttempt = new QuizAttempt(attemptId, enrollmentId, lessonId, score, now, passed);
        enrollmentRepository.saveQuizAttempt(newAttempt);

        LessonProgress progress = getOrCreateLessonProgress(enrollmentId, lessonId);
        progress.setQuizScore(score);

        if (passed) {
            if (!progress.isCompleted()) {
                progress.setCompleted(true);
                progress.setCompletionDate(now);
                enrollmentRepository.updateLessonProgress(progress);
                recalculateAndSaveEnrollmentProgress(enrollment, course);
            }
        } else {
            enrollmentRepository.updateLessonProgress(progress);
        }

        return newAttempt;
    }

    private void checkQuizCooldown(ArrayList<QuizAttempt> attempts) {
        if (attempts.isEmpty()) return;

        // Check if student already passed
        for (int i = 0; i < attempts.size(); i++) {
            if (attempts.get(i).isPassed()) {
                // Already passed, but can review or retry
                return;
            }
        }

        // Count consecutive failed attempts in current cycle
        int failedCount = attempts.size();
        if (failedCount > 0 && failedCount % 3 == 0) {
            // Check the 3rd failed attempt in the latest block
            QuizAttempt lastThirdFailed = attempts.get(failedCount - 1);
            LocalDateTime cooldownEnd = lastThirdFailed.getAttemptDate().plusHours(24);
            LocalDateTime now = DataStore.getCurrentTime();

            if (now.isBefore(cooldownEnd)) {
                throw new IllegalStateException("Quiz locked due to 3 failed attempts. Mandatory 24-hour cooldown in effect until " + cooldownEnd);
            }
            // Cooldown has ended; allowed another attempt cycle
        }
    }

    public LocalDateTime getQuizCooldownEndTime(String enrollmentId, String lessonId) {
        ArrayList<QuizAttempt> attempts = enrollmentRepository.findQuizAttempts(enrollmentId, lessonId);
        if (attempts.isEmpty()) return null;

        for (int i = 0; i < attempts.size(); i++) {
            if (attempts.get(i).isPassed()) {
                return null;
            }
        }

        int failedCount = attempts.size();
        if (failedCount > 0 && failedCount % 3 == 0) {
            QuizAttempt lastThirdFailed = attempts.get(failedCount - 1);
            LocalDateTime cooldownEnd = lastThirdFailed.getAttemptDate().plusHours(24);
            LocalDateTime now = DataStore.getCurrentTime();
            if (now.isBefore(cooldownEnd)) {
                return cooldownEnd;
            }
        }
        return null;
    }

    private Enrollment validateStudentAndEnrollment(String studentId, String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment not found: " + enrollmentId);
        }
        if (!enrollment.getStudentId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalStateException("Student " + studentId + " is not authorized for enrollment " + enrollmentId);
        }

        // Rule 1: A student must be enrolled and ACTIVE before accessing required lessons
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE &&
            enrollment.getStatus() != Enrollment.EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Access denied: Enrollment is not ACTIVE (Current status: " + enrollment.getStatus() + ").");
        }

        return enrollment;
    }

    private Course getCourseForEnrollment(Enrollment enrollment) {
        Course course = courseRepository.findById(enrollment.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + enrollment.getCourseId());
        }
        return course;
    }

    private Lesson validateLessonSequence(Course course, Enrollment enrollment, String lessonId) {
        ArrayList<Lesson> orderedLessons = course.getAllLessonsInOrder();
        int targetIndex = -1;
        Lesson targetLesson = null;

        for (int i = 0; i < orderedLessons.size(); i++) {
            if (orderedLessons.get(i).getLessonId().equalsIgnoreCase(lessonId.trim())) {
                targetIndex = i;
                targetLesson = orderedLessons.get(i);
                break;
            }
        }

        if (targetLesson == null) {
            throw new IllegalArgumentException("Lesson " + lessonId + " does not belong to course " + course.getCourseId());
        }

        // Rule 2 & 3: Lessons must be completed in course order across sections.
        // A lesson cannot be completed until all preceding required lessons are complete.
        for (int i = 0; i < targetIndex; i++) {
            Lesson preceding = orderedLessons.get(i);
            LessonProgress precedingProgress = enrollmentRepository.findLessonProgress(enrollment.getEnrollmentId(), preceding.getLessonId());
            if (precedingProgress == null || !precedingProgress.isCompleted()) {
                throw new IllegalStateException("Sequential progress required: Preceding lesson '" +
                        preceding.getTitle() + "' (Order " + preceding.getOrderNumber() + ") is not yet completed.");
            }
        }

        return targetLesson;
    }

    private LessonProgress getOrCreateLessonProgress(String enrollmentId, String lessonId) {
        LessonProgress progress = enrollmentRepository.findLessonProgress(enrollmentId, lessonId);
        if (progress == null) {
            progress = new LessonProgress(DataStore.generateProgressId(), enrollmentId, lessonId);
            enrollmentRepository.saveLessonProgress(progress);
        }
        return progress;
    }

    private void recalculateAndSaveEnrollmentProgress(Enrollment enrollment, Course course) {
        // Rule 13: If new lessons are added after completion, preserve the existing COMPLETED enrollment
        // and do not require those lessons from that student.
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED) {
            enrollment.setProgressPercentage(100.0);
            enrollmentRepository.update(enrollment);
            return;
        }

        ArrayList<Lesson> lessons = course.getAllLessonsInOrder();
        int totalLessons = lessons.size();
        if (totalLessons == 0) {
            return;
        }

        int completedCount = 0;
        for (int i = 0; i < lessons.size(); i++) {
            LessonProgress lp = enrollmentRepository.findLessonProgress(enrollment.getEnrollmentId(), lessons.get(i).getLessonId());
            if (lp != null && lp.isCompleted()) {
                completedCount++;
            }
        }

        enrollment.setCompletedLessonsCount(completedCount);
        // Rule 10: Calculate progress as completed required lessons / total required lessons * 100
        double percentage = ((double) completedCount / totalLessons) * 100.0;
        enrollment.setProgressPercentage(percentage);

        // Rule 12: When progress reaches 100%, mark enrollment COMPLETED and record completion date
        if (completedCount >= totalLessons) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
            enrollment.setCompletionDate(DataStore.getCurrentTime());
            // Automatically issue certificate
            certificateService.issueCertificate(enrollment.getEnrollmentId());
        }

        enrollmentRepository.update(enrollment);
    }

    public ArrayList<LessonProgress> getProgressForEnrollment(String enrollmentId) {
        return enrollmentRepository.findLessonProgressesByEnrollment(enrollmentId);
    }

    public ArrayList<QuizAttempt> getQuizAttemptsForLesson(String enrollmentId, String lessonId) {
        return enrollmentRepository.findQuizAttempts(enrollmentId, lessonId);
    }
}
