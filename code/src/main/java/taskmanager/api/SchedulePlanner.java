package taskmanager.api;


import reactor.core.publisher.Mono;

import java.util.List;

public interface SchedulePlanner {

    Mono<List<ScheduleRecommendation>> suggestSchedule(
            List<Task> tasks,
            WeatherForecast forecast);

    Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(
            List<Task> tasks,
            String location);
}

record ScheduleRecommendation(Task task, String recommendation) {}