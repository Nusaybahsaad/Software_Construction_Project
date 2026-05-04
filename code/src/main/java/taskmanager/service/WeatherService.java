package taskmanager.service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.exception.WeatherAPIException;
import taskmanager.model.WeatherForecast;

/**
 * Service responsible for communicating with the OpenWeatherMap REST API.
 * It fetches real-time weather data asynchronously.
 */
public class WeatherService {

    private final String apiKey;

    /**
     * Initializes the service with a specific API key.
     * 
     * Preconditions: The apiKey must be a valid OpenWeatherMap key.
     * Postconditions: A WeatherService instance is ready to make requests.
     * 
     * @param apiKey The key used for authentication with the weather provider.
     */
    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Connects to the weather API and parses the JSON response.
     * 
     * Preconditions: The 'city' name must not be null or empty.
     * Postconditions: Returns a Mono that emits a WeatherForecast object.
     * Side-effects: Performs a network I/O operation to an external REST API[cite:
     * 2].
     * Thread-safety: Uses boundedElastic scheduler to prevent blocking the UI
     * thread[cite: 2].
     * 
     * @param city The name of the city to fetch weather for.
     * @return A Mono containing the WeatherForecast data, or a default object on
     *         error.
     * @throws WeatherAPIException if the server response code is not 200 (OK)[cite:
     *                             2].
     */
    public Mono<WeatherForecast> getWeather(String city) {
        return Mono.fromCallable(() -> {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                    + city + "&appid=" + apiKey + "&units=metric";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new WeatherAPIException("Failed to connect to Weather API", null);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parsing JSON response
            JSONObject json = new JSONObject(response.toString());
            String condition = json.getJSONArray("weather").getJSONObject(0).getString("main");
            double temp = json.getJSONObject("main").getDouble("temp");

            return new WeatherForecast(city, java.time.LocalDateTime.now(), temp, condition, 0.0);
        })
                .subscribeOn(Schedulers.boundedElastic()) // Executes API call on a background thread
                .onErrorReturn(new WeatherForecast(city, java.time.LocalDateTime.now(), 0, "Unknown", 0));
    }
}