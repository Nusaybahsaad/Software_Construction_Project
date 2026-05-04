package taskmanager.exception;

public class WeatherAPIException extends RuntimeException {
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}