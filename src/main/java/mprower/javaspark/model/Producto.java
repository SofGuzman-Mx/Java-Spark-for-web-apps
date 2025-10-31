package mprower.javaspark.model;

// Corresponde a las tablas 'producto' y 'descripcion'
public class Producto {
    public int id;
    public String nombre;
    public double precio; // Usar double o BigDecimal para precios
    public String foto;
    public int cantidad;
    public String descripcion; // Obtenida con un JOIN

    // ... getters y setters ...
}