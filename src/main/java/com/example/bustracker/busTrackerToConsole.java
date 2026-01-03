package com.example.bustracker;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

import org.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class busTrackerToConsole {

    public static final String STOP_ID = "1_38024";
    public static final int NUM_OF_ARRIVALS = 3;

    public static void main(String[] args) {
        String url = LinkBuilder();
        String APIData = fetchAPIData(url);
        fetchAndDisplayRouteData(APIData);
        System.out.println();
    }

    public static String LinkBuilder() {

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("API_KEY");

        String url = "https://api.pugetsound.onebusaway.org/api/where/";
        url += "arrivals-and-departures-for-stop/";
        url += STOP_ID;
        url += ".json?key=";
        url += apiKey;
        url += "&minutesAfter=60";

        return url;
    }

    public static String fetchAPIData(String url) {

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
                System.out.println("API request successful ");
                return payload;
            } else {
                System.out.println("API request failed HTTP code: " + httpCode);
                return "";
            }
        } catch (java.net.http.HttpTimeoutException e) {
            System.out.println("API request failed (Connection timeout) ");
            e.printStackTrace();
            return "";
        } catch (Exception e) {
            System.out.println ("API request failed (Other error)");
            e.printStackTrace();
            return "";
        }
    }

    public static void fetchAndDisplayRouteData(String APIData) {
        JSONObject root = new JSONObject(APIData);

        //server clock at top
        long currentTimeMillis = root.getLong("currentTime");
        String serverTime = timeToString(currentTimeMillis);

        System.out.println("Server Time: " + serverTime);
        System.out.println();
        JSONArray arrivals = root.getJSONObject("data")
                                 .getJSONObject("entry")
                                 .getJSONArray("arrivalsAndDepartures");

        if (arrivals.length() == 0) {
            System.out.println("No arrivals found!");
        } else {
            int displayCount = 0;
            int arrivalsIndex = 0;
            while (displayCount < NUM_OF_ARRIVALS && arrivalsIndex < arrivals.length()) {
                JSONObject arrivingBus = arrivals.getJSONObject(arrivalsIndex);

                long arrivalTimeMillis = 0;

                if (arrivingBus.has("predictedArrivalTime")) {
                    arrivalTimeMillis = arrivingBus.getLong("predictedArrivalTime");
                } else {
                    arrivalTimeMillis = arrivingBus.getLong("scheduledArrivalTime");
                }

                int minsTillArrival = (int) ((arrivalTimeMillis - currentTimeMillis) / 60000); // minutes until arrival
                if (minsTillArrival >= 0) { // display only when bus has not arrived yet 
                    String routeShortName = arrivingBus.getString("routeShortName");
                    String stringETA = timeToString(arrivalTimeMillis);

                    System.out.println(arrivalDisplay(routeShortName, minsTillArrival, stringETA));
                    displayCount++;
                }
                arrivalsIndex++;
            }
        }
    }

    private static String arrivalDisplay(String routeShortName, int minsTillArrival, String stringETA) {

        String display = "///////////////////////////////////\n";
        display += "\n Route: " + routeShortName + "\n";
        display += "  Coming in: > ";
        if (minsTillArrival == 0) {
            display += "NOW! \n";
        } else {
            display += minsTillArrival + " min \n";
        } 
        display += "  ETA: " + stringETA + "\n";

        return display;
    }

    private static String timeToString(long millisToTime) {

        Instant instant = Instant.ofEpochMilli(millisToTime);
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String timeString = time.format(formatter);

        return timeString;
    }
}
