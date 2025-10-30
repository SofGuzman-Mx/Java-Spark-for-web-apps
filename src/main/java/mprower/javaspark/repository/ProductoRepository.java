package mprower.javaspark.repository;

import mprower.javaspark.config.Database;
import mprower.javaspark.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ProductoRepository {

    public Collection<Producto> getAllProductos() throws SQLException {
        Collection<Producto> productos = new ArrayList<>();
        // Usamos un JOIN para obtener la descripción
        String sql = "SELECT p.*, d.descripcion FROM producto p JOIN descripcion d ON p.id_descr = d.id";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapRowToProducto(rs));
            }
        }
        return productos;
    }

    public Optional<Producto> getProductoById(int id) throws SQLException {
        String sql = "SELECT p.*, d.descripcion FROM producto p JOIN descripcion d ON p.id_descr = d.id WHERE p.id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProducto(rs));
                }
            }
        }
        return Optional.empty();
    }

    // CRUD de productos (Create, Update, Delete) puede ser añadido aquí

    private Producto mapRowToProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.id = rs.getInt("id");
        p.nombre = rs.getString("nombre");
        p.precio = rs.getDouble("prec");
        p.foto = rs.getString("foto");
        p.cantidad = rs.getInt("cantidad");
        p.descripcion = rs.getString("descripcion");
        return p;
    }
}