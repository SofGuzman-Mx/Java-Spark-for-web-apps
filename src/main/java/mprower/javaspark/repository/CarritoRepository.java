package mprower.javaspark.repository;

import mprower.javaspark.config.Database;
import mprower.javaspark.model.CarritoItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class CarritoRepository {

    /**
     * Agrega un nuevo producto al carrito de un cliente específico.
     * @param idCliente El ID del cliente.
     * @param idProducto El ID del producto a agregar.
     * @param cantidad La cantidad del producto.
     * @return El objeto CarritoItem recién creado.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public CarritoItem agregarAlCarrito(int idCliente, int idProducto, int cantidad) throws SQLException {
        // Podrías añadir lógica aquí para verificar si el item ya existe y en su lugar actualizar la cantidad.
        // Por ahora, simplemente lo inserta.
        String sql = "INSERT INTO carrito (id_cli, id_pro, cantidad) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idCliente);
            pstmt.setInt(2, idProducto);
            pstmt.setInt(3, cantidad);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    CarritoItem item = new CarritoItem();
                    item.id = generatedKeys.getInt(1);
                    item.id_cli = idCliente;
                    item.id_pro = idProducto;
                    item.cantidad = cantidad;
                    return item;
                } else {
                    throw new SQLException("Fallo al agregar al carrito, no se obtuvo ID.");
                }
            }
        }
    }

    /**
     * Obtiene todos los items del carrito para un cliente específico.
     * @param idCliente El ID del cliente cuyo carrito se quiere obtener.
     * @return Una colección de objetos CarritoItem.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public Collection<CarritoItem> getCarritoByClienteId(int idCliente) throws SQLException {
        Collection<CarritoItem> carrito = new ArrayList<>();
        String sql = "SELECT * FROM carrito WHERE id_cli = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    carrito.add(mapRowToCarritoItem(rs));
                }
            }
        }
        return carrito;
    }

    /**
     * Actualiza la cantidad de un item específico en el carrito.
     * @param idCarrito El ID del registro en la tabla 'carrito' a actualizar.
     * @param nuevaCantidad La nueva cantidad para el item.
     * @return true si la actualización fue exitosa, false si el item no fue encontrado.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean actualizarCantidad(int idCarrito, int nuevaCantidad) throws SQLException {
        // Si la nueva cantidad es 0 o menos, eliminamos el item.
        if (nuevaCantidad <= 0) {
            return eliminarDelCarrito(idCarrito);
        }

        String sql = "UPDATE carrito SET cantidad = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevaCantidad);
            pstmt.setInt(2, idCarrito);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Elimina un item del carrito por su ID.
     * @param idCarrito El ID del registro en la tabla 'carrito' a eliminar.
     * @return true si la eliminación fue exitosa, false si el item no fue encontrado.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean eliminarDelCarrito(int idCarrito) throws SQLException {
        String sql = "DELETE FROM carrito WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCarrito);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Método de utilidad para mapear una fila del ResultSet a un objeto CarritoItem.
     * @param rs El ResultSet posicionado en la fila correcta.
     * @return un objeto CarritoItem poblado.
     * @throws SQLException Si hay un error al leer del ResultSet.
     */
    private CarritoItem mapRowToCarritoItem(ResultSet rs) throws SQLException {
        CarritoItem item = new CarritoItem();
        item.id = rs.getInt("id");
        item.id_cli = rs.getInt("id_cli");
        item.id_pro = rs.getInt("id_pro");
        item.cantidad = rs.getInt("cantidad");
        return item;
    }
}