# Order Service Help

This is the Order Service for the e-commerce microservices system.

## Quick Start

1. **Prerequisites**

   - Java 21
   - Maven 3.6+
   - MySQL 8.0+
   - Redis (optional)
   - RabbitMQ (optional)

2. **Database Setup**

   - Create database: `order_service_dev`
   - The service will automatically create tables using Liquibase

3. **Run the Service**

   ```bash
   mvn spring-boot:run
   ```

4. **Access the API**
   - Swagger UI: http://localhost:8085/swagger-ui.html
   - Health Check: http://localhost:8085/actuator/health

## Configuration

### Environment Variables

- `JWT_SECRET`: JWT secret key
- `CART_SERVICE_URL`: Cart service URL
- `PRODUCT_SERVICE_URL`: Product service URL
- `USER_SERVICE_URL`: User service URL

### Profiles

- `dev`: Development configuration
- `prod`: Production configuration
- `test`: Test configuration

## API Endpoints

### Test Endpoints

- `GET /api/test/message` - Get test message
- `GET /api/test/messages` - Get all messages
- `GET /api/test/messages/ar` - Get Arabic messages

### Health Endpoints

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Service info

## Troubleshooting

### Common Issues

1. **Database Connection Error**

   - Check MySQL is running
   - Verify database credentials
   - Ensure database exists

2. **Port Already in Use**

   - Change port in `application.properties`
   - Kill process using port 8085

3. **JWT Token Issues**
   - Check JWT_SECRET is set
   - Verify token format

## Support

For issues and questions, please contact the development team.
