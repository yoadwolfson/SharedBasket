# 🛒 SharedBasket

> A collaborative shopping list management application built with **Spring Boot**, **JSF**, **PostgreSQL**, and **Docker**.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Running Locally with Docker](#-running-locally-with-docker)
- [Running Locally with Spring Boot (Dev Mode)](#-running-locally-with-spring-boot-dev-mode)
- [Access the Application](#-access-the-application)
- [Project Structure](#-project-structure)
- [Database Migrations](#-database-migrations)

---

## 📖 Overview

SharedBasket allows groups of users to collaboratively manage shared shopping lists. Users can create groups, add items to a shared basket, request items, categorise them, and get smart recommendations.

---

## 🛠 Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 11                             |
| Framework    | Spring Boot 2.7.13                  |
| View Layer   | JSF (JavaServer Faces) + JSTL       |
| ORM          | Hibernate 5.6 / Spring Data JPA     |
| Database     | PostgreSQL 15                       |
| Migrations   | Flyway                              |
| Build        | Maven 3                             |
| Packaging    | WAR → Tomcat 9 (embedded)           |
| Containers   | Docker & Docker Compose             |

---

## ✅ Prerequisites

Make sure the following are installed on your machine:

| Tool              | Version       | Download                                        |
|-------------------|---------------|-------------------------------------------------|
| Docker Desktop    | Latest        | https://www.docker.com/products/docker-desktop  |
| Docker Compose    | v2+           | Bundled with Docker Desktop                     |
| Java JDK          | 11            | https://adoptium.net/                           |
| Maven             | 3.9+          | https://maven.apache.org/download.cgi           |

> **Note:** Java and Maven are only required if you want to run the app _outside_ Docker.

---

## 🐳 Running Locally with Docker

This is the **recommended** way to run SharedBasket. Everything — the app and the database — starts with a single command.

### 1. Clone the repository

```bash
git clone https://github.com/yoadwolfson/SharedBasket.git
cd SharedBasket
```

### 2. Start the full stack

```bash
docker compose up --build
```

Docker Compose will:
1. Pull the **PostgreSQL 15** image and start the database.
2. Build the **Spring Boot / Tomcat** image from the `Dockerfile`.
3. Apply all **Flyway** database migrations automatically on startup.
4. Expose the app on **port 8080**.

> The first build may take a few minutes while Maven downloads dependencies.

### 3. Open the application

➡️ **[http://localhost:8080/](http://localhost:8080/)**

### 4. Stop the stack

```bash
docker compose down
```

To also remove the persistent database volume:

```bash
docker compose down -v
```

---

## 🚀 Running Locally with Spring Boot (Dev Mode)

Use this approach if you want faster iteration without rebuilding the Docker image.

### 1. Start only the database

```bash
docker compose up -d db
```

This starts PostgreSQL on `localhost:5433`.

### 2. Run the application with the `dev` profile

**macOS / Linux:**

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

**Windows (PowerShell):**

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
mvn spring-boot:run
```

**Windows (Command Prompt):**

```cmd
set SPRING_PROFILES_ACTIVE=dev && mvn spring-boot:run
```

The `dev` profile connects to the local PostgreSQL container and enables Flyway migrations.

### 3. Open the application

➡️ **[http://localhost:8080/](http://localhost:8080/)**

---

## 🌐 Access the Application

Once the application is running, navigate to:

### 👉 [http://localhost:8080/](http://localhost:8080/)

| Mode                    | URL                                             |
|-------------------------|-------------------------------------------------|
| 🐳 Full Docker Stack    | [http://localhost:8080/](http://localhost:8080/) |
| ☕ Spring Boot Dev Mode | [http://localhost:8080/](http://localhost:8080/) |
| 🗄️ H2 Console (default) | [http://localhost:8081/h2-console](http://localhost:8081/h2-console) |
| 🐘 PostgreSQL (dev)     | `localhost:5433` (user: `sharedbasket`, db: `sharedbasket_dev`) |

---

## 📁 Project Structure

```
SharedBasket/
├── src/
│   └── main/
│       ├── java/com/sharedbasket/
│       │   ├── controller/        # Spring MVC / REST controllers
│       │   ├── service/           # Business logic interfaces & implementations
│       │   ├── model/             # JPA entity classes
│       │   ├── repository/        # Spring Data JPA repositories
│       │   └── exception/         # Global exception handling
│       ├── resources/
│       │   ├── application.properties         # Default (H2, in-memory)
│       │   ├── application-dev.properties     # Dev profile (PostgreSQL)
│       │   └── db/migration/                  # Flyway SQL migrations
│       └── webapp/
│           └── WEB-INF/           # JSF views (.xhtml / .jsp)
├── Dockerfile                     # Multi-stage build: Maven → Tomcat
├── docker-compose.yml             # App + PostgreSQL services
├── pom.xml                        # Maven dependencies & build config
└── README.md
```

---

## 🗄️ Database Migrations

Flyway migrations are applied automatically on startup. Migration scripts are located in:

```
src/main/resources/db/migration/
```

| Migration | Description                          |
|-----------|--------------------------------------|
| V1        | Initial schema                       |
| V2        | Optimistic locking (phase 4)         |
| V3        | Shopping item requests               |
| V4        | Item unit enum                       |
| V5        | Category emoji & drop sort order     |
| V6        | Seed default categories              |

---

## 🧪 Running Tests

```bash
# Unit tests only (E2E tests excluded by default)
mvn test

# All tests including E2E integration tests
mvn verify
```

---

> Built with ❤️ — SharedBasket makes group shopping effortless.
