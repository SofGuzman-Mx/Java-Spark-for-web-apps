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
    }
}