package mprower.javaspark.controller;
import mprower.javaspark.model.Producto;
import com.google.gson.Gson;
import mprower.javaspark.model.CarritoItem;
import mprower.javaspark.model.Venta;                 // <-- NUEVO IMPORT
import mprower.javaspark.repository.CarritoRepository;
import mprower.javaspark.repository.VentaRepository;   // <-- NUEVO IMPORT
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
    private final VentaRepository ventaRepository; // <-- NUEVA INSTANCIA
    private final Gson gson;

    public CarritoController() {
        this.repository = new CarritoRepository();
        this.ventaRepository = new VentaRepository(); // <-- INICIALIZACIÓN
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

        // --- ENDPOINTS DE LA API ---

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

        /**
         * GET /api/carrito — Obtener todos los items del carrito.
         * MODIFICADO: Se quita la autenticación y se simula el cliente 1.
         */
        get("/api/carrito", (req, res) -> {
            res.type("application/json");
            try {
                int clienteId = 1; // <<-- ¡CAMBIO REALIZADO!
                return gson.toJson(repository.getCarritoByClienteId(clienteId));
            } catch (Exception e) {
                // El error de JWT ya no puede ocurrir aquí, pero dejamos el catch general
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al obtener el carrito: " + e.getMessage()));
            }
        });


        // --- ENDPOINTS PARA FORMULARIOS HTML ---

        post("/carrito/add", (req, res) -> {
            Integer clienteId = 1; // Simula el cliente 1.

            try {
                int idProducto = Integer.parseInt(req.queryParams("id"));
                int cantidad = 1;
                repository.agregarAlCarrito(clienteId, idProducto, cantidad);
                System.out.println("FORMULARIO HTML: Producto ID " + idProducto + " agregado al carrito del cliente ID " + clienteId);
                res.redirect("/catalog?status=success");
                return null;
            } catch (Exception e) {
                System.err.println("FORMULARIO HTML: Error al agregar producto - " + e.getMessage());
                String errorMessage = URLEncoder.encode(e.getMessage(), "UTF-8");
                res.redirect("/catalog?status=error&message=" + errorMessage);
                return null;
            }
        });

        /**
         * POST /checkout — Procesa la compra desde el formulario del carrito.
         * Crea un registro en 'venta' y 'detalle_venta', y vacía el carrito.
         */
        post("/checkout", (req, res) -> {
            Integer clienteId = 1; // Simula siempre el cliente 1

            try {
                // Llamamos al nuevo método en VentaRepository
                Venta nuevaVenta = ventaRepository.crearVentaDesdeCarrito(clienteId);

                // Redirigir a una página de éxito (que crearemos más tarde)
                res.redirect("/compra-exitosa?folio=" + nuevaVenta.getFolio());
                return null;
            } catch (Exception e) {
                System.err.println("Error durante el checkout: " + e.getMessage());
                String errorMessage = URLEncoder.encode(e.getMessage(), "UTF-8");
                res.redirect("/carrito?error=" + errorMessage); // Redirige de vuelta al carrito con un error
                return null;
            }
        });


        // --- RESTO DE ENDPOINTS DE LA API ---

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