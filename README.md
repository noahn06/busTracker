## Bus Tracker | Full-Stack Web Application

Bus Tracker is a real-time web application that allows the user to view live bus arrival and tracking information using the OneBusAway API. The backend is built with Spring Boot and exposes REST endpoints, while the frontend provides a simple, responsive interface for interacting with transit data. The tracker is easily scalable to display as many arrivals, different stops, and different bus routes as provided by the OneBusAway API. For fun I also displayed the data in the console with the busTrackerToConsole.java file.

This is my first project ever! I built this project with the sole purpose of using it for the Route 45 bus stop that I take to and from the University of Washington every morning. I was sick and tired of having to pull out my phone, going to the OneBusAway app, and then navigating to my singular Route 45 bus stop only to see that the next bus is arriving NOW while I'm still getting dressed with a bagel halfway in my mouth. I was tired of being late to school so I built this to help increase my quality of life! It was a fun project and there are a few more things that I would like to implement like a weather display that will tell me what to wear depending on the temperature outside.

The project is designed to demonstrate full-stack development concepts, including RESTful APIs, frontend–backend integration, and deployment using GitHub Pages.

### Features
- Real-time bus arrival and ETA according to API server-time
- Manual and automatic (60 seconds) refresh options
- Clean, responsive user interface
- RESTful API built with Spring Boot
- Static frontend deployed via GitHub Pages

### Tech Stack
- **Backend:** Java, Spring Boot
- **Frontend:** HTML, CSS, JavaScript
- **API:** OneBusAway REST API
- **Deployment:** GitHub Pages (frontend), local Spring Boot server (backend)

### What I Learned
- How to structure a Spring Boot application to cleanly separate API and frontend concerns
- Best practices for serving static resources and debugging 404 routing issues
- Managing multiple deployment targets (local backend vs. GitHub Pages frontend)
- Designing user-facing features around real-time data constraints

### Screenshots

Below are screenshots showcasing the core functionality of the application.

#### Main Tracker View
![Main Tracker View](docs/screenshots/trackerHome.png)

#### Auto-Refresh
![Auto Refresh Enabled](docs/screenshots/trackerRefresh.png)

### Live Demo
🔗 https://noahn06.github.io/busTracker/

### Running Locally
1. Clone the repository
2. Obtain API key by emailing oba_api_key@soundtransit.org with name, email and agreement to terms of use
3. Put API key into .env file as API_KEY=API KEY
4. Open repo with any IDE
5. Navigate to -> src\main\java\com\example\bustracker\busTrackerApplication.java 
6. Hit Run
7. Open `http://localhost:8080` in your browser
8. Witness the real-time Route 45 arrivals outside of my house!

WEB BASED BUS TRACKER NON-FUNCTIONAL, NEED TO GET BACKEND SERVER TO HOST API REQUESTS