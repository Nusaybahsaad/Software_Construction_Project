package taskmanager.impl;

import java.util.List;

import reactor.core.publisher.Mono;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
import taskmanager.service.WeatherService;

/**
 * The main manager that connects the Task Service, Weather Service, and
 * Planner.
 */
public class DefaultTaskManager implements TaskManager {

    final DefaultTaskService taskService;
    private final WeatherService weatherService;
    private final SchedulePlanner planner;

    public DefaultTaskManager(DefaultTaskService taskService,
            WeatherService weatherService,
            SchedulePlanner planner) {
        this.taskService = taskService;
        this.weatherService = weatherService;
        this.planner = planner;
    }

    /**
     * Adds a task to the system.
     * 
     * Preconditions: Task object must not be null.
     * Postconditions: Task is saved in the system.
     * Side-effects: Changes the internal task list state.
     * 
     * @param task The task to add.
     */
    @Override
    public void addTask(Task task) {
        taskService.addTask(task).subscribe();
    }

    /**
     * Preconditions: taskId must exist in the system.
     * Postconditions: Task is removed from the system.
     * Side-effects: Changes the internal task list state.
     * 
     * @param taskId ID of the task to delete.
     */
    @Override
    public void removeTask(String taskId) {
        taskService.removeTask(taskId).subscribe();
    }

    /**
     * Preconditions: None.
     * Postconditions: Returns all current tasks.
     * Thread-safety: Uses block() to safely return data to the UI thread.
     * 
     * @return List of tasks.
     */
    @Override
    public List<Task> getTasks() {
        // block() is used here to bridge the reactive service with the synchronous
        // Swing UI
        return taskService.findAllTasksAsList().block();
    }

    @Override
    public Mono<WeatherForecast> fetchWeather(String location) {
        return weatherService.getWeather(location);
    }

    /**
     * Exposes the internal TaskService so that SmartTaskManagerFrame can wire
     * add/delete operations directly to the reactive service.
     *
     * @return The DefaultTaskService used by this manager.
     */
    public DefaultTaskService getTaskService() {
        return taskService;
    }

    /**
     * Connects weather and planning logic.
     * 
     * Preconditions: Location must be a valid city name.
     * Postconditions: Returns smart advice for all tasks.
     * Side-effects: Calls external Weather API.
     * 
     * @param location City name.
     * @return A Mono list of recommendations.
     */
    public Mono<List<ScheduleRecommendation>> getSmartRecommendations(String location) {
        // Step 1: Call the weather service (Work of Member 1)
        return fetchWeather(location)
                // Step 2: Use flatMap to pass the result to the planner (Your Work)
                .flatMap(forecast -> {
                    // Step 3: Call the planner logic and return its result
                    return planner.suggestSchedule(getTasks(), forecast);
                });
    }

    @Override
    public SchedulePlanner getPlanner() {
        return planner;
    }
}