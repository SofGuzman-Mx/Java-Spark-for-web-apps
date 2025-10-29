package mprower.javaspark.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id; // Será un UUID en formato String
    public String username;
    public String email;
    public String role;
    public String createdAt; // Usaremos un String en formato ISO (Instant.now().toString())

    // Constructor vacío (necesario para Gson)
    public User() {
    }

    // Constructor útil para crear un usuario desde la API
    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }


    /**
     * Método helper para validar si un String es nulo, vacío o solo espacios.
     */
    private boolean isNullOrBlank(String str) {
        // Si usas Java 11+, puedes reemplazar str.trim().isEmpty() por str.isBlank()
        return str == null || str.isBlank();
    }
    /**
     * Valida que los campos obligatorios para la creación/actualización estén presentes.
     * @throws IllegalArgumentException si un campo falta.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();

        if (isNullOrBlank(username)) {
            errors.add("El campo 'username' es obligatorio.");
        }
        if (isNullOrBlank(email)) {
            errors.add("El campo 'email' es obligatorio.");
        }
        if (isNullOrBlank(role)) {
            errors.add("El campo 'role' es obligatorio.");
        }

        // Si la lista de errores no está vacía, lanzamos una excepción con todos los mensajes.
        if (!errors.isEmpty()) {
            // Unimos todos los mensajes de error con una coma
            String combinedErrors = String.join(", ", errors);
            throw new IllegalArgumentException(combinedErrors);
        }
    }
}

