// by noah nguyen
// 12/16/2025
// BUS TRACKER
// uses onebusaway api for real time data
// noahnguyen006@gmail.com

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <time.h>

const char* WIFI_SSID = "28hotties";
const char* WIFI_PASSWORD = "c@ptainNemo26";
const char* API_KEY = "REMOVED";
const char* STOP_ID = "1_38024";
const char* ROUTE_NUMBER = "45";
const int UPDATE_INTERVAL = 60000;

unsigned long lastUpdateTime = 0;

//////////////////////////////////////////////////////////
void setup() {
  Serial.begin(115200);
  connectToWiFi();
  syncTime();
}

//////////////////////////////////////////////////////////
void loop() {
  unsigned long currTime = millis(); // seconds after init boot

  if (currTime - lastUpdateTime >= UPDATE_INTERVAL) { // call api every 60 sec
    String jsonResponse = fetchBusData();
    if (jsonResponse.length() > 0) { // if there is a bus
      int minutesTillBus = parseNextArrival(jsonResponse);
      displayBusInfo(minutesTillBus);
    }
    lastUpdateTime = currTime;
  }
  delay(100);
}

//////////////////////////////////////////////////////////
void syncTime() {
  Serial.println("Syncing time with NTP servers...");
  
  configTime(-8 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  
  Serial.print("Waiting for time sync");
  int attempts = 0;
  while (time(nullptr) < 100000 && attempts < 10) {
    delay(1000);
    Serial.print(".");
    attempts++;
  }
  Serial.println();
  
  time_t now = time(nullptr);
  Serial.print("Current time: ");
  Serial.println(ctime(&now));
}

//////////////////////////////////////////////////////////
void connectToWiFi() {
  Serial.print("Connecting to WiFi: ");
  Serial.println(WIFI_SSID);
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    
    attempts++;
    
    if (attempts > 20) {
      Serial.println("\nERROR: Could not connect to WiFi!");
      return;
    }
  }
  
  Serial.println("\nWiFi connected!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
}

//////////////////////////////////////////////////////////
String fetchBusData() {
  String url = "https://api.pugetsound.onebusaway.org/api/where/";
  url += "arrivals-and-departures-for-stop/";
  url += STOP_ID;
  url += ".json?key=";
  url += API_KEY;
  url += "&minutesAfter=60";
  
  Serial.print("API URL: ");
  Serial.println(url);
  
  HTTPClient http;
  
  http.begin(url);
  
  http.setTimeout(10000);
  
  int httpCode = http.GET();
  
  if (httpCode == 200) {  // 200 = HTTP OK
    String payload = http.getString();  
    http.end(); 
    
    Serial.println("API request successful!");
    return payload;
    
  } else {
    // Request failed
    Serial.print("API request failed! HTTP code: ");
    Serial.println(httpCode);
    
    if (httpCode == -1) {
      Serial.println("(Connection timeout or DNS failure)");
    }
    
    http.end();
    return ""; 
  }
}

//////////////////////////////////////////////////////////
// returns min till next bus
int parseNextArrival(String jsonString) {
  JsonDocument doc;
  
  // Parse the JSON string
  DeserializationError error = deserializeJson(doc, jsonString);
  
  // Check if parsing failed
  if (error) {
    Serial.print("JSON parsing failed: ");
    Serial.println(error.c_str());
    return -1;
  }
  
  JsonArray arrivals = doc["data"]["entry"]["arrivalsAndDepartures"];
  
  if (arrivals.size() == 0) {
    Serial.println("No bus arrivals found!");
    return -1;
  }
  
  // loop through all arrivals to find route 45
  for (JsonObject arrival : arrivals) {
    String route = arrival["routeShortName"];
    
    if (route == ROUTE_NUMBER) {
      long long arrivalTimeMs = arrival["predictedArrivalTime"];
      
      // if no prediction, use scheduled time
      if (arrivalTimeMs == 0) {
        arrivalTimeMs = arrival["scheduledArrivalTime"];
      }
      
      time_t now;
      time(&now);
      long long currentTimeMs = (long long)now * 1000;  // Convert to milliseconds
      
      long long differenceMs = arrivalTimeMs - currentTimeMs;
      
      int minutes = differenceMs / 1000 / 60;
      
      if (minutes < 0) {
        minutes = 0;
      }
      
      Serial.print("Found Route ");
      Serial.print(ROUTE_NUMBER);
      Serial.print(" arrival in ");
      Serial.print(minutes);
      Serial.println(" minutes");
      
      return minutes;
    }
  }
  // dnf route
  Serial.print("Route ");
  Serial.print(ROUTE_NUMBER);
  Serial.println(" not found in arrivals");
  Serial.println(arrival in ## minutes);
  return -1;
} 

//////////////////////////////////////////////////////////
void displayBusInfo(int minutes) {
  Serial.println("\n=============================");
  Serial.println("    BUS ARRIVAL    ");
  Serial.println("=============================");
  Serial.print("Route: ");
  Serial.println(ROUTE_NUMBER);
  Serial.print("Stop ID: ");
  Serial.println(STOP_ID);
  Serial.println("-----------------------------");
  
  if (minutes == -1) {
    Serial.println("Status: ERROR");
    Serial.println("Could not get bus data");
  } else if (minutes == 0) {
    Serial.println("Status: → NOW!");
  } else if (minutes == 1) {
    Serial.println("Status: → 1 minute");
  } else {
    Serial.print("Status: → ");
    Serial.print(minutes);
    Serial.println(" minutes");
  }
  
  time_t now = time(nullptr);
  Serial.print("Updated: ");
  Serial.print(ctime(&now));
  
  Serial.println("=============================\n");
}