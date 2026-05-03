package taskmanager.api;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * Service responsible for communicating with the OpenWeatherMap REST API.
 * It fetches real-time weather data asynchronously.
 */
public class WeatherService {

    private final String apiKey;

    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Connects to the weather API and parses the JSON response.
     * <p><b>Technical Note:</b> Uses boundedElastic scheduler to prevent blocking the UI thread.</p>
     * @param city The name of the city to fetch weather for.
     * @return A Mono containing the WeatherForecast data.
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