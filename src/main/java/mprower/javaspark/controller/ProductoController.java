package mprower.javaspark.controller;

import com.google.gson.Gson;
import mprower.javaspark.repository.ProductoRepository;
import mprower.javaspark.util.ErrorResponse;
import mprower.javaspark.model.Producto;
import java.util.Collection;

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
                return gson.toJson(new ErrorResponse("500", "Error retrieving products"));
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
                            return gson.toJson(new ErrorResponse("404", "Product not found"));
                        });
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new ErrorResponse("400", "Invalid product ID"));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error obtaining the product"));
            }
        });

        // Obtener todos los productos en oferta
        get("/api/ofertas", (req, res) -> {
            res.type("application/json");
            try {
                Collection<Producto> ofertas = repository.getProductosEnOferta();

                // Mantenemos el estilo de manejo de errores que ya tienes
                if (ofertas.isEmpty()) {
                    res.status(404);
                    return gson.toJson(new ErrorResponse("404", "No offers were found at this time"));
                }

                return gson.toJson(ofertas);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error retrieving offers"));
            }
        });

    }
}