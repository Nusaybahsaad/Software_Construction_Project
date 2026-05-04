package taskmanager.api;
import java.util.List;

import reactor.core.publisher.Mono;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

public interface SchedulePlanner {

    Mono<List<ScheduleRecommendation>> suggestSchedule(
            List<Task> tasks,
            WeatherForecast forecast);

    Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(
            List<Task> tasks,
            String location);
}

//record ScheduleRecommendation(Task task, String recommendation) {}