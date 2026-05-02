package taskmanager.api;

import java.time.LocalDateTime;

public class WeatherForecast {

    private final String location;
    private final LocalDateTime time;
    private final double temperatureCelsius;
    private final String condition;
    private final double precipitationProbability;

    public WeatherForecast(String location, LocalDateTime time,
                           double temperatureCelsius,
                           String condition,
                           double precipitationProbability) {
        this.location = location;
        this.time = time;
        this.temperatureCelsius = temperatureCelsius;
        this.condition = condition;
        this.precipitationProbability = precipitationProbability;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public String getCondition() {
        return condition;
    }

    public double getPrecipitationProbability() {
        return precipitationProbability;
    }
}