package taskmanager.api;


/**
 * A single suggested schedule entry for one task.
 */
public record ScheduleRecommendation(Task task, String recommendation) {
}