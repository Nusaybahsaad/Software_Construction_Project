package taskmanager.impl;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.service.WeatherService;

/**
 * Implementation of the TaskManagerBuilder interface.
 * This class follows the Builder Design Pattern to simplify the creation
 * and configuration of a DefaultTaskManager instance.
 */
public class DefaultTaskManagerBuilder implements TaskManager.TaskManagerBuilder {

    private String apiKey;
    private String storagePath;

    /**
     * Configures the API key required for weather data retrieval.
     * 
     * Preconditions: The apiKey should be a valid string from OpenWeatherMap.
     * Postconditions: Stores the key and returns the current builder instance.
     * Side-effects: Updates the internal apiKey state.
     * 
     * @param apiKey The OpenWeatherMap authentication key.
     * @return The current builder instance to allow method chaining.
     */

    @Override
    public TaskManager.TaskManagerBuilder withWeatherApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Configures the optional file path for task persistence.
     * 
     * Preconditions: The path string must not be null.
     * Postconditions: Stores the storage path and returns the builder.
     * Side-effects: Updates the internal storagePath state.
     * 
     * @param path The directory or file path for saving tasks.
     * @return The current builder instance.
     */

    @Override
    public TaskManager.TaskManagerBuilder withStoragePath(String path) {
        this.storagePath = path;
        return this;
    }

    /**
     * Assembles all internal components to create a functional TaskManager.
     * 
     * Preconditions: withWeatherApiKey() should have been called with a valid key.
     * Postconditions: Returns a fully initialized DefaultTaskManager linked with
     * TaskService, WeatherService, and SchedulePlanner.
     * Side-effects: Instantiates multiple service objects (TaskService,
     * WeatherService, Planner).
     * 
     * @return A ready-to-use TaskManager instance.
     */
    @Override
    public TaskManager build() {
        // Here we initialize the services that TaskManager needs
        DefaultTaskService taskService = new DefaultTaskService();
        WeatherService weatherService = new WeatherService(apiKey);
        SchedulePlanner planner = new DefaultSchedulePlanner();

        return new DefaultTaskManager(taskService, weatherService, planner);
    }
}