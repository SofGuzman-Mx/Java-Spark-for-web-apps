package mprower.javaspark.model;

// Corresponde a la tabla 'cliente'
public class Cliente {
    public int id;
    public String nombre;
    public String password; // Solo para recibirlo, no para enviarlo de vuelta
    public String numero;

    // Getters y setters son útiles para Gson y para un código más limpio
    // ... puedes agregarlos si quieres, pero los campos públicos funcionan
}