package Repository;

import Models.Course;
import java.util.ArrayList;

public class CourseRepository {

    public Course save(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (course.getCourseId() == null || course.getCourseId().trim().isEmpty()) {
            course.setCourseId(DataStore.generateCourseId());
        }
        DataStore.courses.add(course);
        return course;
    }

    public Course findById(String courseId) {
        if (courseId == null) return null;
        for (int i = 0; i < DataStore.courses.size(); i++) {
            Course c = DataStore.courses.get(i);
            if (c.getCourseId().equalsIgnoreCase(courseId.trim())) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<Course> findAll() {
        return new ArrayList<>(DataStore.courses);
    }

    public ArrayList<Course> findByInstructor(String instructorId) {
        ArrayList<Course> result = new ArrayList<>();
        if (instructorId == null) return result;
        for (int i = 0; i < DataStore.courses.size(); i++) {
            Course c = DataStore.courses.get(i);
            if (c.getInstructorId().equalsIgnoreCase(instructorId.trim())) {
                result.add(c);
            }
        }
        return result;
    }

    public ArrayList<Course> findPublishedCourses() {
        ArrayList<Course> result = new ArrayList<>();
        for (int i = 0; i < DataStore.courses.size(); i++) {
            Course c = DataStore.courses.get(i);
            if (c.getStatus() == Course.Status.PUBLISHED) {
                result.add(c);
            }
        }
        return result;
    }

    public ArrayList<Course> findFlaggedCourses() {
        ArrayList<Course> result = new ArrayList<>();
        for (int i = 0; i < DataStore.courses.size(); i++) {
            Course c = DataStore.courses.get(i);
            if (c.isFlaggedForReview()) {
                result.add(c);
            }
        }
        return result;
    }

    public boolean update(Course course) {
        if (course == null || course.getCourseId() == null) return false;
        for (int i = 0; i < DataStore.courses.size(); i++) {
            if (DataStore.courses.get(i).getCourseId().equals(course.getCourseId())) {
                DataStore.courses.set(i, course);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String courseId) {
        if (courseId == null) return false;
        for (int i = 0; i < DataStore.courses.size(); i++) {
            if (DataStore.courses.get(i).getCourseId().equalsIgnoreCase(courseId.trim())) {
                DataStore.courses.remove(i);
                return true;
            }
        }
        return false;
    }
}
