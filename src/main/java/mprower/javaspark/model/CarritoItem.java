package mprower.javaspark.model;

// Representa un item en el carrito de un cliente
public class CarritoItem {
    public int id; // ID del registro en la tabla carrito
    public int id_cli;
    public int id_pro;
    public int cantidad;

    // Opcional: para mostrar detalles en la respuesta de la API
    public Producto producto;
}