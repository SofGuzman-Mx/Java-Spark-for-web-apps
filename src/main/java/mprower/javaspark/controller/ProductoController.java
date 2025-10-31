package mprower.javaspark.controller;

import com.google.gson.Gson;
import mprower.javaspark.repository.ProductoRepository;
import mprower.javaspark.util.ErrorResponse;

import static spark.Spark.*;

public class ProductoController {

    private final ProductoRepository repository;
    private final Gson gson;

    public ProductoController() {
        this.repository = new ProductoRepository();
        this.gson = new Gson();
        initializeRoutes();
    }

    private void initializeRoutes() {
        // Obtener todos los productos (catálogo)
        get("/api/productos", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(repository.getAllProductos());
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al obtener productos"));
            }
        });

        // Obtener un producto por ID
        get("/api/productos/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                return repository.getProductoById(id)
                        .map(gson::toJson)
                        .orElseGet(() -> {
                            res.status(404);
                            return gson.toJson(new ErrorResponse("404", "Producto no encontrado"));
                        });
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new ErrorResponse("400", "ID de producto inválido"));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al obtener el producto"));
            }
        });
    }
}