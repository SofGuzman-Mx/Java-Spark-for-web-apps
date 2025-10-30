# E-commerce API con Java Spark

Este proyecto es una API RESTful para un sistema básico de e-commerce, desarrollada utilizando el micro-framework **Java Spark**. La API permite la gestión de clientes, la visualización de un catálogo de productos y la administración de un carrito de compras. La autenticación se maneja a través de JSON Web Tokens (JWT).

## Características Principales

-   **Gestión de Clientes:** Registro y login de usuarios.
-   **Seguridad:** Contraseñas hasheadas con jBCrypt y rutas protegidas mediante JWT.
-   **Catálogo de Productos:** Endpoints para listar y ver detalles de productos.
-   **Carrito de Compras:** Funcionalidad CRUD completa para el carrito de un usuario autenticado.
-   **Arquitectura en Capas:** Estructura organizada en Controladores, Repositorios y Modelos para una mejor mantenibilidad.
-   **Base de Datos:** Integración con una base de datos MySQL.
-   **Manejo de Excepciones:** Respuestas de error estandarizadas para `404 Not Found`, `500 Internal Server Error`, y `401 Unauthorized`.

## Tecnologías Utilizadas

-   **Lenguaje:** Java 11+
-   **Framework:** Spark Java 2.9.3
-   **Base de Datos:** MySQL 8.0
-   **Dependencias (Maven):**
    -   `spark-core`: Micro-framework web.
    -   `gson`: Para la serialización/deserialización de JSON.
    -   `mysql-connector-java`: Driver JDBC para MySQL.
    -   `jbcrypt`: Para el hasheo seguro de contraseñas.
    -   `java-jwt`: Para la creación y verificación de JSON Web Tokens.
    -   `slf4j-simple`: Para logging en la consola.

---

## Configuración del Entorno de Desarrollo

Sigue estos pasos para poner en marcha el proyecto en tu máquina local.

### Prerrequisitos

-   JDK 11 o superior instalado.
-   Apache Maven instalado.
-   Un servidor de base de datos MySQL en ejecución.
-   Un cliente de API como [Postman](https://www.postman.com/downloads/).

### 1. Configuración de la Base de Datos

Primero, necesitas crear la base de datos y las tablas.

```sql
-- Crear la base de datos
CREATE DATABASE ecommerce;

-- Usar la base de datos
USE ecommerce;

-- Crear las tablas necesarias (script completo)
CREATE TABLE cliente (
    id INT AUTO_INCREMENT NOT NULL,
    nombre VARCHAR(40),
    password VARCHAR(60), -- IMPORTANTE: Ajustado a 60 para BCrypt
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

-- (Aquí irían las tablas 'venta' y 'detalle_venta')
```

### 2. Poblar la Base de Datos con Productos

Ejecuta el siguiente script para añadir los productos iniciales al catálogo.

```sql
-- Inserts para la tabla 'descripcion'
INSERT INTO descripcion (id, descripcion) VALUES
(1, 'Una gorra autografiada por el famoso Peso Pluma.'),
(2, 'Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!'),
(3, 'Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista.'),
(4, 'Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo.'),
(5, 'Un jersey autografiado por el legendario rapero Snoop Dogg.'),
(6, 'Un crop-top usado y autografiado por la famosa rapera Cardi B. en su última visita a México'),
(7, 'Una guitarra eléctrica autografiada por la popular banda británica Coldplay, un día antes de su concierto en Monterrey en 2022.');

-- Inserts para la tabla 'producto'
INSERT INTO producto (nombre, prec, foto, cantidad, id_descr) VALUES
('Gorra autografiada por Peso Pluma', 621.34, 'gorra_pp.jpg', 10, 1),
('Casco autografiado por Rosalía', 734.57, 'casco_rosalia.jpg', 5, 2),
('Chamarra de Bad Bunny', 521.89, 'chamarra_bb.jpg', 8, 3),
('Guitarra de Fernando Delgadillo', 823.12, 'guitarra_fd.jpg', 3, 4),
('Jersey firmado por Snoop Dogg', 355.67, 'jersey_snoop.jpg', 15, 5),
('Prenda de Cardi B autografiada', 674.23, 'prenda_cardib.jpg', 7, 6),
('Guitarra autografiada por Coldplay', 458.91, 'guitarra_coldplay.jpg', 4, 7);
```

### 3. Configurar las Credenciales de la Base de Datos

Abre el archivo `src/main/java/mprower/javaspark/config/Database.java` y ajusta las credenciales de conexión a tu configuración local.

```java
package mprower.javaspark.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // Reemplaza con tus credenciales
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String USER = "root";
    private static final String PASSWORD = "TU_CONTRASEÑA_AQUI"; // <-- CAMBIA ESTO

    // ... resto del código ...
}
```

### 4. Compilar y Ejecutar el Proyecto

Abre una terminal en la raíz del proyecto y ejecuta los siguientes comandos Maven:

```bash
# Compila el proyecto y empaquétalo en un JAR
mvn clean package

# Ejecuta la aplicación
java -jar target/NOMBRE-DE-TU-ARCHIVO-JAR.jar
```

Si todo está configurado correctamente, verás un mensaje en la consola indicando que el servidor se ha iniciado en `http://localhost:8080`.

---

## Guía de la API

A continuación se detallan los endpoints disponibles.

### Autenticación

#### `POST /api/register`
Registra un nuevo cliente.

-   **Body (raw/json):**
    ```json
    {
        "nombre": "nuevoUsuario",
        "password": "passwordSegura123",
        "numero": "5551234567"
    }
    ```
-   **Respuesta Exitosa (201 Created):**
    ```json
    {
        "id": 1,
        "nombre": "nuevoUsuario",
        "password": null,
        "numero": "5551234567"
    }
    ```

#### `POST /api/login`
Inicia sesión y obtiene un token JWT.

-   **Body (raw/json):**
    ```json
    {
        "nombre": "nuevoUsuario",
        "password": "passwordSegura123"
    }
    ```
-   **Respuesta Exitosa (200 OK):**
    ```json
    {
        "token": "ey...[JWT_TOKEN_LARGO]...Jc"
    }
    ```

### Productos (Público)

#### `GET /api/productos`
Obtiene la lista de todos los productos del catálogo.

#### `GET /api/productos/:id`
Obtiene los detalles de un producto específico por su ID.

### Carrito de Compras (Requiere Autenticación)

**Importante:** Todas las peticiones a estos endpoints deben incluir la cabecera de autorización:
`Authorization: Bearer <TU_TOKEN_JWT>`

#### `POST /api/carrito`
Agrega un producto al carrito del usuario autenticado.

-   **Body (raw/json):**
    ```json
    {
        "idProducto": 1,
        "cantidad": 2
    }
    ```

#### `GET /api/carrito`
Obtiene todos los items del carrito del usuario autenticado.

#### `PUT /api/carrito/:id`
Actualiza la cantidad de un item específico en el carrito. `:id` es el ID del **registro en la tabla carrito**.

-   **Body (raw/json):**
    ```json
    {
        "cantidad": 5
    }
    ```

#### `DELETE /api/carrito/:id`
Elimina un item del carrito por su ID.