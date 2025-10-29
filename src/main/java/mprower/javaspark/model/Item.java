package mprower.javaspark.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de datos (POJO) para un Artículo Coleccionable.
 */
public class Item {

    public String id;
    public String name;
    public String description;
    public String ownerId; // Para saber a qué User le pertenece (opcional)
    public String createdAt;
    public String price;

    public Item() {
        // Constructor vacío para Gson
    }

    /**
     * Valida los campos obligatorios.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();

        if (isNullOrBlank(name)) {
            errors.add("El campo 'name' es obligatorio.");
        }

        // Si la lista de errores no está vacía, lanzamos una excepción.
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}