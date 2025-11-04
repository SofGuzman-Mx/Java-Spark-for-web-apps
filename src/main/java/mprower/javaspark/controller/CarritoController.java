package mprower.javaspark.controller;
import mprower.javaspark.model.Producto;
import com.google.gson.Gson;
import mprower.javaspark.model.CarritoItem;
import mprower.javaspark.repository.CarritoRepository;
import mprower.javaspark.util.Auth;
import mprower.javaspark.util.ErrorResponse;
import spark.Request;

import java.net.URLEncoder;

import static spark.Spark.*;

/**
 * Controlador para gestionar las operaciones del carrito de compras.
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
     * Método helper para autenticar una petición de API con JWT.
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

        // --- ENDPOINTS DE LA API (NO SE TOCAN) ---

        post("/api/carrito", (req, res) -> {
            res.type("application/json");
            try {
                int clienteId = autenticar(req);

                AddItemRequest data = gson.fromJson(req.body(), AddItemRequest.class);
                if (data == null) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("400", "Cuerpo de la petición (Body) está vacío o mal formado."));
                }

                CarritoItem nuevoItem = repository.agregarAlCarrito(clienteId, data.idProducto, data.cantidad);
                res.status(201);
                return gson.toJson(nuevoItem);
            } catch (Exception e) {
                if (e instanceof com.auth0.jwt.exceptions.JWTVerificationException) {
                    res.status(401);
                    return gson.toJson(new ErrorResponse("401", "Token inválido o expirado: " + e.getMessage()));
                }
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al agregar al carrito: " + e.getMessage()));
            }
        });

        get("/api/carrito", (req, res) -> {
            res.type("application/json");
            try {
                int clienteId = autenticar(req);
                return gson.toJson(repository.getCarritoByClienteId(clienteId));
            } catch (Exception e) {
                if (e instanceof com.auth0.jwt.exceptions.JWTVerificationException) {
                    res.status(401);
                    return gson.toJson(new ErrorResponse("401", "Token inválido o expirado: " + e.getMessage()));
                }
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al obtener el carrito: " + e.getMessage()));
            }
        });


        // --- ENDPOINT PARA EL FORMULARIO HTML (MODIFICADO SIN LOGIN) ---

        /**
         * POST /carrito/add — Agrega un producto al carrito desde un formulario HTML tradicional.
         * SIMULA que el usuario es siempre el cliente con ID = 1 para pruebas.
         */
        post("/carrito/add", (req, res) -> {
            // 1. Simular el ID del cliente. ¡Hemos quitado la verificación de sesión!
            Integer clienteId = 1; // <<-- ¡CAMBIO CLAVE! Siempre será el cliente 1.

            try {
                // 2. Obtener el ID del producto desde los datos del formulario
                int idProducto = Integer.parseInt(req.queryParams("id"));
                int cantidad = 1; // Siempre se agrega 1 unidad desde el catálogo

                // 3. Llamar al repositorio para guardar en la base de datos
                repository.agregarAlCarrito(clienteId, idProducto, cantidad);

                System.out.println("FORMULARIO HTML: Producto ID " + idProducto + " agregado al carrito del cliente ID " + clienteId);

                // 4. Redirigir de vuelta al catálogo con un mensaje de éxito
                res.redirect("/catalog?status=success");
                return null;

            } catch (Exception e) {
                System.err.println("FORMULARIO HTML: Error al agregar producto - " + e.getMessage());
                // 5. Si hay un error, redirigir con un mensaje de error
                String errorMessage = URLEncoder.encode(e.getMessage(), "UTF-8");
                res.redirect("/catalog?status=error&message=" + errorMessage);
                return null;
            }
        });


        // --- RESTO DE ENDPOINTS DE LA API (NO SE TOCAN) ---

        put("/api/carrito/:id", (req, res) -> {
            res.type("application/json");
            try {
                autenticar(req);
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
                return gson.toJson(new ErrorResponse("500", "Error al actualizar la cantidad: " + e.getMessage()));
            }
        });

        delete("/api/carrito/:id", (req, res) -> {
            res.type("application/json");
            try {
                autenticar(req);
                int idCarrito = Integer.parseInt(req.params(":id"));
                boolean eliminado = repository.eliminarDelCarrito(idCarrito);
                if (eliminado) {
                    return gson.toJson(new StatusResponse("Item eliminado del carrito."));
                } else {
                    res.status(404);
                    return gson.toJson(new ErrorResponse("404", "Item del carrito no encontrado."));
                }
            } catch (Exception e) {
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