package Models;

import java.util.ArrayList;

public class Section {
    private String sectionId;
    private String courseId;
    private String title;
    private int orderNumber;
    private ArrayList<Lesson> lessons;

    public Section(String sectionId, String courseId, String title, int orderNumber) {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Section ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty.");
        }
        if (orderNumber <= 0) {
            throw new IllegalArgumentException("Section order number must be positive.");
        }
        this.sectionId = sectionId.trim();
        this.courseId = courseId.trim();
        this.title = title.trim();
        this.orderNumber = orderNumber;
        this.lessons = new ArrayList<>();
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Section ID cannot be empty.");
        }
        this.sectionId = sectionId.trim();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        this.courseId = courseId.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty.");
        }
        this.title = title.trim();
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        if (orderNumber <= 0) {
            throw new IllegalArgumentException("Section order number must be positive.");
        }
        this.orderNumber = orderNumber;
    }

    public ArrayList<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(ArrayList<Lesson> lessons) {
        if (lessons == null) {
            this.lessons = new ArrayList<>();
        } else {
            this.lessons = lessons;
        }
    }

    public void addLesson(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null.");
        }
        // Verify order uniqueness or insert in sorted order
        for (Lesson existing : this.lessons) {
            if (existing.getLessonId().equals(lesson.getLessonId())) {
                throw new IllegalArgumentException("Lesson with ID " + lesson.getLessonId() + " already exists in this section.");
            }
            if (existing.getOrderNumber() == lesson.getOrderNumber()) {
                throw new IllegalArgumentException("Lesson order number " + lesson.getOrderNumber() + " is already taken in this section.");
            }
        }
        this.lessons.add(lesson);
        // Sort lessons by orderNumber using simple insertion sort
        for (int i = 0; i < this.lessons.size() - 1; i++) {
            for (int j = 0; j < this.lessons.size() - i - 1; j++) {
                if (this.lessons.get(j).getOrderNumber() > this.lessons.get(j + 1).getOrderNumber()) {
                    Lesson temp = this.lessons.get(j);
                    this.lessons.set(j, this.lessons.get(j + 1));
                    this.lessons.set(j + 1, temp);
                }
            }
        }
    }

    public boolean removeLesson(String lessonId) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId().equals(lessonId)) {
                lessons.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Section{" +
                "id='" + sectionId + '\'' +
                ", order=" + orderNumber +
                ", title='" + title + '\'' +
                ", lessonsCount=" + lessons.size() +
                '}';
    }
}
