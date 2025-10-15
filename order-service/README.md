# Order Service

Order service part of e-commerce microservices system.

## Features

- Order management
- Order item management
- Order status tracking
- Integration with other microservices
- JWT authentication
- Internationalization (i18n) support
- Swagger/OpenAPI documentation
- Database migration with Liquibase
- RabbitMQ integration
- Redis caching

## Technology Stack

- Java 21
- Spring Boot 3.4.1
- Spring Security
- Spring Data JPA
- MySQL
- Redis
- RabbitMQ
- Liquibase
- Swagger/OpenAPI
- Maven

## Configuration

### Database

- Development: `order_service_dev`
- Production: `order_service_prod`

### Port

- Default: 8085

### External Services

- Cart Service: http://localhost:8084
- Product Service: http://localhost:8083
- User Service: http://localhost:8082

## API Documentation

Once the service is running, you can access:

- Swagger UI: http://localhost:8085/swagger-ui.html
- API Docs: http://localhost:8085/v3/api-docs

## Running the Service

```bash
mvn spring-boot:run
```

## Testing

```bash
mvn test
```

## Database Migration

The service uses Liquibase for database migrations. Migrations are automatically applied on startup.

## Internationalization

The service supports both English and Arabic languages. Messages are configured in:

- `messages.properties` (English)
- `messages_ar.properties` (Arabic)
