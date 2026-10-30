package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Course {
    public enum Category {
        PROGRAMMING,
        DESIGN,
        BUSINESS,
        DATA_SCIENCE,
        LANGUAGE
    }

    public enum Level {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }

    public enum Status {
        DRAFT,
        PUBLISHED,
        WITHDRAWN
    }

    private String courseId;
    private String instructorId;
    private String title;
    private String description;
    private Category category;
    private Level level;
    private double price;
    private Status status;
    private LocalDateTime creationDate;
    private double averageRating;
    private int totalRatings;
    private ArrayList<Section> sections;
    private boolean flaggedForReview;

    public Course(String courseId, String instructorId, String title, String description,
                  Category category, Level level, double price, LocalDateTime creationDate) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor ID cannot be empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Course description cannot be empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null.");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("Course price cannot be negative.");
        }

        this.courseId = courseId.trim();
        this.instructorId = instructorId.trim();
        this.title = title.trim();
        this.description = description.trim();
        this.category = category;
        this.level = level;
        this.price = Math.round(price * 100.0) / 100.0;
        this.status = Status.DRAFT; // Default status is DRAFT
        this.creationDate = creationDate != null ? creationDate : LocalDateTime.now();
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.sections = new ArrayList<>();
        this.flaggedForReview = false;
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

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor ID cannot be empty.");
        }
        this.instructorId = instructorId.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Course description cannot be empty.");
        }
        this.description = description.trim();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        this.category = category;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null.");
        }
        this.level = level;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0.0) {
            throw new IllegalArgumentException("Course price cannot be negative.");
        }
        this.price = Math.round(price * 100.0) / 100.0;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
        checkReviewFlag();
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
        checkReviewFlag();
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public void setFlaggedForReview(boolean flaggedForReview) {
        this.flaggedForReview = flaggedForReview;
    }

    private void checkReviewFlag() {
        // Module 5 Rule 8: If the course has at least 20 ratings and its average rating is below 2.5, flag it for admin review.
        if (this.totalRatings >= 20 && this.averageRating < 2.5) {
            this.flaggedForReview = true;
        } else {
            this.flaggedForReview = false;
        }
    }

    public ArrayList<Section> getSections() {
        return sections;
    }

    public void setSections(ArrayList<Section> sections) {
        if (sections == null) {
            this.sections = new ArrayList<>();
        } else {
            this.sections = sections;
        }
    }

    public void addSection(Section section) {
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null.");
        }
        for (Section existing : this.sections) {
            if (existing.getSectionId().equals(section.getSectionId())) {
                throw new IllegalArgumentException("Section with ID " + section.getSectionId() + " already exists in this course.");
            }
            if (existing.getOrderNumber() == section.getOrderNumber()) {
                throw new IllegalArgumentException("Section order number " + section.getOrderNumber() + " is already taken.");
            }
        }
        this.sections.add(section);
        // Sort sections by orderNumber
        for (int i = 0; i < this.sections.size() - 1; i++) {
            for (int j = 0; j < this.sections.size() - i - 1; j++) {
                if (this.sections.get(j).getOrderNumber() > this.sections.get(j + 1).getOrderNumber()) {
                    Section temp = this.sections.get(j);
                    this.sections.set(j, this.sections.get(j + 1));
                    this.sections.set(j + 1, temp);
                }
            }
        }
    }

    public Section findSectionById(String sectionId) {
        for (Section s : sections) {
            if (s.getSectionId().equals(sectionId)) {
                return s;
            }
        }
        return null;
    }

    public int getTotalLessonsCount() {
        int count = 0;
        for (Section s : sections) {
            count += s.getLessons().size();
        }
        return count;
    }

    public ArrayList<Lesson> getAllLessonsInOrder() {
        ArrayList<Lesson> allLessons = new ArrayList<>();
        for (Section s : sections) {
            for (Lesson l : s.getLessons()) {
                allLessons.add(l);
            }
        }
        return allLessons;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + courseId + '\'' +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", level=" + level +
                ", price=" + price +
                ", status=" + status +
                ", sections=" + sections.size() +
                ", totalLessons=" + getTotalLessonsCount() +
                ", rating=" + averageRating + " (" + totalRatings + " reviews)" +
                (flaggedForReview ? " [FLAGGED FOR REVIEW]" : "") +
                '}';
    }
}
