package mprower.javaspark.model;

import java.util.Date;
import java.util.List;

public class Venta {
    private int id;
    private Date fecha;
    private int folio;
    private double total;
    private int id_cli;
    private List<DetalleVenta> detalles; // Opcional, para contener los detalles

    // Getters y Setters para todos los campos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public int getFolio() { return folio; }
    public void setFolio(int folio) { this.folio = folio; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public int getId_cli() { return id_cli; }
    public void setId_cli(int id_cli) { this.id_cli = id_cli; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}