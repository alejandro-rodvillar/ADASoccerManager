package rodriguezvillar.alejandro.ADASoccerManager;

import java.util.Map;

public class Usuario {
    private String nombre;
    private String email;
    private Long monedas;
    private String ligaID;
    private Integer puntos;

    private Map<String, Jugador> equipoUsuario;

    public Usuario() {}

    public Usuario(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    public Usuario(String nombre, String email, Long monedas, String ligaID, Integer puntos) {
        this.nombre = nombre;
        this.email = email;
        this.monedas = monedas;
        this.ligaID = ligaID;
        this.puntos = puntos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getMonedas() {
        return monedas;
    }

    public void setMonedas(Long monedas) {
        this.monedas = monedas;
    }

    public String getLigaID() {
        return ligaID;
    }

    public void setLigaID(String ligaID) {
        this.ligaID = ligaID;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public Map<String, Jugador> getEquipoUsuario() {
        return equipoUsuario;
    }

    public void setEquipoUsuario(Map<String, Jugador> equipoUsuario) {
        this.equipoUsuario = equipoUsuario;
    }
}
