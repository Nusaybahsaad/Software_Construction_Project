package taskmanager.api;

import java.time.LocalDateTime;

public class Task {

    private final String id;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private boolean weatherSensitive;

    public Task(String id, String title, LocalDateTime dueDateTime, boolean weatherSensitive) {
        this.id = id;
        this.title = title;
        this.dueDateTime = dueDateTime;
        this.weatherSensitive = weatherSensitive;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public boolean isWeatherSensitive() {
        return weatherSensitive;
    }

    public void setWeatherSensitive(boolean weatherSensitive) {
        this.weatherSensitive = weatherSensitive;
    }
}