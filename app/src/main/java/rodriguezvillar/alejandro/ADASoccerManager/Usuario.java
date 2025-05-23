package rodriguezvillar.alejandro.ADASoccerManager;

public class Usuario {
    private String nombre;
    private String email;

    // Constructor vacío requerido por Firestore
    public Usuario() {}

    public Usuario(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y setters (recomendado aunque no siempre obligatorio)
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
        this.email =email;
    }

}
