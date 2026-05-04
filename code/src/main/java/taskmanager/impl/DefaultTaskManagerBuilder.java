package taskmanager.impl;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.service.WeatherService;

/**
 * This class is a builder for TaskManager.
 * It helps set up the TaskManager with the necessary API key and services.
 */
public class DefaultTaskManagerBuilder implements TaskManager.TaskManagerBuilder {
    
    private String apiKey;
    private String storagePath;

    /**
     * Stores the API key for weather services.
     * @param apiKey The OpenWeatherMap key.
     * @return The builder instance itself.
     */
    @Override
    public TaskManager.TaskManagerBuilder withWeatherApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this; // We return 'this' to allow method chaining (e.g., .with().with())
    }

    /**
     * Stores the path where tasks might be saved (optional).
     * @param path The file storage path.
     * @return The builder instance.
     */
    @Override
    public TaskManager.TaskManagerBuilder withStoragePath(String path) {
        this.storagePath = path;
        return this;
    }

    /**
     * Creates the final DefaultTaskManager object by linking all parts together.
     * @return A fully initialized TaskManager.
     */
    @Override
    public TaskManager build() {
        // Here we initialize the services that TaskManager needs
        DefaultTaskService taskService = new DefaultTaskService();
        WeatherService weatherService = new WeatherService(apiKey);
        SchedulePlanner planner = new DefaultSchedulePlanner();

        // Returning the final manager with all its components
        return new DefaultTaskManager(taskService, weatherService, planner);
    }
}