# Java User Registration (Multi-Branch Architecture Evolution)

A Java project demonstrating the architectural evolution of data access patterns connecting to MySQL:
From **Pure JDBC** to **Spring JDBC (`JdbcClient`)**, and finally to **Spring Data JDBC (`CrudRepository`)**.

## Branches Overview

| Branch | Technology | Highlights |
| :--- | :--- | :--- |
| **`main`** | **Pure JDBC** | Pure JDBC with `PreparedStatement`, `ResultSet`, connection pool (HikariCP), DAO, Service, and Facade patterns. |
| **`spring-jdbc`** | **Spring JDBC** | Eliminates connection boilerplate using `JdbcClient`, `SimpleJdbcInsert`, Spring DI, and declarative `@Transactional`. |
| **`spring-data-jdbc`** | **Spring Data JDBC** | Eliminates manual DAO classes entirely (0 boilerplate lines). Auto CRUD generation via `CrudRepository` without Hibernate ORM overhead. |

## Database Setup (Docker)

```bash
docker run -d \
  --name mysql-db \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=appdb \
  -e MYSQL_USER=agung \
  -e MYSQL_PASSWORD=agung12345 \
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql \
  mysql:8.4
```

Database schema (`schema.sql`) and seed data (`data.sql`) for provinces and cities are automatically initialized upon startup.

## Running the Application

```bash
# Run console demo
mvn exec:java

# Run tests
mvn test
```
