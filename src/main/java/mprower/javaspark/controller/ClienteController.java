package mprower.javaspark.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import mprower.javaspark.model.Cliente;
import mprower.javaspark.repository.ClienteRepository;
import mprower.javaspark.util.Auth;
import mprower.javaspark.util.ErrorResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.Optional;

import static spark.Spark.*;

public class ClienteController {

    private final ClienteRepository repository;
    private final Gson gson;

    public ClienteController() {
        this.repository = new ClienteRepository();
        this.gson = new Gson();
        initializeRoutes();
    }

    private void initializeRoutes() {
        // Endpoint de registro adaptado para formulario HTML
        post("/api/register", (req, res) -> {
            res.type("application/json");
            try {
                // Leer parámetros del formulario
                String nombre = req.queryParams("nombre");
                String numero = req.queryParams("numero");
                String password = req.queryParams("password");

                // Crear cliente
                Cliente nuevoCliente = new Cliente();
                nuevoCliente.setNombre(nombre);
                nuevoCliente.setNumero(numero);
                nuevoCliente.setPassword(password);

                // Guardar en la base de datos
                Cliente clienteRegistrado = repository.registrar(nuevoCliente);

                res.status(201);
                return gson.toJson(clienteRegistrado);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error al registrar cliente: " + e.getMessage()));
            }
        });

    // Endpoint de login
        post("/api/login", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject loginInfo = gson.fromJson(req.body(), JsonObject.class);
                String nombre = loginInfo.get("nombre").getAsString();
                String password = loginInfo.get("password").getAsString();

                Optional<Cliente> clienteOpt = repository.findByNombre(nombre);

                if (clienteOpt.isPresent() && BCrypt.checkpw(password, clienteOpt.get().password)) {
                    Cliente cliente = clienteOpt.get();
                    String token = Auth.generateToken(cliente.id, cliente.nombre);
                    return gson.toJson(Map.of("token", token));
                } else {
                    res.status(401); // Unauthorized
                    return gson.toJson(new ErrorResponse("401", "Credenciales incorrectas"));
                }
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ErrorResponse("500", "Error en el login: " + e.getMessage()));
            }
        });
    }
}