package mprower.javaspark.model;

import static spark.Spark.*;
import com.google.gson.Gson;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class User {

    static Map<String, UserModel> users = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        port(port);
        Gson gson = new Gson();

        // --- Habilitar CORS ---
        // Esto es crucial para que un frontend web pueda llamar a tu API
        // sin errores de "Cross-Origin Resource Sharing".
        enableCORS();

        // --- Rutas de ejemplo (de tu código base) ---
        get("/health", (req, res) -> {
            res.type("application/json");
            return gson.toJson(new Status("OK"));
        });

        get("/hello", (req, res) -> {
            res.type("text/plain");
            return "Hello World";
        });

        get("/api/ping", (req, res) -> {
            res.type("application/json");
            return gson.toJson(new Status("pong"));
        });

        // --- API REST de Usuarios ---

        /**
         * POST /users — Añadir un usuario.
         * Recibe un JSON en el body sin ID. El servidor genera el ID.
         */
        post("/users", (req, res) -> {
            res.type("application/json");
            try {
                UserModel newUser = gson.fromJson(req.body(), UserModel.class);
                if (newUser.name == null || newUser.email == null) {
                    res.status(400); // Bad Request
                    return gson.toJson(new ErrorResponse("Missing 'name' or 'email'"));
                }

                String id = UUID.randomUUID().toString();
                newUser.id = id;
                users.put(id, newUser);

                res.status(201); // 201 Created
                return gson.toJson(newUser);
            } catch (Exception e) {
                res.status(400); // Bad Request
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }
        });

        /**
         * GET /users — Obtener la lista de todos los usuarios.
         */
        get("/users", (req, res) -> {
            res.type("application/json");
            return gson.toJson(users.values());
        });

        /**
         * GET /users/:id — Obtener un usuario por el ID dado.
         */
        get("/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            UserModel user = users.get(id);

            if (user != null) {
                return gson.toJson(user);
            } else {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("User not found with id: " + id));
            }
        });

        /**
         * PUT /users/:id — Editar un usuario específico.
         * Reemplaza el usuario existente con el JSON enviado en el body.
         */
        put("/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");

            if (!users.containsKey(id)) {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("User not found with id: " + id));
            }

            try {
                UserModel updatedUser = gson.fromJson(req.body(), UserModel.class);
                updatedUser.id = id; // Asegurar que el ID es el de la URL
                users.put(id, updatedUser);
                return gson.toJson(updatedUser);
            } catch (Exception e) {
                res.status(400); // Bad Request
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }
        });

        /**
         * DELETE /users/:id — Borrar un usuario específico.
         */
        delete("/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            UserModel removedUser = users.remove(id);

            if (removedUser != null) {
                return gson.toJson(new Status("User deleted: " + id));
            } else {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("User not found with id: " + id));
            }
        });

        /**
         * OPTIONS /users/:id — Chequear si un usuario existe.
         *
         * NOTA: La forma estándar de "chequear" es hacer un GET /users/:id
         * y ver si la respuesta es 200 (existe) o 404 (no existe).
         * El método OPTIONS es para CORS, y ya está manejado por 'enableCORS()'.
         *
         * Si *realmente* quieres un endpoint OPTIONS para esto (no estándar):
         */
        options("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (users.containsKey(id)) {
                res.status(200); // OK
                return "User exists";
            } else {
                res.status(404); // Not Found
                return "User not found";
            }
        });
    }

    // --- Clases de Modelo y Utilidad ---

    /**
     * Modelo de datos para un Usuario (POJO)
     */
    static class UserModel {
        // Se usan campos públicos para simplificar el ejemplo con Gson
        public String id;
        public String name;
        public String email;

        // Constructor vacío necesario para Gson al deserializar
        public UserModel() {}
    }

    /**
     * Clase para respuestas de estado simples (la que ya tenías)
     */
    static class Status {
        String status;
        Status(String s) { this.status = s; }
    }

    /**
     * Clase simple para respuestas de error
     */
    static class ErrorResponse {
        String message;
        ErrorResponse(String m) { this.message = m; }
    }

    /**
     * Método helper para habilitar CORS en todas las rutas
     */
    private static void enableCORS() {
        // Maneja las peticiones OPTIONS (pre-flight)
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        // Se ejecuta antes de cada petición
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
            response.header("Access-Control-Request-Method", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            response.header("Access-Control-Allow-Credentials", "true");
        });
    }
}

