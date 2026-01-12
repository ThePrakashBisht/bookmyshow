
---

# 🎬 BookMyShow – Distributed Ticket Booking System

A **microservices-based ticket booking platform** similar to BookMyShow, built with **Spring Boot** and modern **cloud-native technologies**.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

---

## 🏗️ Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND                                                    │
│ (React / Next.js)                                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│ Spring Cloud Gateway (Auth, Rate Limiting)                  │
│ Port: 8080                                                  │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ User Service │   │ Event Service│   │ Booking      │
│ (8081)       │   │ (8082)       │   │ Service      │
│ • Auth/JWT   │   │ • Movies     │   │ (8083)       │
│ • Profiles   │   │ • Shows      │   │ • Seat Lock  │
│ • Roles      │   │ • Venues     │   │ • Payments   │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                  │                  │
       ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ PostgreSQL   │   │ PostgreSQL   │   │ PostgreSQL   │
│ Users DB     │   │ Events DB    │   │ Bookings DB │
└──────────────┘   └──────────────┘   └──────────────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
        ┌──────────────┐       ┌──────────────┐
        │ Redis        │       │ Kafka         │
        │ • Caching    │       │ • Events      │
        │ • Seat Locks │       │ • Async       │
        │ • Rate Limit │       │ • Notifications│
        └──────────────┘       └──────────────┘
```

---

## ✨ Key Features

### 🔐 User Service

* JWT-based authentication
* Role-based access control (USER, ADMIN)
* User registration & profile management
* Password encryption with BCrypt

### 🎬 Event Service

* Movie / Event catalog management
* Venue & screen management
* Show scheduling
* City-wise event listing
* Seat layout configuration

### 🎫 Booking Service

* **Distributed seat locking with Redis** (prevents double booking)
* Booking timeout handling
* Payment integration ready
* Booking history

### 🚪 API Gateway

* Centralized routing
* JWT validation at gateway level
* Rate limiting with Redis
* Circuit breaker (Resilience4j)
* Request logging with correlation IDs

---

## 🛠️ Tech Stack

| Category         | Technology              |
| ---------------- | ----------------------- |
| Language         | Java 17                 |
| Framework        | Spring Boot 3.2         |
| API Gateway      | Spring Cloud Gateway    |
| Database         | PostgreSQL 15           |
| Caching          | Redis 7                 |
| Messaging        | Apache Kafka            |
| Security         | Spring Security + JWT   |
| Resilience       | Resilience4j            |
| Containerization | Docker & Docker Compose |
| Build Tool       | Maven                   |

---

## 🚀 Quick Start

### Prerequisites

* Java 17+
* Docker & Docker Compose
* Maven 3.8+

### Run with Docker

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/bookmyshow-backend.git
cd bookmyshow-backend

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f api-gateway
```

### 🧑‍💻 Run for Development

```bash
# Start infrastructure only
docker-compose -f docker-compose.dev.yml up -d
```

Run services in IDE (in order):

1. UserServiceApplication (8081)
2. EventServiceApplication (8082)
3. BookingServiceApplication (8083)
4. GatewayApplication (8080)

---

## 📡 API Endpoints

### Public Endpoints (No Auth Required)

| Method | Endpoint                   | Description         |
| ------ | -------------------------- | ------------------- |
| POST   | /api/auth/register         | Register new user   |
| POST   | /api/auth/login            | User login          |
| GET    | /api/cities                | Get all cities      |
| GET    | /api/events                | Get all events      |
| GET    | /api/events/type/{type}    | Get events by type  |
| GET    | /api/shows/event/{eventId} | Get shows for event |

### 🔐 Protected Endpoints (Auth Required)

| Method | Endpoint                    | Description       |
| ------ | --------------------------- | ----------------- |
| GET    | /api/users/profile          | Get user profile  |
| POST   | /api/bookings/initiate      | Start booking     |
| POST   | /api/bookings/{id}/confirm  | Confirm payment   |
| GET    | /api/bookings/user/{userId} | Get user bookings |

---

## 🔒 Distributed Lock Solution (Redis)

```java
public BookingResult bookSeat(String seatId, String userId) {
    String lockKey = "lock:seat:" + seatId;

    boolean locked = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, userId, Duration.ofMinutes(5));

    if (!locked) {
        return BookingResult.SEAT_ALREADY_LOCKED;
    }

    try {
        // Process booking
        return BookingResult.SUCCESS;
    } finally {
        redisTemplate.delete(lockKey);
    }
}
```

---

## 📊 Monitoring

* Health: `GET /actuator/health`
* Metrics: `GET /actuator/metrics`
* Circuit Breakers: `GET /actuator/circuitbreakers`

---

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# E2E tests
./test-api.sh
```

---

## 📁 Project Structure

```text
bookmyshow/
├── api-gateway/
├── user-service/
├── event-service/
├── booking-service/
├── docker-compose.yml
├── docker-compose.dev.yml
└── README.md
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch

   ```bash
   git checkout -b feature/amazing-feature
   ```
3. Commit changes

   ```bash
   git commit -m "Add amazing feature"
   ```
4. Push to branch

   ```bash
   git push origin feature/amazing-feature
   ```
5. Open a Pull Request

---

## 📝 License

This project is licensed under the **MIT License**.

---

## 👨‍💻 Author

**Prakash Bisht**
GitHub: [https://github.com/ThePrakashBisht](https://github.com/ThePrakashBisht)
LinkedIn: [https://www.linkedin.com/in/theprakashbisht/](https://www.linkedin.com/in/theprakashbisht/)

---

