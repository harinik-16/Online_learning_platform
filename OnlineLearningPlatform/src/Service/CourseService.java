package Service;

import Models.Course;
import Models.Enrollment;
import Models.Lesson;
import Models.Section;
import Models.User;
import Repository.CourseRepository;
import Repository.DataStore;
import Repository.EnrollmentRepository;
import Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final RefundService refundService;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentRepository enrollmentRepository,
                         UserRepository userRepository,
                         RefundService refundService) {
        if (courseRepository == null || enrollmentRepository == null ||
            userRepository == null || refundService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.refundService = refundService;
    }

    public Course createCourse(String creatorUserId, String title, String description,
                               Course.Category category, Course.Level level, double price) {
        User creator = userRepository.findById(creatorUserId);
        if (creator == null) {
            throw new IllegalArgumentException("User not found: " + creatorUserId);
        }

        // Rule 1 & 2: Only INSTRUCTOR and ADMIN can create courses. STUDENT cannot.
        if (creator.getRole() == User.Role.STUDENT) {
            throw new IllegalStateException("Access denied: Students cannot create courses.");
        }

        // Rule 3 & 4: Non-empty title, valid category, valid level, non-negative price (0 means free).
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Course description cannot be empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Course category must be specified.");
        }
        if (level == null) {
            throw new IllegalArgumentException("Course level must be specified.");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("Course price cannot be negative.");
        }

        String courseId = DataStore.generateCourseId();
        Course course = new Course(courseId, creatorUserId, title, description, category, level, price, DataStore.getCurrentTime());
        // Default status is DRAFT as per rule
        return courseRepository.save(course);
    }

    public Section addSection(String userId, String courseId, String title, int orderNumber) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty.");
        }
        if (orderNumber <= 0) {
            throw new IllegalArgumentException("Section order number must be positive.");
        }

        String sectionId = DataStore.generateSectionId();
        Section section = new Section(sectionId, courseId, title, orderNumber);
        course.addSection(section);
        courseRepository.update(course);
        return section;
    }

    public Lesson addLesson(String userId, String courseId, String sectionId, String title,
                            Lesson.LessonType type, int durationMinutes, int orderNumber) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);
        Section section = course.findSectionById(sectionId);
        if (section == null) {
            throw new IllegalArgumentException("Section not found: " + sectionId + " in course " + courseId);
        }

        // Rule 12: Validate lesson duration, section order, lesson order
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Lesson duration must be greater than zero minutes.");
        }
        if (orderNumber <= 0) {
            throw new IllegalArgumentException("Lesson order number must be positive.");
        }

        String lessonId = DataStore.generateLessonId();
        Lesson lesson = new Lesson(lessonId, sectionId, title, type, durationMinutes, orderNumber);
        section.addLesson(lesson);
        courseRepository.update(course);
        return lesson;
    }

    public Course publishCourse(String userId, String courseId) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);

        if (course.getStatus() == Course.Status.PUBLISHED) {
            throw new IllegalStateException("Course is already published.");
        }
        if (course.getStatus() == Course.Status.WITHDRAWN) {
            throw new IllegalStateException("Withdrawn course cannot be published.");
        }

        // Rule 6: A course can be published only if it has at least one section and at least three lessons across all sections.
        if (course.getSections().isEmpty()) {
            throw new IllegalStateException("Cannot publish course: Must have at least one section.");
        }
        int totalLessons = course.getTotalLessonsCount();
        if (totalLessons < 3) {
            throw new IllegalStateException("Cannot publish course: Must have at least three lessons across all sections (currently has " + totalLessons + ").");
        }

        course.setStatus(Course.Status.PUBLISHED);
        courseRepository.update(course);
        return course;
    }

    public void editCourseDetails(String userId, String courseId, String newTitle, String newDescription) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);

        // Rule 8: After publication, title and description may be edited.
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            course.setTitle(newTitle.trim());
        }
        if (newDescription != null && !newDescription.trim().isEmpty()) {
            course.setDescription(newDescription.trim());
        }
        courseRepository.update(course);
    }

    public void updateCategoryAndLevel(String userId, String courseId, Course.Category newCategory, Course.Level newLevel) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);

        // Rule 7: After publication, category and level cannot be changed.
        if (course.getStatus() != Course.Status.DRAFT) {
            throw new IllegalStateException("Category and Level cannot be changed after the course has been published or withdrawn.");
        }

        if (newCategory != null) {
            course.setCategory(newCategory);
        }
        if (newLevel != null) {
            course.setLevel(newLevel);
        }
        courseRepository.update(course);
    }

    public void updatePrice(String userId, String courseId, double newPrice) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);
        if (newPrice < 0.0) {
            throw new IllegalArgumentException("Course price cannot be negative.");
        }
        // Rule 10: A course price change affects only new enrollments.
        course.setPrice(newPrice);
        courseRepository.update(course);
    }

    public boolean deleteLesson(String userId, String courseId, String sectionId, String lessonId) {
        Course course = getAndAuthorizeCourseModification(userId, courseId);

        // Rule 9: A lesson cannot be deleted if any student has enrolled in that course.
        ArrayList<Enrollment> enrollments = enrollmentRepository.findByCourse(courseId);
        if (!enrollments.isEmpty()) {
            throw new IllegalStateException("Cannot delete lesson: Students have already enrolled in this course.");
        }

        Section section = course.findSectionById(sectionId);
        if (section == null) {
            throw new IllegalArgumentException("Section not found: " + sectionId);
        }

        boolean removed = section.removeLesson(lessonId);
        if (removed) {
            courseRepository.update(course);
        }
        return removed;
    }

    public Course withdrawCourse(String adminUserId, String courseId) {
        User admin = userRepository.findById(adminUserId);
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            throw new IllegalStateException("Access denied: Only an ADMIN can withdraw a course.");
        }

        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }

        // Rule 11: Admin can withdraw a course. Withdrawal must trigger full refunds for eligible paid enrollments.
        course.setStatus(Course.Status.WITHDRAWN);
        courseRepository.update(course);

        // Trigger withdrawal refunds
        refundService.processWithdrawalRefunds(courseId);

        return course;
    }

    private Course getAndAuthorizeCourseModification(String userId, String courseId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }

        // Must be the instructor who owns the course or an Admin
        if (user.getRole() != User.Role.ADMIN && !course.getInstructorId().equalsIgnoreCase(userId.trim())) {
            throw new IllegalStateException("Access denied: Only the course instructor or an admin can modify this course.");
        }

        return course;
    }

    public Course getCourseById(String courseId) {
        return courseRepository.findById(courseId);
    }

    public ArrayList<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public ArrayList<Course> getPublishedCourses() {
        return courseRepository.findPublishedCourses();
    }

    public ArrayList<Course> getCoursesByInstructor(String instructorId) {
        return courseRepository.findByInstructor(instructorId);
    }

    public ArrayList<Course> getFlaggedCourses() {
        return courseRepository.findFlaggedCourses();
    }
}
