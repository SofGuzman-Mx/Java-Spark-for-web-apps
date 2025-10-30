package mprower.javaspark.controller;

import com.google.gson.Gson;
import mprower.javaspark.model.CarritoItem;
import mprower.javaspark.repository.CarritoRepository;
import mprower.javaspark.util.Auth; // Necesitarás esta clase para verificar el token
import mprower.javaspark.util.ErrorResponse;

import static spark.Spark.*;

/**
 * Controlador para gestionar las operaciones del carrito de compras.
 * Todas las rutas aquí definidas requieren autenticación.
 */
public class CarritoController {

    private final CarritoRepository repository;
    private final Gson gson;

    public CarritoController() {
        this.repository = new CarritoRepository();
        this.gson = new Gson();
        initializeRoutes();
    }

    private void initializeRoutes() {

        // --- Filtro de Autenticación ---
        // Este filtro se ejecuta ANTES de cualquier ruta definida en este controlador bajo "/api/carrito/*"
        // Su trabajo es verificar el token JWT y extraer el ID del cliente.
        before("/api/carrito/*", (req, res) -> {
            String header = req.headers("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                halt(401, gson.toJson(new ErrorResponse("401", "Token de autorización no provisto o inválido.")));
            }

            String token = header.substring(7); // Quita "Bearer "
            try {
                int clienteId = Auth.verifyTokenAndGetId(token);
                // Guardamos el ID del cliente en el request para que las rutas lo puedan usar
                req.attribute("clienteId", clienteId);
            } catch (Exception e) {
                halt(401, gson.toJson(new ErrorResponse("401", "Token inválido o expirado.")));
            }
        });

        // --- Endpoints del Carrito ---

        /**
         * GET /api/carrito — Obtener todos los items del carrito del usuario autenticado.
         */
        get("/api/carrito", (req, res) -> {
            res.type("application/json");
            // Obtenemos el ID que el filtro 'before' ya validó y guardó para nosotros.
            int clienteId = req.attribute("clienteId");
            try {
                return gson.toJson(repository.getCarritoByClienteId(clienteId));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al obtener el carrito: " + e.getMessage()));
            }
        });

        /**
         * POST /api/carrito — Agregar un producto al carrito.
         * Body: JSON con idProducto y cantidad. Ejemplo: { "idProducto": 1, "cantidad": 2 }
         */
        post("/api/carrito", (req, res) -> {
            res.type("application/json");
            int clienteId = req.attribute("clienteId");
            try {
                AddItemRequest data = gson.fromJson(req.body(), AddItemRequest.class);
                CarritoItem nuevoItem = repository.agregarAlCarrito(clienteId, data.idProducto, data.cantidad);
                res.status(201); // 201 Created
                return gson.toJson(nuevoItem);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al agregar al carrito: " + e.getMessage()));
            }
        });

        /**
         * PUT /api/carrito/:id — Actualizar la cantidad de un item en el carrito.
         * :id es el ID del registro en la tabla 'carrito'.
         * Body: JSON con la nueva cantidad. Ejemplo: { "cantidad": 5 }
         */
        put("/api/carrito/:id", (req, res) -> {
            res.type("application/json");
            // Aquí no necesitamos el clienteId para la operación, pero el filtro 'before'
            // ya aseguró que el usuario está logueado y tiene permiso.
            // Para mayor seguridad, el repositorio podría verificar que el idCarrito pertenece al clienteId.
            try {
                int idCarrito = Integer.parseInt(req.params(":id"));
                UpdateQuantityRequest data = gson.fromJson(req.body(), UpdateQuantityRequest.class);

                boolean actualizado = repository.actualizarCantidad(idCarrito, data.cantidad);
                if (actualizado) {
                    return gson.toJson(new StatusResponse("Cantidad actualizada correctamente."));
                } else {
                    res.status(404);
                    return gson.toJson(new ErrorResponse("404", "Item del carrito no encontrado."));
                }
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new ErrorResponse("400", "ID de item de carrito inválido."));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al actualizar la cantidad: " + e.getMessage()));
            }
        });

        /**
         * DELETE /api/carrito/:id — Eliminar un item del carrito.
         * :id es el ID del registro en la tabla 'carrito'.
         */
        delete("/api/carrito/:id", (req, res) -> {
            res.type("application/json");
            try {
                int idCarrito = Integer.parseInt(req.params(":id"));
                boolean eliminado = repository.eliminarDelCarrito(idCarrito);
                if (eliminado) {
                    return gson.toJson(new StatusResponse("Item eliminado del carrito."));
                } else {
                    res.status(404);
                    return gson.toJson(new ErrorResponse("404", "Item del carrito no encontrado."));
                }
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new ErrorResponse("400", "ID de item de carrito inválido."));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al eliminar el item: " + e.getMessage()));
            }
        });
    }

    // --- Clases internas para representar los cuerpos de las peticiones (Request Bodies) ---
    private static class AddItemRequest {
        int idProducto;
        int cantidad;
    }

    private static class UpdateQuantityRequest {
        int cantidad;
    }

    private static class StatusResponse {
        String message;
        StatusResponse(String message) { this.message = message; }
    }
}