package mprower.javaspark;

import mprower.javaspark.config.Database;
import mprower.javaspark.controller.ClienteController;
import mprower.javaspark.controller.ProductoController;
import mprower.javaspark.controller.CarritoController;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import static spark.Spark.*;

import mprower.javaspark.model.Producto;
import mprower.javaspark.repository.ProductoRepository;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class App {
    public static void main(String[] args) {
        port(8080);

        staticFiles.location("/public");
        // Habilitar CORS
        enableCORS();

        // Inicializar controladores
        new ClienteController();
        new ProductoController();
        new CarritoController(); // Cuando lo implementes

        System.out.println("Servidor iniciado en http://localhost:8080");
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

        get("/login", (req, res) -> {
            return renderLoginView();
        }, new MustacheTemplateEngine());

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
    }


    private static ModelAndView renderLoginView() {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Iniciar sesión - Coleccionables");
        return new ModelAndView(model, "login.mustache");
    }

}