package mprower.javaspark.repository;

import mprower.javaspark.config.Database;
import mprower.javaspark.model.Producto;
import mprower.javaspark.model.Venta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VentaRepository {

    public Venta crearVentaDesdeCarrito(int clienteId) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // ¡Iniciamos una transacción!

            // 1. Obtener los productos del carrito
            String sqlCarrito = "SELECT c.id_pro, c.cantidad, p.prec FROM carrito c JOIN producto p ON c.id_pro = p.id WHERE c.id_cli = ?";
            List<Object[]> itemsParaVenta = new ArrayList<>();
            double totalVenta = 0;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlCarrito)) {
                pstmt.setInt(1, clienteId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int idPro = rs.getInt("id_pro");
                    int cantidad = rs.getInt("cantidad");
                    double precio = rs.getDouble("prec");
                    itemsParaVenta.add(new Object[]{idPro, cantidad, precio});
                    totalVenta += cantidad * precio;
                }
            }

            if (itemsParaVenta.isEmpty()) {
                throw new IllegalStateException("El carrito está vacío, no se puede realizar la compra.");
            }

            // 2. Crear el registro en la tabla 'venta'
            Venta nuevaVenta = new Venta();
            nuevaVenta.setFolio(new Random().nextInt(900000) + 100000); // Folio aleatorio de 6 dígitos
            nuevaVenta.setTotal(totalVenta);
            nuevaVenta.setId_cli(clienteId);

            String sqlVenta = "INSERT INTO venta (fech, folio, total, id_cli) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setInt(2, nuevaVenta.getFolio());
                pstmt.setDouble(3, nuevaVenta.getTotal());
                pstmt.setInt(4, clienteId);
                pstmt.executeUpdate();

                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    nuevaVenta.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID de la venta.");
                }
            }

            // 3. Crear los registros en 'detalle_venta' (el trigger de la BD se encargará del stock)
            String sqlDetalle = "INSERT INTO detalle_venta (subtotal, cant, prec, id_vent, id_pro) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDetalle)) {
                for (Object[] item : itemsParaVenta) {
                    int idPro = (int) item[0];
                    int cantidad = (int) item[1];
                    double precio = (double) item[2];

                    pstmt.setDouble(1, cantidad * precio);
                    pstmt.setInt(2, cantidad);
                    pstmt.setDouble(3, precio);
                    pstmt.setInt(4, nuevaVenta.getId());
                    pstmt.setInt(5, idPro);
                    pstmt.addBatch(); // Agregamos la inserción al lote
                }
                pstmt.executeBatch(); // Ejecutamos todas las inserciones
            }

            // 4. Vaciar el carrito del cliente
            String sqlDeleteCarrito = "DELETE FROM carrito WHERE id_cli = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteCarrito)) {
                pstmt.setInt(1, clienteId);
                pstmt.executeUpdate();
            }

            conn.commit(); // ¡Confirmamos la transacción! Todo salió bien.
            return nuevaVenta;

        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // Si algo falla, deshacemos todo
            throw e; // Relanzamos la excepción para que el controlador la maneje
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}