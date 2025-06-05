package rodriguezvillar.alejandro.ADASoccerManager;

public class Jugador {
    private String id;
    private String nombre;
    private String equipo;
    private String posicion;
    private int precio;
    private int puntos;
    private String estado;
    private String propietarioNombre;

    public Jugador() {} // Requerido por Firestore

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEquipo() { return equipo; }
    public String getPosicion() { return posicion; }
    public int getPrecio() { return precio; }
    public int getPuntos() { return puntos; }
    public String getEstado() { return estado; }
    public String getPropietarioNombre() { return propietarioNombre; }

    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEquipo(String equipo) { this.equipo = equipo; }
    public void setPosicion(String posicion) { this.posicion = posicion; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public void setPrecio(int precio) { this.precio = precio; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setPropietarioNombre(String propietarioNombre) { this.propietarioNombre = propietarioNombre; }
}
