package rodriguezvillar.alejandro.ADASoccerManager;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class JugadorActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // leer jugadores
        leerJugadores();
    }

    private void leerJugadores() {
        db.collection("jugadores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Jugador jugador = document.toObject(Jugador.class);
                            Log.d("Jugador", "Nombre: " + jugador.getNombre() +
                                    ", Equipo: " + jugador.getEquipo() +
                                    ", Posición: " + jugador.getPosicion() +
                                    ", Precio: " + jugador.getPrecio() +
                                    ", Puntos: " + jugador.getPuntos());
                        }
                    } else {
                        Log.e("Firestore", "Error al leer jugadores", task.getException());
                    }
                });
    }
}
