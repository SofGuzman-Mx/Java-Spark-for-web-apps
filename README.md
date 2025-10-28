# Spark Java Online Store – Sprint 1

## Project
This project consists of developing an online store for collectibles using Java and the Spark framework. Sprint 1 focuses on the initial configuration of the project, the implementation of RESTful API services, and basic route and error handling.

---

## Sprint 1 Objective
Implement an API service for the online store, defining HTTP routes, configuring Maven, and managing basic operations (GET, POST, PUT, DELETE) for users and items.

---

## User Stories – Sprint 1

### 1. Project Configuration
**As** a system administrator  
**I want** to configure Maven and Spark  
**So that** the application compiles, runs, and exposes HTTP endpoints for development and testing.

 Requirements:
- `pom.xml` file with dependencies: Spark, SLF4J, Gson, JUnit.
- Standard Maven project structure.
- Entry point with configurable port.
- `/health` endpoint that returns status 200 OK in JSON.

---

### 2. User API
**As** an API client  
**I want** CRUD endpoints for users  
**So that** I can create, read, update, and delete users from the frontend or integrations.

 Requirements:
- Routes:
    - `POST /api/users`
    - `GET /api/users`
    - `GET /api/users/:id`
    - `PUT /api/users/:id`
    - `DELETE /api/users/:id`
- Validations: username, email, role.
- Responses in JSON format.
- Appropriate HTTP codes (201, 200, 400, 404, etc.).

---

- Model: title, description, category, price, stock, status.
- Optional filters in `GET /api/items`.
- Consistent validations and JSON responses.

---

### 4. Global Error Handling
**As** a developer  
**I want** to handle errors centrally  
**So that** customers receive consistent and clear JSON responses in the event of failures.

 Requirements:
- Global exception handler with Spark.
- `ErrorResponse` class to structure errors.
- Codes: 400 (validation), 404 (not found), 500 (internal error).
- Error logging in console.
---
## Project Structure
```
src/ 
├── main/ 
│ ├── java/ 
│ │ └── mprover/ 
│ │ └── javaspark/ 
│ │ ├── HelloWorld.java 
│ │ ├── api/ 
│ │ │ └── ErrorResponse.java 
│ │ ├── model/ 
│ │ │  ├── User.java 
│ │  │  ├── Item.java 
│ │  └── repository/ 
│ │    ├── UserRepository.java 
│ │    └── ItemRepository.java 
│ └── resources/ 
│    └── application.properties 
└── test/ 
└── java/
└── mprover/
└── javaspark/
└── HelloWorldTest.java
```
---
## ✅ Sprint 1 Delivery Checklist
- [✅] Maven project with dependencies configured.
- [✅] Functional `/hello` and `/health` endpoints.
- [✅] REST API for users and items.
- [✅] Basic validations and JSON responses.
- [✅] Global error handling.
- [✅] Basic unit tests with JUnit.

---

## 📚 Next Sprint
In Sprint 2, views with Mustache, forms for item offers, and advanced exception handling in the interface will be implemented.

---

