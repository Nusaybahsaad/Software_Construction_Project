package taskmanager.impl;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import taskmanager.api.SchedulePlanner;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
/**
 * This class handles the logic for suggesting task schedules
 * based on weather conditions.
 */
public class DefaultSchedulePlanner implements SchedulePlanner {

    /**
     * Analyzes tasks and provides weather advice.
     * 
     * Preconditions: The tasks list and forecast must not be null.
     * Postconditions: Returns a list of recommendations for all input tasks.
     * 
     * @param tasks    The list of tasks to analyze.
     * @param forecast The weather data.
     * @return A Mono containing the list of advice.
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestSchedule(List<Task> tasks, WeatherForecast forecast) {

        return Mono.fromCallable(() -> {
            return tasks.stream()
                    .map(task -> {
                        String advice;

                        if (task.isWeatherSensitive() && forecast.getPrecipitationProbability() > 0.5) {
                            advice = "⚠️ Warning: High chance of rain (" +
                                    (forecast.getPrecipitationProbability() * 100) +
                                    "%). It's better to delay this task.";
                        } else if (task.isWeatherSensitive() && forecast.getTemperatureCelsius() > 40.0) {
                            advice = "🔥 Alert: Extreme heat (" +
                                    forecast.getTemperatureCelsius() +
                                    "°C). Stay safe and stay indoors.";
                        } else {
                            advice = "✅ The weather looks good for this task.";
                        }

                        return new ScheduleRecommendation(task, advice);
                    })
                    .collect(Collectors.toList());
        });
    }

    /**
     * Preconditions: Location name must not be empty.
     * Postconditions: Always returns an error because this is handled by
     * TaskManager.
     * 
     * @param tasks    List of tasks.
     * @param location City name.
     * @return A Mono error.
     * @throws UnsupportedOperationException To signal that TaskManager should be
     *                                       used.
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(List<Task> tasks, String location) {
        return Mono.error(new UnsupportedOperationException("Use TaskManager for this."));
    }
}