package mprower.javaspark.repository;

import mprower.javaspark.config.Database;
import mprower.javaspark.model.Cliente;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;

public class ClienteRepository {

    public Cliente registrar(Cliente cliente) throws SQLException {
        // Hashear la contraseña antes de guardarla
        String hashedPassword = BCrypt.hashpw(cliente.password, BCrypt.gensalt());

        String sql = "INSERT INTO cliente (nombre, password, numero) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cliente.nombre);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, cliente.numero);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.id = generatedKeys.getInt(1);
                    cliente.password = null; // No devolver el hash
                    return cliente;
                } else {
                    throw new SQLException("Fallo al crear cliente, no se obtuvo ID.");
                }
            }
        }
    }

    public Optional<Cliente> findByNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE nombre = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.id = rs.getInt("id");
                cliente.nombre = rs.getString("nombre");
                cliente.password = rs.getString("password"); // Necesitamos el hash para comparar
                cliente.numero = rs.getString("numero");
                return Optional.of(cliente);
            }
        }
        return Optional.empty();
    }
}