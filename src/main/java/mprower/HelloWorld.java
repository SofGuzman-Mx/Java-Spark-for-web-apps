package mprower;

import com.google.gson.Gson;

import static spark.Spark.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HelloWorld {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // 1. Obtener el puerto configurado
        int appPort = getPort();

        // 2. Establecer el puerto en Spark
        port(appPort);

        // En main justo después de port()
        exception(Exception.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson(new Exception("Internal server error")));
            e.printStackTrace();
        });

        // 3. Inicializar el resto de tu API (ej. tus rutas de UserApi)
        // Puedes mover el código de tu 'UserApi.main()' a un método 'init()'
        // y llamarlo aquí.
        // new UserApi().init(); // <- Ejemplo de cómo lo estructurarías

        // Ruta de ejemplo para probar que el puerto funciona
        get("/hello-port", (req, res) -> {
            res.type("text/plain");
            return "¡Hola! El servidor está corriendo en el puerto: " + appPort;
        });

        System.out.println("Servidor Spark iniciado. Escuchando en http://localhost:" + appPort);
    }

    /**
     * Obtiene el puerto de tres fuentes, en orden de prioridad:
     * 1. Variable de entorno 'PORT'.
     * 2. Archivo 'application.properties' (propiedad 'app.port').
     * 3. Puerto por defecto '4567'.
     *
     * @return El puerto a utilizar.
     */
    private static int getPort() {
        // 1. Valor por defecto (mínima prioridad)
        int defaultPort = 4567;

        // 2. Intentar leer de application.properties (media prioridad)
        Properties properties = new Properties();
        String propertiesPort = null;
        try (InputStream input = HelloWorld.class.getResourceAsStream("/application.properties")) {

            if (input == null) {
                System.out.println("No se encontró 'application.properties'. Usando puerto por defecto o variable de entorno.");
            } else {
                properties.load(input);
                propertiesPort = properties.getProperty("app.port");
            }

        } catch (IOException ex) {
            System.err.println("Error al leer 'application.properties': " + ex.getMessage());
        }

        if (propertiesPort != null) {
            try {
                defaultPort = Integer.parseInt(propertiesPort);
                System.out.println("Port loaded from 'application.properties': " + defaultPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid 'app.port' value in 'application.properties'. Ignoring.");
            }
        }

        // 3. Intentar leer de Variable de Entorno (máxima prioridad)
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                int port = Integer.parseInt(envPort);
                System.out.println("Puerto cargado desde variable de entorno 'PORT': " + port);
                return port; // La variable de entorno GANA
            } catch (NumberFormatException e) {
                System.err.println("Variable de entorno 'PORT' inválida. Usando puerto de properties o por defecto.");
            }
        }

        // Devuelve el puerto de properties o el default si no hay variable de entorno
        return defaultPort;
    }
}
