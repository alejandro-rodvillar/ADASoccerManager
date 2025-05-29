package rodriguezvillar.alejandro.ADASoccerManager;

public class Jugador {
    private String nombre;
    private String equipo;
    private String posicion;
    private int precio;
    private int puntos;
    private String estado;

    public Jugador() {} // Requerido por Firestore

    public String getNombre() { return nombre; }
    public String getEquipo() { return equipo; }
    public String getPosicion() { return posicion; }
    public int getPrecio() { return precio; }
    public int getPuntos() { return puntos; }
    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }
}
