# Local database for development

The dev database is configured for PostgreSQL running in Docker. The app uses the
settings from src/main/resources/application-dev.properties:

- JDBC URL: jdbc:postgresql://localhost:5432/sharedbasket_dev
- Username: sharedbasket
- Password: sharedbasket
- Flyway: enabled with migrations from src/main/resources/db/migration

1) Start Postgres via Docker Compose

   docker-compose up -d

   This starts the PostgreSQL container on localhost:5432 with database name
   sharedbasket_dev and credentials sharedbasket / sharedbasket.

2) Run the application with the 'dev' profile

   $env:SPRING_PROFILES_ACTIVE='dev'
   mvn spring-boot:run

   If you prefer a one-liner in PowerShell, use:

   mvn --% spring-boot:run -Dspring-boot.run.profiles=dev

   If Docker Compose is already running an older app container, recreate it so
   it picks up the new port mapping:

   docker compose up -d --build --force-recreate app

   Flyway applies migrations from src/main/resources/db/migration automatically.

3) Verify

   - Check the application logs for Flyway migration success and a PostgreSQL connection.
   - Connect with psql: psql -h localhost -U sharedbasket -d sharedbasket_dev
   - Optional: check the containers with `docker ps` and view logs with `docker compose logs -f db`.
