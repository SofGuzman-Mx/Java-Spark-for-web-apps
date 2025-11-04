package mprower.javaspark;

import mprower.javaspark.config.Database;
import mprower.javaspark.controller.ClienteController;
import mprower.javaspark.controller.ProductoController;
import mprower.javaspark.controller.CarritoController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.*;
import static spark.Spark.*;

import static spark.Spark.*;

import mprower.javaspark.model.CarritoItem;
import mprower.javaspark.model.Producto;
import mprower.javaspark.repository.CarritoRepository;
import mprower.javaspark.repository.ProductoRepository;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;


public class App {
    public static void main(String[] args) {
        port(8080);

        staticFiles.location("/public");

        // Habilitar CORS para la API
        // Habilitar CORS
        enableCORS();

        // Inicializar Controladores de la API
        new ClienteController();
        new ProductoController();
        new CarritoController();

        // --- Definir Rutas de Vistas HTML ---
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

        // Ruta para la página de login
        get("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Iniciar sesión - Coleccionables");
            return new ModelAndView(model, "login.mustache");
        }, templateEngine);

        // --- NUEVA RUTA PARA LA PÁGINA DE REGISTRO ---
        get("/signup", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Registro de Nuevo Usuario");
            return new ModelAndView(model, "signup.mustache");
        }, templateEngine);

        // --- NUEVA RUTA PARA LA PÁGINA DE CATÁLOGO ---
        get("/catalog", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Map<String, Object>> items = new ArrayList<>();

            String query = req.queryParams("q");

            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id_pro FROM oferta")) {

                Set<Integer> idsEnOferta = new HashSet<>();
                while (rs.next()) {
                    idsEnOferta.add(rs.getInt("id_pro"));
                }

                Collection<Producto> productos = new ProductoRepository().getAllProductos();

                for (Producto p : productos) {
                    if (query == null || p.getNombre().toLowerCase().contains(query.toLowerCase())) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", p.getId());
                        item.put("nombre", p.getNombre());
                        item.put("descripcion", p.getDescripcion());
                        item.put("foto", p.getFoto());

                        if (idsEnOferta.contains(p.getId())) {
                            item.put("precioOriginal", String.format("%.2f", p.getPrecio() * 1.25)); // Precio sin descuento
                            item.put("prec", String.format("%.2f", p.getPrecio())); // Precio con descuento
                            item.put("oferta", true);
                        } else {
                            item.put("prec", String.format("%.2f", p.getPrecio()));
                            item.put("oferta", false);
                        }

                        items.add(item);
                    }
                }

                model.put("items", items);
                return new ModelAndView(model, "catalog.mustache");

            } catch (Exception e) {
                model.put("items", new ArrayList<>());
                model.put("error", "No se pudo cargar el catálogo: " + e.getMessage());
                return new ModelAndView(model, "catalog.mustache");
            }
        }, new MustacheTemplateEngine());

        get("/offers", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Map<String, Object>> items = new ArrayList<>();

            try {
                // Obtener IDs de productos en oferta
                Set<Integer> idsEnOferta = new HashSet<>();
                try (Connection conn = Database.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id_pro FROM oferta")) {
                    while (rs.next()) {
                        idsEnOferta.add(rs.getInt("id_pro"));
                    }
                }
                // Obtener todos los productos
                Collection<Producto> productos = new ProductoRepository().getAllProductos();

                for (Producto p : productos) {
                    if (idsEnOferta.contains(p.getId())) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", p.getId());
                        item.put("nombre", p.getNombre());
                        item.put("descripcion", p.getDescripcion());
                        item.put("precioOriginal", String.format("%.2f", p.getPrecio() * 1.25)); // 25% más
                        item.put("prec", String.format("%.2f", p.getPrecio())); // Precio con descuento
                        item.put("foto", p.getFoto());
                        item.put("oferta", true); // ✅ Indicador visual
                        items.add(item);
                    }
                }

                model.put("items", items);
                return new ModelAndView(model, "offers.mustache");
            } catch (Exception e) {
                model.put("items", new ArrayList<>());
                model.put("error", "No se pudo cargar las ofertas: " + e.getMessage());
                return new ModelAndView(model, "offers.mustache");
            }
        }, new MustacheTemplateEngine());

        System.out.println("Servidor iniciado en http://localhost:8080");

        get("/carrito", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Map<String, Object>> items = new ArrayList<>();

            try {
                int clienteId = 1; // Simulado, puedes reemplazar con sesión o token

                CarritoRepository repo = new CarritoRepository();
                List<CarritoItem> carritoCliente = new ArrayList<>(repo.getCarritoByClienteId(clienteId));
                double totalGeneral = 0;

                for (CarritoItem item : carritoCliente) {
                    Map<String, Object> map = new HashMap<>();
                    Producto p = item.producto;

                    map.put("id", item.id);
                    map.put("nombre", p.getNombre());
                    map.put("descripcion", p.getDescripcion());
                    map.put("foto", p.getFoto());
                    map.put("precio", String.format("%.2f", p.getPrecio()));
                    map.put("cantidad", item.cantidad);
                    map.put("total", String.format("%.2f", p.getPrecio() * item.cantidad));

                    totalGeneral += p.getPrecio() * item.cantidad;
                    items.add(map);
                }

                model.put("items", items);
                model.put("totalGeneral", String.format("%.2f", totalGeneral));
                return new ModelAndView(model, "carrito.mustache");

            } catch (Exception e) {
                model.put("items", new ArrayList<>());
                model.put("error", "No se pudo cargar el carrito: " + e.getMessage());
                return new ModelAndView(model, "carrito.mustache");
            }
        }, new MustacheTemplateEngine());

        post("/carrito/add", (req, res) -> {
            int idProducto = Integer.parseInt(req.queryParams("id"));
            int cantidad = 1;

            // Obtener el carrito desde sesión, o crear uno nuevo
            List<CarritoItem> carrito = req.session().attribute("carrito");
            if (carrito == null) {
                carrito = new ArrayList<>();
            }

            // Crear el item y agregarlo
            CarritoItem item = new CarritoItem();
            item.id_pro = idProducto;
            item.cantidad = cantidad;
            item.id_cli = 1; // Simulado, puedes usar sesión real
            item.producto = new ProductoRepository().getProductoById(IdProducto); // si tienes este método

            carrito.add(item);
            req.session().attribute("carrito", carrito);

            System.out.println("Producto agregado: " + item.producto.getNombre());
            res.redirect("/catalog");
            return null;
        });


    }

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
            response.header("Access-Control-Allow-Headers", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        });
    }
}