# Orange E-Commerce Microservices System

## 📌 Project Overview
The **Orange E-Commerce Microservices System** is a modern, modular e-commerce platform built using **Spring Boot**, **Java**, and a **microservices architecture**.  
The system provides a scalable and efficient online shopping experience by separating core functionalities into independent services.

It supports multiple services such as **user management, product catalog, cart, order processing, and notifications**.  
Each service is independent, communicates via **REST APIs**, and leverages **JWT authentication** and **RabbitMQ** for asynchronous messaging.

---

## 🏗 Architecture & Services

### 1. **Gateway Service**
- Acts as the entry point for all client requests. 🌐
- Handles **JWT authentication** and request routing to microservices.

### 2. **User Service**
- Manages user accounts, authentication, and roles. 👤
- Handles user registration, login, and profile management.
- Provides **JWT tokens** for secure access.
- Supports organization enrollment and status approval.

### 3. **Product Service**
- Manages product catalog including creation, updates, and deletion. 🛍️
- Provides search and filter functionality for products.
- Exposes APIs for retrieving product information to other services.

### 4. **Cart Service**
- Manages users’ shopping carts. 🛒
- Allows adding, removing, and updating items in the cart.
- Communicates with **product-service** to verify product details.
- Prepares cart data for checkout.

### 5. **Order Service**
- Handles order creation and management. 📦
- Processes orders from carts and stores order history.
- Updates order status and communicates with **notification-service** for updates.
- Integrates **RabbitMQ events** to asynchronously process cart checkouts.

### 6. **Notification Service**
- Sends **email notifications** to users (e.g., welcome email, order confirmation). 📧
- Listens to RabbitMQ events from other services (User and order).

---

## ✨ Key Features
- **Microservices Architecture:** Independent, modular services with clear responsibilities. 🏗️
- **JWT Authentication:** Secure communication between frontend and backend. 🔒
- **RabbitMQ Integration:** Event-driven messaging for decoupled services. ⚡
- **RESTful APIs:** Clean and scalable API endpoints for each service. 📡
- **Scalability:** Each service can be scaled independently based on load. 📈
- **Organization Enrollment:** Users can enroll via registration codes; admins approve access. ✅
- **Admin Dashboards:** Super admin and organization admin can manage users and content. 🛠️

---

## 💻 Tech Stack
- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Messaging:** RabbitMQ
- **Database:** MySQL
- **API Gateway:** Spring Cloud Gateway
- **Authentication:** JWT (JSON Web Tokens)
- **Build Tool:** Maven
- **Version Control:** Git
- **Containerization:** Docker 🐳

---

## 🚀 Getting Started

### Prerequisites
- Java 23
- Maven
- MySQL
- RabbitMQ

