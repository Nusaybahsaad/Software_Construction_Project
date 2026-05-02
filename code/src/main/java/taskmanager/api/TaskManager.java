package taskmanager.api;

import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Main facade for the Smart Task Manager.
 * Other developers will use this to interact with the system.
 */
public interface TaskManager {

    void addTask(Task task);

    void removeTask(String taskId);

    List<Task> getTasks();

    // Fetches weather data asynchronously without blocking
    Mono<WeatherForecast> fetchWeather(String location); 

    SchedulePlanner getPlanner();

    // Static factory method to get a new instance of a builder
    static TaskManagerBuilder builder() {
        return new DefaultTaskManagerBuilder(); // Creates and returns the hidden implementation of the builder
    }

    // A nested interface defining the steps to build a TaskManager
    interface TaskManagerBuilder {
        TaskManagerBuilder withWeatherApiKey(String apiKey); // stores the key.
        TaskManagerBuilder withStoragePath(String path);  // optional;stores the file path, if needed
        TaskManager build();//creates the final TaskManager object.
    }
}