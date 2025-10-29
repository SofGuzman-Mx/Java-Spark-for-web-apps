package mprower.javaspark.api;

import com.google.gson.Gson;
import mprower.javaspark.model.Item; // Importa el modelo
import mprower.javaspark.repository.ItemRepository; // Importa el repositorio
import mprower.javaspark.api.ErrorResponse;

import java.util.Optional;

import static spark.Spark.*; // Importa los métodos de Spark (get, post, etc)

/**
 * Controlador de API para gestionar los Items.
 * Define todos los endpoints de /api/items.
 */
public class ItemController {

    // Guarda las instancias que necesitamos
    private final ItemRepository repository;
    private final Gson gson;

    // El constructor prepara el controlador
    public ItemController() {
        this.repository = new ItemRepository();
        this.gson = new Gson();

        // Llama al método que inicializa las rutas
        initializeRoutes();
    }

    private void initializeRoutes() {

        /**
         * POST /api/items — Crear un nuevo item.
         */
        post("/api/items", (req, res) -> {
            res.type("application/json");
            try {
                Item newItem = gson.fromJson(req.body(), Item.class);
                Item createdItem = repository.createItem(newItem);
                res.status(201); // Created
                return gson.toJson(createdItem);
            } catch (IllegalArgumentException e) {
                res.status(400); // Bad Request (validación)
                return gson.toJson(new ErrorResponse("400", e.getMessage()));
            } catch (Exception e) {
                res.status(400); // Bad Request (JSON)
                return gson.toJson(new ErrorResponse("400", e.getMessage()));
            }
        });

        /**
         * GET /api/items — Obtener todos los items.
         */
        get("/api/items", (req, res) -> {
            res.type("application/json");
            return gson.toJson(repository.getAllItems());
        });

        /**
         * GET /api/items/:id — Obtener un item por ID.
         */
        get("/api/items/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            Optional<Item> item = repository.getItemById(id);

            if (item.isPresent()) {
                return gson.toJson(item.get());
            } else {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("404", "Item not found: " + id));
            }
        });

        /**
         * PUT /api/items/:id — Actualizar un item por ID.
         */
        put("/api/items/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            try {
                Item updatedItem = gson.fromJson(req.body(), Item.class);
                Optional<Item> result = repository.updateItem(id, updatedItem);

                if (result.isPresent()) {
                    return gson.toJson(result.get());
                } else {
                    res.status(404); // Not Found
                    return gson.toJson(new ErrorResponse("404","Item not found: " + id));
                }
            } catch (IllegalArgumentException e) {
                res.status(400); // Bad Request (validación)
                return gson.toJson(new ErrorResponse("400", e.getMessage()));
            } catch (Exception e) {
                res.status(400); // Bad Request (JSON)
                return gson.toJson(new ErrorResponse("400", e.getMessage()));
            }
        });

        /**
         * DELETE /api/items/:id — Eliminar un item por ID.
         */
        delete("/api/items/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            boolean deleted = repository.deleteItem(id);

            if (deleted) {
                return gson.toJson(new ErrorResponse("404","Item deleted: " + id));
            } else {
                res.status(404); // Not Found
                return gson.toJson(new ErrorResponse("404","Item not found: " + id));
            }
        });
    }
}