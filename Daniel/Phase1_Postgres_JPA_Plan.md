# Phase 1 — Core Platform: PostgreSQL + Spring Boot (JPA + Flyway)

Goal
- Provision a local PostgreSQL database and connect Spring Boot with JPA.
- Ensure Flyway applies the existing migration (src/main/resources/db/migration/V1__initial_schema.sql).
- Verify the application starts with the `dev` profile and JPA can query the schema.

Assumptions
- Repo already contains: `V1__initial_schema.sql`, `application-dev.properties` configured for Postgres, and Postgres JDBC driver in `pom.xml`.
- Development machine: Windows with PowerShell, Maven, JDK 11.

Quick checklist
- [ ] Install Postgres locally (psql available).
- [ ] Add Flyway dependency to pom.xml.
- [ ] Create DB role and database: `sharedbasket` / `sharedbasket_dev`.
- [ ] Start Spring Boot with `dev` profile and confirm Flyway applied V1.

Files to edit
- pom.xml — add Flyway:

```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

- Verify: src/main/resources/application-dev.properties (should contain correct JDBC URL, credentials, and flyway settings).

PowerShell: create DB user + DB
1. Open PowerShell as a user that can run psql (or use postgres superuser):

psql -U postgres -h localhost

Then in psql:

CREATE ROLE sharedbasket WITH LOGIN PASSWORD 'sharedbasket';
CREATE DATABASE sharedbasket_dev OWNER sharedbasket;
GRANT ALL PRIVILEGES ON DATABASE sharedbasket_dev TO sharedbasket;
\q

(If psql is not on PATH, run via pgAdmin or the PostgreSQL installer tools.)

Start the Spring Boot app (dev profile)
Option A (environment variable for current session):

$env:SPRING_PROFILES_ACTIVE='dev'
mvn spring-boot:run

Option B (PowerShell-safe one-liner):

mvn --% spring-boot:run -Dspring-boot.run.profiles=dev

What to expect (verification)
- Flyway logs showing application of `V1__initial_schema.sql` and creation of `flyway_schema_history`.
- Tables from V1 present: `app_user`, `sb_group`, `group_member`, `category`, `shopping_item`, `purchase_record`.
- JPA/Hibernate connects successfully (no connection errors in logs).

Verify with psql

psql -U sharedbasket -h localhost -d sharedbasket_dev
\dt
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
SELECT count(*) FROM app_user;

Optional quick runtime check (dev-only)
Add a small CommandLineRunner bean to print a count from `app_user` for fast verification (only active in `dev` profile):

```java
@Component
@Profile("dev")
public class DBCheckRunner implements CommandLineRunner {
  @Autowired
  private JdbcTemplate jdbc;

  @Override
  public void run(String... args) {
	Integer cnt = jdbc.queryForObject("SELECT count(*) FROM app_user", Integer.class);
	System.out.println("app_user rows: " + cnt);
  }
}
```

Next backend tasks for Phase 1
- Implement JPA entities mapping the migration schema: AppUser, SbGroup, GroupMember, Category, ShoppingItem, PurchaseRecord.
- Create Spring Data repositories for each entity.
- Add DTOs and mappers (MapStruct recommended).
- Validation annotations, shared exceptions, and base REST contracts.
- Integration tests (use Testcontainers for CI or local test runs).

Risks & notes
- Default `application.properties` contains H2 settings; always activate `dev` profile when using Postgres.
- Port 5432 conflicts: stop other Postgres instances or change the port in `application-dev.properties`.
- If Flyway dependency is not added, migrations will not run; ensure `flyway-core` is added to pom.

Commands summary
- Create DB (psql): use the commands above.
- Build: `mvn clean package`
- Run with dev profile: `mvn spring-boot:run -Dspring-boot.run.profiles=dev` or set env var.

Contact
- If you want, I can apply the pom.xml change and add the optional DBCheckRunner to the repo.
