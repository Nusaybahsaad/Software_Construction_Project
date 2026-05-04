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
 * The main Facade implementation of the TaskManager interface.
 * It coordinates between TaskService, WeatherService, and SchedulePlanner.
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

    @Override
    public void addTask(Task task) {
        taskService.addTask(task).subscribe();
    }

    @Override
    public void removeTask(String taskId) {
        taskService.removeTask(taskId).subscribe();
    }

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
     * This is the main reactive pipeline that connects weather fetching
     * with schedule planning.
     * 
     * @param location The city name to check.
     * @return A Mono list of smart recommendations.
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