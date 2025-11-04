package mprower.javaspark.model;

public class DetalleVenta {
    private int id;
    private double subtotal;
    private int cant;
    private double prec;
    private int id_vent;
    private int id_pro;

    // Getters y Setters para todos los campos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public int getCant() { return cant; }
    public void setCant(int cant) { this.cant = cant; }
    public double getPrec() { return prec; }
    public void setPrec(double prec) { this.prec = prec; }
    public int getId_vent() { return id_vent; }
    public void setId_vent(int id_vent) { this.id_vent = id_vent; }
    public int getId_pro() { return id_pro; }
    public void setId_pro(int id_pro) { this.id_pro = id_pro; }
}