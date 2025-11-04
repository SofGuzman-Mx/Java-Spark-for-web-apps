# E-commerce API with Java Spark

This project is a full-stack e-commerce platform built with Java Spark, designed to support product catalog browsing, user authentication, and shopping cart functionality. It integrates backend logic with dynamic frontend rendering using Mustache templates and Bootstrap 5. The system is scalable, secure, and adaptable to future business needs.

## Key Features

-   *Customer Management:* User registration and login.
-   *Security:* Passwords hashed with jBCrypt and protected routes using JWT authentication.
-   *Product Catalog:* Endpoints to list and view product details.
-   *Shopping Cart:* Full CRUD functionality for an authenticated user's shopping cart.
-   *Layered Architecture:* Organized structure into Controllers, Repositories, and Models for better maintainability.
-   *Database Integration:* Connects to a MySQL database.
-   *Exception Handling:* Standardized error responses for 404 Not Found, 500 Internal Server Error, and 401 Unauthorized.

---
🧠 Architecture Overview
The application follows a clean MVC architecture:

Model: Represents domain entities (Producto, Cliente, CarritoItem) with encapsulated logic.

View: Uses Mustache templates for dynamic HTML rendering (catalog.mustache, carrito.mustache, etc.).

Controller: Handles routing and business logic (ProductoController, CarritoController, ClienteController).

Diagram available in /docs/architecture.png (optional visual reference)

**Innovation Strategy**
This project integrates innovative features with technical, economic, and social depth:
- Offer System: Products can be flagged as discounted, with dynamic price rendering.
- Session-Based Cart: Allows anonymous users to interact with the cart before login.
- Multilingual Support (planned): Templates structured for easy localization.
- Gamification Potential: Architecture allows future integration of badges, points, or dynamic pricing.

🔐 Security & Authentication
- JWT Authentication: Secures all /api/carrito/* routes.
- Password Hashing: Uses jBCrypt to store passwords securely.
- Token Validation: Implemented via Auth.verifyTokenAndGetId() with robust error handling.

## Sprint 2 Deliverables

This section documents the requirements met during Sprint 2, focusing on the backend implementation.

### 1. Exception Handling Module

A robust exception handling system has been implemented to ensure the API responds predictably to different error scenarios.

-   *ErrorResponse Class:* A standardized DTO (util/ErrorResponse.java) was created to format all error messages into a consistent JSON structure ({ "errorCode": "...", "message": "..." }).
-   *Supported Cases:*
    -   *404 Not Found:* Triggered when a client requests a specific resource by an ID that does not exist in the database. The controllers handle empty Optional results from the repository to return a clear 404 response.
    -   *500 Internal Server Error:* A global try-catch (Exception e) block in each endpoint acts as a safety net. It captures any unexpected server-side errors (e.g., database connection failures, SQL syntax errors), preventing the application from crashing and returning a generic but safe 500 error message.
    -   *401 Unauthorized:* Implemented for protected routes (/api/carrito/*). An authentication method validates the JWT on each request. If the token is missing, invalid, or expired, the request is halted, and a 401 error is returned.

### 2. Source Code Implementation

The backend source code for the main business logic was developed, including:

-   *Authentication Logic (ClienteController, Auth.java):* Implemented user registration and login endpoints. The system generates a JWT upon successful login.
-   *Product Catalog API (ProductoController, ProductoRepository):* Created endpoints to fetch all products and a single product by its ID from the database.
-   *Shopping Cart API (CarritoController, CarritoRepository):* Developed the full CRUD functionality for the shopping cart, with each endpoint protected by JWT authentication to ensure data privacy.

### 3. Integration and Logic Error Resolution

During development and testing with Postman, several integration and logic errors were identified and resolved:

-   *Database Schema Mismatch:* Corrected the password column length in the cliente table from VARCHAR(50) to VARCHAR(60) to accommodate the length of a BCrypt hash.
-   *Authentication Flow (NullPointerException):* Debugged and resolved a NullPointerException in the CarritoController caused by an issue in the JWT validation filter. The authentication logic was refactored to ensure the user ID was correctly retrieved from the token and passed to the business logic.
-   *API Contract Mismatch (Foreign Key Constraint Fails):* Fixed a 500 Server Error caused by a foreign key violation when adding items to the cart. The error was traced to a mismatch between the JSON key expected by the API (idProducto) and the key being sent (id). The documentation and Postman examples were updated to reflect the correct API contract.

---

## Tech Stack

-   *Language:* Java 17
-   *Framework:* Spark Java 2.9.3
-   *Database:* MySQL 8.0
-   *Dependencies (Maven):*
    -   spark-core: Web micro-framework.
    -   gson: For JSON serialization/deserialization.
    -   mysql-connector-java: JDBC driver for MySQL.
    -   jbcrypt: For secure password hashing.
    -   java-jwt: For creating and verifying JSON Web Tokens.
    -   slf4j-simple: For console logging.

---

## Development Environment Setup

Follow these steps to get the project running on your local machine.

### Prerequisites

-   JDK 17 or higher installed.
-   Apache Maven installed.
-   A running MySQL database server.
-   An API client like [Postman](https://www.postman.com/downloads/).

### 1. Database Configuration

First, you need to create the database and tables.

sql
-- Create the database
CREATE DATABASE ecommerce;

-- Use the database
USE ecommerce;

-- Create the required tables (full script)
CREATE TABLE cliente (
id INT AUTO_INCREMENT NOT NULL,
nombre VARCHAR(40),
password VARCHAR(60), -- IMPORTANT: Sized to 60 for BCrypt
numero VARCHAR(20),
CONSTRAINT PRIMARY KEY(id)
);

CREATE TABLE descripcion(
id INT AUTO_INCREMENT,
descripcion VARCHAR(200),
CONSTRAINT PRIMARY KEY(id)
);

CREATE TABLE producto(
id INT AUTO_INCREMENT NOT NULL,
nombre VARCHAR(60),
prec FLOAT(10,2),
foto VARCHAR(100),
cantidad INT,
id_descr INT,
CONSTRAINT FOREIGN KEY(id_descr) REFERENCES descripcion(id),
CONSTRAINT PRIMARY KEY(id)
);

CREATE TABLE carrito (
id INT AUTO_INCREMENT,
id_cli INT,
id_pro INT,
cantidad INT,
CONSTRAINT PRIMARY KEY(id),
CONSTRAINT FOREIGN KEY(id_cli) REFERENCES cliente(id),
CONSTRAINT FOREIGN KEY(id_pro) REFERENCES producto(id)
);

-- (The 'venta' and 'detalle_venta' tables would go here)


### 2. Populate the Database with Products

Run the following script to add initial products to the catalog.

sql
-- Inserts for 'descripcion' table (product descriptions)
INSERT INTO descripcion (id, descripcion) VALUES
(1, 'A cap autographed by the famous Peso Pluma.'),
(2, 'A helmet autographed by the famous singer Rosalía, a true MOTOMAMI!'),
(3, 'A jacket of Bad Bunny''s favorite brand, autographed by the artist himself.'),
(4, 'A high-quality acoustic guitar used by the famous singer-songwriter Fernando Delgadillo.'),
(5, 'A jersey signed by the legendary rapper Snoop Dogg.'),
(6, 'A crop-top worn and autographed by the famous rapper Cardi B. on her last visit to Mexico'),
(7, 'An electric guitar autographed by the popular British band Coldplay, a day before their concert in Monterrey in 2022.');

-- Inserts for 'producto' table
INSERT INTO producto (nombre, prec, foto, cantidad, id_descr) VALUES
('Gorra autografiada por Peso Pluma', 621.34, 'gorra_pp.jpg', 10, 1),
('Casco autografiado por Rosalía', 734.57, 'casco_rosalia.jpg', 5, 2),
('Chamarra de Bad Bunny', 521.89, 'chamarra_bb.jpg', 8, 3),
('Guitarra de Fernando Delgadillo', 823.12, 'guitarra_fd.jpg', 3, 4),
('Jersey firmado por Snoop Dogg', 355.67, 'jersey_snoop.jpg', 15, 5),
('Prenda de Cardi B autografiada', 674.23, 'prenda_cardib.jpg', 7, 6),
('Guitarra autografiada por Coldplay', 458.91, 'guitarra_coldplay.jpg', 4, 7);


### 3. Configure Database Credentials

Open the file src/main/java/mprower/javaspark/config/Database.java and adjust the connection credentials to match your local setup.

java
package mprower.javaspark.config;

// ... imports

public class Database {
// Replace with your credentials
private static final String URL = "jdbc:mysql://localhost:3306/ecommerce";
private static final String USER = "root";
private static final String PASSWORD = "YOUR_PASSWORD_HERE"; // <-- CHANGE THIS

    // ... rest of the code ...
}


### 4. Compile and Run the Project

Open a terminal at the project root and run the following Maven commands:

bash
# Clean the project and build the JAR file
mvn clean package

# Run the application
java -jar target/YOUR-JAR-FILE-NAME.jar


Alternatively, you can run the main method in App.java directly from your IDE. If everything is set up correctly, you will see a message in the console indicating that the server has started on http://localhost:8080.

---

## API Guide

The following endpoints are available.

### Authentication

#### POST /api/register
Registers a new customer.

-   *Body (raw/json):*
    json
    {
    "nombre": "newUser",
    "password": "securePassword123",
    "numero": "5551234567"
    }

-   *Success Response (201 Created):*
    json
    {
    "id": 1,
    "nombre": "newUser",
    "password": null,
    "numero": "5551234567"
    }


#### POST /api/login
Logs in a user and returns a JWT.

-   *Body (raw/json):*
    json
    {
    "nombre": "newUser",
    "password": "securePassword123"
    }

-   *Success Response (200 OK):*
    json
    {
    "token": "ey...[LONG_JWT_TOKEN]...Jc"
    }


### Products (Public)

#### GET /api/productos
Gets the list of all products in the catalog.

#### GET /api/productos/:id
Gets the details of a specific product by its ID.

### Shopping Cart (Authentication Required)

*Important:* All requests to these endpoints must include the Authorization header:
Authorization: Bearer <YOUR_JWT_TOKEN>

#### POST /api/carrito
Adds a product to the authenticated user's cart.

-   *Body (raw/json):*
    json
    {
    "idProducto": 1,
    "cantidad": 2
    }

-   *Success Response (201 Created):* Returns the newly created cart item object.

#### GET /api/carrito
Gets all items in the authenticated user's cart.

#### PUT /api/carrito/:id
Updates the quantity of a specific item in the cart. :id is the ID of the *record in the carrito table*.

-   *Body (raw/json):*
    json
    {
    "cantidad": 5
    }


#### DELETE /api/carrito/:id
Deletes an item from the cart by its ID.

## Sprint 3.
✅ Dynamic Catalog & Offers<br>
- Products loaded from MySQL and rendered via Mustache.
- Offer logic implemented with SQL joins and conditional rendering.
- Search bar filters products by name using query parameters.

✅ Add-to-Cart via HTML Forms<br>
- Each product includes a form with hidden id field.
- POST /carrito/add route stores item in session.
- Cart view displays item details, quantity, and total.

✅ UI/UX Enhancements<br>
- Bootstrap 5 styling for cards, buttons, and modals.
- Offer highlighting with strikethrough and red price.
- Modal popups for product details.
---
📦 Tech Stack
------
| Layer       | Technology           |
|-------------|----------------------|
| Language    | Java 17              |
| Framework   | Spark Java 2.9.3     |
| Database    | MySQL 8.0            |
| Templating  | Mustache             |
| Styling     | Bootstrap 5          |
| Auth        | java-jwt, jBCrypt    |
| Logging     | slf4j-simple         |
---
## 🧪 Testing & Validation<br>
- Manual testing via Postman and browser.
- Edge cases handled: invalid product ID, empty cart, expired token.
- Console logs and error messages used for debugging.

📈 Impact Analysis
---
| Metric                     | Value                |
|---------------------------|----------------------|
| Products in catalog       | 7                    |
| Offer-enabled products    | 3                    |
| Average response time     | < 150ms              |
| Authentication success rate | 100% (manual test) |

## 🔄 Continuous Improvement
✅Iterated on cart logic to support session-based storage.

✅ Refactored SQL queries for performance.

 🔜 Planned features: favorites, reviews, real-time updates via WebSocket.

## 🧠 Strategic Justification
- Spark Java chosen for lightweight routing and fast prototyping.
- Mustache selected for simplicity and logic-less templating.
- Session-based cart allows smoother UX before login.
- Modular architecture enables easy feature expansion.

## 💬 Retrospective & Soft Skills
- “Debugging the JWT flow taught me to trace logic across layers and handle edge cases gracefully.”
- Overcame NullPointerException in token parsing.
- Coordinated with peers to resolve foreign key constraint errors.
- Documented all fixes and decisions in Git commits and README.

## 🧭 Deployment & Backup Strategy
- GitHub repository with versioned commits and clear structure.
- Manual backup via GitHub + optional GitHub Actions (planned).
- README includes full setup instructions for reproducibility.

## 🧠 Future Scalability
Architecture supports:
- Real-time updates via WebSocket
- Category-based filtering
- Mobile responsiveness
- Multilingual templates