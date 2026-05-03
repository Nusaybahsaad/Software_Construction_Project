package taskmanager.api;

import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements the SchedulePlanner interface.
 * Its main job is to check tasks against weather conditions and give advice.
 */
public class DefaultSchedulePlanner implements SchedulePlanner {

    /**
     * This method takes a list of tasks and the weather forecast to suggest recommendations.
     * 
     * Preconditions: The tasks list and forecast object should not be null.
     * Postconditions: Returns a list of tasks paired with weather advice.
     * 
     * @param tasks The tasks to check.
     * @param forecast The weather information.
     * @return A Mono list of recommendations.
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestSchedule(List<Task> tasks, WeatherForecast forecast) {
        
        // We use Mono.fromCallable to make the processing asynchronous
        return Mono.fromCallable(() -> {
            return tasks.stream()
                .map(task -> {
                    String advice;
                    
                    // Check if the task is weather-sensitive and if there is high rain probability
                    if (task.isWeatherSensitive() && forecast.getPrecipitationProbability() > 0.5) {
                        advice = "⚠️ Warning: High chance of rain (" + 
                                 (forecast.getPrecipitationProbability() * 100) + 
                                 "%). It's better to delay this task.";
                    } 
                    // Check if the task is weather-sensitive and if it's too hot
                    else if (task.isWeatherSensitive() && forecast.getTemperatureCelsius() > 40.0) {
                        advice = "🔥 Alert: Extreme heat (" + 
                                 forecast.getTemperatureCelsius() + 
                                 "°C). Stay safe and stay indoors.";
                    } 
                    else {
                        advice = "✅ The weather looks good for this task.";
                    }
                    
                    return new ScheduleRecommendation(task, advice);
                })
                .collect(Collectors.toList());
        });
    }

    /**
     * This feature is not implemented here as it needs the API connection first.
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(List<Task> tasks, String location) {
        return Mono.error(new UnsupportedOperationException("Needs API connection."));
    }
}