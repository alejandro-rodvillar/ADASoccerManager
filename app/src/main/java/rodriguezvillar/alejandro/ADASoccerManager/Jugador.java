package rodriguezvillar.alejandro.ADASoccerManager;

public class Jugador {
    private String nombre;
    private String equipo;
    private String posicion;
    private double precio;
    private int puntos;

    public Jugador() {} // Requerido por Firestore

    public String getNombre() { return nombre; }
    public String getEquipo() { return equipo; }
    public String getPosicion() { return posicion; }
    public double getPrecio() { return precio; }
    public int getPuntos() { return puntos; }
}
