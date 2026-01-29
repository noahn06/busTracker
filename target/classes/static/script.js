let autoRefreshInterval;
let countdownInterval;
let secondsRemaining = 60;

loadBuses();
toggleAutoRefresh();
loadWeather();

function loadBuses() {
  document.getElementById("output").innerHTML = "Loading...";

  fetch("/api/buses")
    .then((response) => response.json())
    .then((data) => {
      displayBuses(data);
      resetCountdown();
    })
    .catch((error) => {
      document.getElementById("output").innerHTML = "Error loading buses";
      console.error("Error:", error);
    });
}

function displayBuses(data) {
  let output = "";

  output += '<div class="call">API Call</div>';
  output += '<div class="success">API request successful</div>';
  output +=
    '<div class="server-time">Server Time: ' + data.serverTime + "</div>";

  data.buses.forEach((bus) => {
    const minutesText =
      bus.minutesUntil === 0 ? "NOW!" : bus.minutesUntil + " min";

    output += '<div class="bus-card">';
    output += "///////////////////////////////////\n";
    output += " Route: " + bus.routeShortName + "\n";
    output += "  Coming in: > " + minutesText + "\n";
    output += "  ETA: " + bus.arrivalTime + "\n";
    output += "</div>";
  });

  if (data.buses.length === 0) {
    output += "<div>No arrivals found!</div>";
  }

  document.getElementById("output").innerHTML = output;
}



function loadWeather() {
  document.getElementById("weather-bar").innerHTML = "Loading weather...";

  fetch("/api/weather")
    .then((response) => response.json())
    .then((data) => {
      displayWeather(data);
    })
    .catch((error) => {
      document.getElementById("weather-bar").innerHTML = "Error loading weather";
      console.error("Weather error:", error);
    });
}

function displayWeather(data) {
  let output = "";

  const currentHour = new Date().getHours();

  data.hourly.forEach((hour) => {
    const hourNum = parseInt(hour.time.split("T")[1].split(":")[0]);

    const isPast = hourNum < currentHour;

    const time12hr = convertTo12Hour(hour.time);

    output += '<div class="weather-hour' + (isPast ? ' past' : '') + '">';
    output += "/////////////////\n";
    output += " " + time12hr + "\n";
    output += "  Temp: " + Math.round(hour.temperature) + "°F\n";
    output += "  Feels: " + Math.round(hour.feelsLike) + "°F\n";
    output += "  Rain Prob: " + hour.precipProbability + "%\n";
    output += "  Rain: " + hour.precipitation + " in\n";
    output += "/////////////////\n";
    output += "</div>";
  });

  document.getElementById("weather-bar").innerHTML = output;
}

function convertTo12Hour(timeString) {
  const hour24 = parseInt(timeString.split("T")[1].split(":")[0]);

  if (hour24 === 0) {
    return "12 AM";
  } else if (hour24 < 12) {
    return hour24 + " AM";
  } else if (hour24 === 12) {
    return "12 PM";
  } else {
    return (hour24 - 12) + " PM";
  }
}

function resetCountdown() {
  secondsRemaining = 60;
  updateCountdownDisplay();
}

function updateCountdownDisplay() {
  const label = document.querySelector("label");
  if (document.getElementById("autoRefresh").checked) {
    label.innerHTML = `<input type="checkbox" id="autoRefresh" checked onchange="toggleAutoRefresh()"> Auto-refresh (${secondsRemaining}s)`;
  } else {
    label.innerHTML = `<input type="checkbox" id="autoRefresh" onchange="toggleAutoRefresh()"> Auto-refresh (paused at ${secondsRemaining}s)`;
  }
}

function startCountdown() {
  clearInterval(countdownInterval);
  countdownInterval = setInterval(() => {
    secondsRemaining--;
    updateCountdownDisplay();
    if (secondsRemaining <= 0) {
      secondsRemaining = 60;
    }
  }, 1000);
}

function refreshNow() {
  clearInterval(autoRefreshInterval);
  clearInterval(countdownInterval);
  loadBuses();
  if (document.getElementById("autoRefresh").checked) {
    autoRefreshInterval = setInterval(loadBuses, 60000);
    startCountdown();
  }
}

function toggleAutoRefresh() {
  clearInterval(autoRefreshInterval);
  clearInterval(countdownInterval);
  if (document.getElementById("autoRefresh").checked) {
    autoRefreshInterval = setInterval(loadBuses, 60000);
    startCountdown();
  }
  updateCountdownDisplay();
}
