package mprower.javaspark;

import mprower.javaspark.controller.ClienteController;
import mprower.javaspark.controller.ProductoController;
import mprower.javaspark.controller.CarritoController;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.*;

import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        port(8080);

        // Servir archivos estáticos desde /public
        staticFiles.location("/public");

        // Habilitar CORS
        enableCORS();

        // Inicializar controladores
        new ClienteController();
        new ProductoController();
        new CarritoController();

        // Endpoint para mostrar el catálogo
        get("/signup", (req, res) -> renderSignupView(), new MustacheTemplateEngine());

        get("/catalog", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<Map<String, Object>> items = new ArrayList<>();
            items.add(Map.of("id", "1", "nombre", "Gorra autografiada por Peso Pluma", "descripcion" ,"Una gorra autografiada por el famoso Peso Pluma", "prec", "621.34 USD", "foto", "/img/gorraPP.jpg"));
            items.add(Map.of("id", "2", "nombre", "Casco autografiado por Rosalía", "descripcion", "Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!", "prec", "734.57 USD", "foto", "/img/cascoRosalia.jpg"));
            items.add(Map.of("id", "3", "nombre", "Chamarra de Bad Bunny", "descripcion", "Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista.", "prec", "521.89 USD", "foto", "/img/chamarraBB.png"));
            items.add(Map.of("id", "4", "nombre", "Guitarra de Fernando Delgadillo", "descripcion", "Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo.", "prec", "823.12 USD", "foto", "/img/guitarraFD.jpg"));
            items.add(Map.of("id", "5", "nombre", "Jersey firmado por Snoop Dogg", "descripcion", "Un jersey autografiado por el legendario rapero Snoop Dogg.", "prec", "355.67 USD", "foto", "/img/jerseySD.png"));
            items.add(Map.of("id", "6", "nombre", "Prenda de Cardi B autografiada", "descripcion", "Una playera usada y autografiada por la famosa rapera Cardi B. en su última visita a México", "prec", "674.23 USD", "foto", "/img/playeraCB.png"));
            items.add(Map.of("id", "7", "nombre", "Guitarra autografiada por Coldplay", "descripcion", "Una guitarra eléctrica autografiada por la popular banda británica Coldplay, un día antes de su concierto en Monterrey en 2022", "prec", "458.91 USD", "foto", "/img/guitarraColdplay.png"));

            model.put("items", items);
            return new ModelAndView(model, "catalog.mustache");
        }, new MustacheTemplateEngine());

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

        get("/item/:id", (req, res) -> {
            String id = req.params(":id");

            // Simulación de búsqueda (reemplaza con tu repositorio real)
            Map<String, Object> item = Map.of(
                    "id", id,
                    "nombre", "Gorra autografiada por Peso Pluma",
                    "descripcion", "Una gorra autografiada por el famoso Peso Pluma",
                    "prec", "621.34",
                    "foto", "/img/gorraPP.jpg"
            );

            private static ModelAndView renderSignupView() {
                Map<String, Object> model = new HashMap<>();
                model.put("title", "Registro");
                model.put("message", "Crea tu cuenta para acceder al catálogo de coleccionables");
                model.put("error", "");
                return new ModelAndView(model, "signup.mustache");
            }


            return new ModelAndView(item, "item-detail.mustache");
        }, new MustacheTemplateEngine());

    }
}
