package Models;

public class Lesson {
    public enum LessonType {
        VIDEO,
        READING,
        QUIZ
    }

    private String lessonId;
    private String sectionId;
    private String title;
    private LessonType type;
    private int durationMinutes;
    private int orderNumber;

    public Lesson(String lessonId, String sectionId, String title, LessonType type, int durationMinutes, int orderNumber) {
        if (lessonId == null || lessonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID cannot be empty.");
        }
        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Section ID cannot be empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson title cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Lesson type cannot be null.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Lesson duration must be greater than zero minutes.");
        }
        if (orderNumber <= 0) {
            throw new IllegalArgumentException("Lesson order number must be positive.");
        }
        this.lessonId = lessonId.trim();
        this.sectionId = sectionId.trim();
        this.title = title.trim();
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.orderNumber = orderNumber;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        if (lessonId == null || lessonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID cannot be empty.");
        }
        this.lessonId = lessonId.trim();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson title cannot be empty.");
        }
        this.title = title.trim();
    }

    public LessonType getType() {
        return type;
    }

    public void setType(LessonType type) {
        if (type == null) {
            throw new IllegalArgumentException("Lesson type cannot be null.");
        }
        this.type = type;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Lesson duration must be greater than zero minutes.");
        }
        this.durationMinutes = durationMinutes;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        if (orderNumber <= 0) {
            throw new IllegalArgumentException("Lesson order number must be positive.");
        }
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id='" + lessonId + '\'' +
                ", order=" + orderNumber +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", duration=" + durationMinutes + " mins" +
                '}';
    }
}
