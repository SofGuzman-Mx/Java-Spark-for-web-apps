package mprower.javaspark.model;

import static spark.Spark.*;
import com.google.gson.Gson;
import mprower.javaspark.repository.UserRepository;
import mprower.javaspark.api.ItemController;

import java.util.Optional;

public class AppController {
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        port(port);

        Gson gson = new Gson();
        UserRepository repository = new UserRepository(); // Instancia del repositorio

        // --- Habilitar CORS ---
        enableCORS();

        // --- API REST de Usuarios (CRUD) ---

        /**
         * POST /api/users — Crear un nuevo usuario.
         * Body: JSON con username, email, role.
         */
        post("/api/users", (req, res) -> {
            res.type("application/json");
            try {
                User newUser = gson.fromJson(req.body(), User.class);
                User createdUser = repository.createUser(newUser); // Delegar al repo
                res.status(201); // 201 Created
                return gson.toJson(createdUser);
            } catch (IllegalArgumentException e) {
                res.status(400); // Bad Request (por validación fallida)
                return gson.toJson(new ErrorResponse(e.getMessage()));
            } catch (Exception e) {
                res.status(400); // Bad Request (por JSON mal formado)
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }
        });

        /**
         * GET /api/users — Obtener todos los usuarios.
         */
        get("/api/users", (req, res) -> {
            res.type("application/json");
            return gson.toJson(repository.getAllUsers());
        });

        /**
         * GET /api/users/:id — Obtener un usuario por ID.
         */
        get("/api/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            Optional<User> user = repository.getUserById(id);

            if (user.isPresent()) {
                return gson.toJson(user.get());
            } else {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("User not found with id: " + id));
            }
        });

        /**
         * PUT /api/users/:id — Actualizar un usuario por ID.
         * Body: JSON con username, email, role.
         */
        put("/api/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");

            try {
                User updatedUser = gson.fromJson(req.body(), User.class);
                Optional<User> result = repository.updateUser(id, updatedUser);

                if (result.isPresent()) {
                    return gson.toJson(result.get());
                } else {
                    res.status(404); // Not Found
                    return gson.toJson(new ErrorResponse("User not found with id: " + id));
                }
            } catch (IllegalArgumentException e) {
                res.status(400); // Bad Request (validación)
                return gson.toJson(new ErrorResponse(e.getMessage()));
            } catch (Exception e) {
                res.status(400); // Bad Request (JSON)
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }
        });

        /**
         * DELETE /api/users/:id — Eliminar un usuario por ID.
         */
        delete("/api/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            boolean deleted = repository.deleteUser(id);

            if (deleted) {
                return gson.toJson(new Status("User deleted: " + id));
            } else {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("User not found with id: " + id));
            }
        });
        new ItemController();
    }

    // --- Clases de Utilidad (anidadas) ---

    static class Status {
        String status;
        Status(String s) { this.status = s; }
    }

    static class ErrorResponse {
        String message;
        ErrorResponse(String m) { this.message = m; }
    }

    // --- Método de Configuración CORS (tu código original) ---
    private static void enableCORS() {
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

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            response.header("Access-Control-Allow-Credentials", "true");
        });
    }
}
