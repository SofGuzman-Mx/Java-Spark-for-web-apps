package mprower.javaspark.repository;

import mprower.javaspark.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maneja la lógica de negocio y el almacenamiento de datos (en memoria) para los Usuarios.
 */
public class UserRepository {

    // Usamos String para el ID para simplificar el parseo en los endpoints (req.params(":id"))
    // aunque el requisito pedía UUID, lo almacenamos como String.
    private final Map<String, User> users = new ConcurrentHashMap<>();

    /**
     * Crea un nuevo usuario, le asigna ID y createdAt, y lo guarda.
     * @param user El usuario a crear (sin id, sin createdAt)
     * @return El usuario completo, ya guardado.
     * @throws IllegalArgumentException si la validación falla.
     */
    public User createUser(User user) {
        // 1. Validar campos
        user.validate();

        // 2. Asignar campos del servidor
        user.id = UUID.randomUUID().toString();
        user.createdAt = Instant.now().toString();

        // 3. Guardar
        users.put(user.id, user);
        return user;
    }

    /**
     * Devuelve todos los usuarios.
     * @return Una colección de todos los usuarios.
     */
    public Collection<User> getAllUsers() {
        return users.values();
    }

    /**
     * Busca un usuario por su ID.
     * @param id El ID (String) del usuario.
     * @return Un Optional que contiene el User si se encuentra, o vacío si no.
     */
    public Optional<User> getUserById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Actualiza un usuario existente.
     * @param id El ID del usuario a actualizar.
     * @param updatedUser El objeto usuario con los nuevos datos.
     * @return Un Optional con el usuario actualizado, o vacío si el ID no existe.
     * @throws IllegalArgumentException si la validación falla.
     */
    public Optional<User> updateUser(String id, User updatedUser) {
        if (!users.containsKey(id)) {
            return Optional.empty(); // No encontrado
        }

        // Validar el usuario entrante
        updatedUser.validate();

        // Preservar el ID y la fecha de creación originales
        User existingUser = users.get(id);
        updatedUser.id = id;
        updatedUser.createdAt = existingUser.createdAt; // No permitir que se sobreescriba

        // Guardar (reemplazar)
        users.put(id, updatedUser);
        return Optional.of(updatedUser);
    }

    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar.
     * @return true si el usuario existía y fue eliminado, false si no.
     */
    public boolean deleteUser(String id) {
        return users.remove(id) != null;
    }
}