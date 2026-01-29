package com.example.bustracker;

import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class weatherController {

    // seattle coordinates

    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=47.6603&longitude=-122.3117" +
            "&hourly=temperature_2m,precipitation,apparent_temperature,precipitation_probability" +
            "&timezone=America%2FLos_Angeles" +
            "&forecast_days=1" +
            "&wind_speed_unit=mph" +
            "&temperature_unit=fahrenheit" +
            "&precipitation_unit=inch";

    @GetMapping("/api/weather")
    public WeatherResponse getWeather() {
        String apiData = fetchWeatherData();

        if (apiData.isEmpty()) {
            return new WeatherResponse("Error fetching weather", new ArrayList<>());
        }

        return parseWeatherData(apiData);
    }

    private String fetchWeatherData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WEATHER_API_URL))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Weather API request successful");
                return response.body();
            } else {
                System.out.println("Weather API failed: HTTP " + response.statusCode());
                return "";
            }
        } catch (Exception e) {
            System.out.println("Weather API error: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private WeatherResponse parseWeatherData(String apiData) {
        try {
            JSONObject root = new JSONObject(apiData);
            JSONObject hourly = root.getJSONObject("hourly");

            JSONArray times = hourly.getJSONArray("time");
            JSONArray temps = hourly.getJSONArray("temperature_2m");
            JSONArray precip = hourly.getJSONArray("precipitation");
            JSONArray precipProb = hourly.getJSONArray("precipitation_probability");
            JSONArray feelsLike = hourly.getJSONArray("apparent_temperature");
            List<HourlyWeather> hourlyList = new ArrayList<>();
            // Get first 12 hours (or however many you want)
            int hoursToShow = Math.min(24, times.length());

            for (int i = 0; i < hoursToShow; i++) {
                String time = times.getString(i);
                double temp = temps.getDouble(i);
                double precipitation = precip.getDouble(i);
                int precipProbability = precipProb.getInt(i); // percentage 0-100
                double apparentTemp = feelsLike.getDouble(i);

                hourlyList.add(new HourlyWeather(time, temp, precipitation, precipProbability, apparentTemp));
            }
            return new WeatherResponse("Success", hourlyList);

        } catch (Exception e) {
            e.printStackTrace();
            return new WeatherResponse("Error parsing weather data", new ArrayList<>());
        }
    }
}

// Response wrapper class
class WeatherResponse {
    public String status;
    public List<HourlyWeather> hourly;

    public WeatherResponse(String status, List<HourlyWeather> hourly) {
        this.status = status;
        this.hourly = hourly;
    }
}

// Data class for each hour
class HourlyWeather {
    public String time;
    public double temperature;
    public double precipitation;
    public int precipProbability; // percentage 0-100
    public double feelsLike;

    public HourlyWeather(String time, double temperature, double precipitation, int precipProbability,
            double feelsLike) {
        this.time = time;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.precipProbability = precipProbability;
        this.feelsLike = feelsLike;
    }
}
