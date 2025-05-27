package edu.cit.stathis.task.enums;

public enum BadgeType {
    TASK_COMPLETION("Task Completion"),
    PERFECT_SCORE("Perfect Score"),
    SPEED_DEMON("Speed Demon"),
    ACCURACY_MASTER("Accuracy Master"),
    EXPERT_PERFORMANCE("Expert Performance");

    private final String description;

    BadgeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 