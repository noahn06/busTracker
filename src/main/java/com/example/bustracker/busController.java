package com.example.bustracker;

import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;

@RestController
@CrossOrigin(origins = "*")
public class busController {

    public static final String STOP_ID = "1_38024";
    public static final int NUM_OF_ARRIVALS = 3;

    @GetMapping("/api/buses")
    public BusResponse getBuses() {
        String url = linkBuilder();
        String apiData = fetchAPIData(url);

        if (apiData.isEmpty()) {
            return new BusResponse("Error", new ArrayList<>());
        }

        return fetchAndDisplayRouteData(apiData);
    }

    private String linkBuilder() {
        String apiKey = System.getenv("API_KEY");

        // if apikey not found on render, refer back to local api key in .env
        if (apiKey == null) {
            Dotenv dotenv = Dotenv.load();
            apiKey = dotenv.get("API_KEY");
        }

        String url = "https://api.pugetsound.onebusaway.org/api/where/";
        url += "arrivals-and-departures-for-stop/";
        url += STOP_ID;
        url += ".json?key=";
        url += apiKey;
        url += "&minutesAfter=60";
        return url;
    }

    private String fetchAPIData(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int httpCode = response.statusCode();

            if (httpCode == 200) {
                String payload = response.body();
                System.out.println("API request successful");
                return payload;
            } else {
                System.out.println("API request failed HTTP code: " + httpCode);
                return "";
            }
        } catch (java.net.http.HttpTimeoutException e) {
            System.out.println("API request failed (Connection timeout)");
            e.printStackTrace();
            return "";
        } catch (Exception e) {
            System.out.println("API request failed (Other error)");
            e.printStackTrace();
            return "";
        }
    }

    private BusResponse fetchAndDisplayRouteData(String APIData) {
        try {
            JSONObject root = new JSONObject(APIData);

            long currentTimeMillis = root.getLong("currentTime");
            String serverTime = timeToString(currentTimeMillis);

            JSONArray arrivals = root.getJSONObject("data")
                    .getJSONObject("entry")
                    .getJSONArray("arrivalsAndDepartures");

            List<BusArrival> busList = new ArrayList<>();

            if (arrivals.length() == 0) {
                return new BusResponse(serverTime, busList);
            }

            int displayCount = 0;
            int arrivalsIndex = 0;

            while (displayCount < NUM_OF_ARRIVALS && arrivalsIndex < arrivals.length()) {
                JSONObject arrivingBus = arrivals.getJSONObject(arrivalsIndex);

                long arrivalTimeMillis;
                if (arrivingBus.has("predictedArrivalTime")) {
                    arrivalTimeMillis = arrivingBus.getLong("predictedArrivalTime");
                } else {
                    arrivalTimeMillis = arrivingBus.getLong("scheduledArrivalTime");
                }

                int minsTillArrival = (int) ((arrivalTimeMillis - currentTimeMillis) / 60000);

                if (minsTillArrival >= 0) {
                    String routeShortName = arrivingBus.getString("routeShortName");
                    String stringETA = timeToString(arrivalTimeMillis);

                    busList.add(new BusArrival(routeShortName, minsTillArrival, stringETA));
                    displayCount++;
                }
                arrivalsIndex++;
            }

            return new BusResponse(serverTime, busList);

        } catch (Exception e) {
            e.printStackTrace();
            return new BusResponse("Error parsing data", new ArrayList<>());
        }
    }

    private String timeToString(long millisToTime) {
        Instant instant = Instant.ofEpochMilli(millisToTime);
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return time.format(formatter);
    }
}

class BusResponse {
    public String serverTime;
    public List<BusArrival> buses;

    public BusResponse(String serverTime, List<BusArrival> buses) {
        this.serverTime = serverTime;
        this.buses = buses;
    }
}

class BusArrival {
    public String routeShortName;
    public int minutesUntil;
    public String arrivalTime;

    public BusArrival(String routeShortName, int minutesUntil, String arrivalTime) {
        this.routeShortName = routeShortName;
        this.minutesUntil = minutesUntil;
        this.arrivalTime = arrivalTime;
    }
}
