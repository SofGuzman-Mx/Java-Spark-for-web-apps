package mprower.javaspark.controller;

import com.google.gson.Gson;
import mprower.javaspark.model.CarritoItem;
import mprower.javaspark.repository.CarritoRepository;
import mprower.javaspark.util.Auth;
import mprower.javaspark.util.ErrorResponse;
import spark.Request; // Importamos la clase Request

import static spark.Spark.*;

/**
 * Controlador para gestionar las operaciones del carrito de compras.
 * Cada ruta maneja su propia autenticación para depuración.
 */
public class CarritoController {

    private final CarritoRepository repository;
    private final Gson gson;

    public CarritoController() {
        this.repository = new CarritoRepository();
        this.gson = new Gson();
        initializeRoutes();
    }

    /**
     * Método helper para autenticar una petición.
     * Extrae el token, lo valida y devuelve el ID del cliente.
     * @param req La petición de Spark.
     * @return El ID del cliente si la autenticación es exitosa.
     * @throws Exception Si la autenticación falla por cualquier motivo.
     */
    private int autenticar(Request req) throws Exception {
        String header = req.headers("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new Exception("Token de autorización no provisto o inválido.");
        }
        String token = header.substring(7);
        return Auth.verifyTokenAndGetId(token);
    }


    private void initializeRoutes() {

        // --- Endpoints del Carrito ---

        /**
         * GET /api/carrito — Obtener todos los items del carrito del usuario autenticado.
         */
        get("/api/carrito", (req, res) -> {
            res.type("application/json");
            try {
                int clienteId = autenticar(req); // Autenticación dentro del endpoint
                return gson.toJson(repository.getCarritoByClienteId(clienteId));
            } catch (Exception e) {
                // Si la autenticación falla, devolvemos 401. Si es otro error, devolvemos 500.
                if (e instanceof com.auth0.jwt.exceptions.JWTVerificationException) {
                    res.status(401);
                    return gson.toJson(new ErrorResponse("401", "Token inválido o expirado: " + e.getMessage()));
                }
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al obtener el carrito: " + e.getMessage()));
            }
        });

        // Endpoint para agregar al carrito desde formulario HTML (sin autenticación por ahora)
        post("/carrito/add", (req, res) -> {
            try {
                int idProducto = Integer.parseInt(req.queryParams("id"));
                int cantidad = 1; // Por defecto 1 unidad

                // Aquí puedes usar una sesión o lógica temporal para simular un carrito
                System.out.println("Producto agregado al carrito desde HTML: ID = " + idProducto);

                // Redirigir de vuelta al catálogo o mostrar mensaje
                res.redirect("/catalog");
                return null;
            } catch (Exception e) {
                res.status(400);
                return "Error al agregar producto al carrito: " + e.getMessage();
            }
        });

        /**
         * PUT /api/carrito/:id — Actualizar la cantidad de un item en el carrito.
         */
        put("/api/carrito/:id", (req, res) -> {
            res.type("application/json");
            try {
                autenticar(req); // Solo para verificar que el usuario está logueado

                int idCarrito = Integer.parseInt(req.params(":id"));
                UpdateQuantityRequest data = gson.fromJson(req.body(), UpdateQuantityRequest.class);

                boolean actualizado = repository.actualizarCantidad(idCarrito, data.cantidad);
                if (actualizado) {
                    return gson.toJson(new StatusResponse("Cantidad actualizada correctamente."));
                } else {
                    res.status(404);
                    return gson.toJson(new ErrorResponse("404", "Item del carrito no encontrado."));
                }
            } catch (Exception e) {
                if (e instanceof com.auth0.jwt.exceptions.JWTVerificationException) {
                    res.status(401);
                    return gson.toJson(new ErrorResponse("401", "Token inválido o expirado: " + e.getMessage()));
                }
                if (e instanceof NumberFormatException) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("400", "ID de item de carrito inválido."));
                }
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al actualizar la cantidad: " + e.getMessage()));
            }
        });

        /**
         * DELETE /api/carrito/:id — Eliminar un item del carrito.
         */
        delete("/api/carrito/:id", (req, res) -> {
            res.type("application/json");
            try {
                autenticar(req); // Solo para verificar que el usuario está logueado

                int idCarrito = Integer.parseInt(req.params(":id"));
                boolean eliminado = repository.eliminarDelCarrito(idCarrito);
                if (eliminado) {
                    return gson.toJson(new StatusResponse("Item eliminado del carrito."));
                } else {
                    res.status(404);
                    return gson.toJson(new ErrorResponse("404", "Item del carrito no encontrado."));
                }
            } catch (Exception e) {
                if (e instanceof com.auth0.jwt.exceptions.JWTVerificationException) {
                    res.status(401);
                    return gson.toJson(new ErrorResponse("401", "Token inválido o expirado: " + e.getMessage()));
                }
                if (e instanceof NumberFormatException) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("400", "ID de item de carrito inválido."));
                }
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