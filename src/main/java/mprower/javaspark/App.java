package mprower.javaspark;

import mprower.javaspark.controller.ClienteController;
import mprower.javaspark.controller.ProductoController;
import mprower.javaspark.controller.CarritoController;
import mprower.javaspark.model.Cliente;

import java.util.*;
import static spark.Spark.*;

import mprower.javaspark.model.Producto;
import mprower.javaspark.repository.ProductoRepository;
import spark.ModelAndView;
import spark.Spark;
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

            try {
                Collection<Producto> productos = new ProductoRepository().getAllProductos();

                for (Producto p : productos) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", p.id);
                    item.put("nombre", p.nombre);
                    item.put("descripcion", p.descripcion);
                    item.put("prec", String.format("%.2f", p.precio));
                    item.put("foto", p.foto);
                    items.add(item);
                }

                model.put("items", items);
                return new ModelAndView(model, "catalog.mustache");
            } catch (Exception e) {
                model.put("items", new ArrayList<>());
                model.put("error", "No se pudo cargar el catálogo: " + e.getMessage());
                return new ModelAndView(model, "catalog.mustache");
            }
        }, new MustacheTemplateEngine());


    }

    private static ModelAndView renderLoginView() {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Iniciar sesión - Coleccionables");
        return new ModelAndView(model, "login.mustache");
    }
}