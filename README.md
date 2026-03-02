# SEQ Transit – Backend Server

A Spring Boot REST API that serves scheduled and real-time public transport data for South East Queensland.

The server queries:

- Static GTFS data stored in a Neon-hosted PostgreSQL database
- Live GTFS-Realtime feeds from Translink

It is designed for personal use to be hosted on Google Cloud Run, strictly adhering to free-tier cloud constraints.

---

## Overview

<img width="820" height="180" alt="D2" src="https://github.com/user-attachments/assets/3cf2c2e8-fe71-4359-94da-9b9bee232bca" />

Within the SEQ Transit pipeline, `seq-transit-server` is responsible for:

- Querying the PostgreSQL database for static transit data, including stops, services, and scheduled arrivals
- Lazily fetching real-time arrival times from the GTFS-RT feed, ensuring that no more than one fetch occurs within a 60-second period
- (currently disabled) Caching API responses in Redis to improve performance and reduce load
- Exposing RESTful API endpoints for the mobile app to consume real-time and static transit data

Companion Projects:
- [Static Loader](https://github.com/wychesterso/seq-transit-static-loader)
- [Mobile App](https://github.com/wychesterso/seq-transit-app)

---

## API Endpoints

### 1. **Get Services by Prefix**  
`GET /services/prefix`  
Retrieve a list of service groups based on a prefix search term. The results are sorted by proximity to the nearest stop.

- **Parameters:**
  - `prefix` (required): A prefix to search for in the route's name (e.g. 412).
  
- **Response:**  
  A list of **BriefServiceResponse** objects, containing service information

### 2. **Get Services at a Specific Stop**  
`GET /services/stop`  
Retrieve a list of service groups that stop at a particular stop.

- **Parameters:**
  - `id` (required): The ID of the stop to query.
  
- **Response:**  
  A list of **ServiceResponse** objects, containing:
  - Service information
  - Next 3 arrivals at the queried stop  

### 3. **Get Nearest Services**  
`GET /services/nearest`  
Retrieve a list of service groups that stop near a given set of coordinates (latitude and longitude). The results are sorted by the nearest stop's proximity to the query point.

- **Parameters:**
  - `lat` (required): Latitude of the query point.
  - `lon` (required): Longitude of the query point.
  - `radius` (required): Search radius (in meters) to consider for nearby stops.

- **Response:**  
  A list of **ServiceResponse** objects, containing:
  - Service information  
  - Its nearest stop to the query point
  - Next 3 arrivals at the nearest stop  

### 4. **Get Full Service Information**  
`GET /services/info`  
Retrieve detailed information for a service, including its canonical stop list and the next 3 arrivals for each stop in sequence.

- **Parameters:**
  - `route` (required): Route number or short name.
  - `headsign` (required): The service's headsign.
  - `dir` (required): Direction ID of the service.
  - `lat` (required): Latitude of the query point (for nearest stop calculation).
  - `lon` (required): Longitude of the query point (for nearest stop calculation).

- **Response:**  
  A **FullServiceResponse** object, containing:
  - Service information
  - Service route (as a list of coordinate points)
  - Canonical list of stops for the service
  - The next 3 arrivals for each stop in the sequence
  - The nearest stop to the query point

### 5. **Get Nearest Stops**  
`GET /stops/nearest`  
Retrieve a list of stops near a given set of coordinates (latitude and longitude). The results are sorted by proximity to the query point.

- **Parameters:**
  - `lat` (required): Latitude of the query point.
  - `lon` (required): Longitude of the query point.
  - `radius` (required): Search radius (in meters) to consider for nearby stops.

- **Response:**  
  A list of **BriefStopResponse** objects, containing stop information

---

## Data Source

Static GTFS data is sourced from: **[https://gtfsrt.api.translink.com.au/GTFS/SEQ_GTFS.zip](https://gtfsrt.api.translink.com.au/GTFS/SEQ_GTFS.zip)**  
Real-time GTFS feed is sourced from: **[https://gtfsrt.api.translink.com.au/api/realtime/SEQ/TripUpdates](https://gtfsrt.api.translink.com.au/api/realtime/SEQ/TripUpdates)**

---

## Tech Stack

- Java 17
- Spring Boot 3.5.9
- Gradle
- Docker
- PostgreSQL (org.postgresql:postgresql)
- Redis

---

## Cloud Architecture

The system was redesigned specifically to operate within free-tier limits:  
→ Cloud Run (0–1 instances)  
→ PostgreSQL (Neon)

Cold starts are acceptable and expected for this personal-use system.

Deployment:

```bash
docker build -t REGION-docker.pkg.dev/PROJECT_ID/REPOSITORY/SERVICE_NAME:TAG .
docker push REGION-docker.pkg.dev/PROJECT_ID/REPOSITORY/SERVICE_NAME:TAG .
gcloud run deploy SERVICE_NAME \
  --image REGION-docker.pkg.dev/PROJECT_ID/REPOSITORY/SERVICE_NAME:TAG \
  --region REGION \
```

---

## Environment Variables

Required configuration:

```bash
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
```
