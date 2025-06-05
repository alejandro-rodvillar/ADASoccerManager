package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.UUID;

public class UnirseLiga extends AppCompatActivity {

    private EditText etLeagueCode;
    private Button btnJoinLeague;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;
    private static final String TAG = "UnirseLiga";

    // Añadido BottomNavigationView
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_liga);

        etLeagueCode = findViewById(R.id.etLeagueCode);
        btnJoinLeague = findViewById(R.id.btnJoinLeague);
        dbRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Inicializar BottomNavigationView
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Listener para seleccionar menú
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(UnirseLiga.this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(UnirseLiga.this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(UnirseLiga.this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(UnirseLiga.this, MercadoActivity.class));
                return true;
            }
            return false;
        });

        btnJoinLeague.setOnClickListener(v -> {
            String codigoLiga = etLeagueCode.getText().toString().trim();

            if (TextUtils.isEmpty(codigoLiga)) {
                Toast.makeText(UnirseLiga.this, "Introduce un código de liga", Toast.LENGTH_SHORT).show();
                return;
            }

            unirseALiga(codigoLiga);
        });
    }

    private void unirseALiga(String codigoLiga) {
        dbRef.child("ligas").orderByChild("ligaId").equalTo(codigoLiga)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(UnirseLiga.this, "La liga no existe", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot ligaSnapshot : snapshot.getChildren()) {
                            String ligaKey = ligaSnapshot.getKey();
                            Long maxParticipantes = ligaSnapshot.child("maxParticipantes").getValue(Long.class);
                            DataSnapshot jugadoresSnapshot = ligaSnapshot.child("jugadores");

                            int numJugadoresActuales = (int) jugadoresSnapshot.getChildrenCount();

                            if (numJugadoresActuales >= maxParticipantes) {
                                Toast.makeText(UnirseLiga.this, "La liga está llena", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = currentUser.getUid();
                            String teamId = UUID.randomUUID().toString().substring(0, 8);

                            // Añadir el usuario a la liga
                            dbRef.child("ligas").child(ligaKey).child("jugadores").child(uid).setValue(true)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Añadir ligaId
                                            DatabaseReference usuarioRef = dbRef.child("usuarios").child(uid);
                                            usuarioRef.child("ligaId").setValue(codigoLiga)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            //generar equipo
                                                            usuarioRef.child("equipo").setValue(teamId);
                                                            // añadir puntos
                                                            usuarioRef.child("puntos").setValue(0);
                                                            //Añadir monedas al usuario
                                                            usuarioRef.child("monedas").setValue(100000)
                                                                    .addOnCompleteListener(task3 -> {
                                                                        if (task3.isSuccessful()) {
                                                                            Toast.makeText(UnirseLiga.this, "Te has unido a la liga correctamente", Toast.LENGTH_SHORT).show();
                                                                            finish(); // Redirigir si lo deseas
                                                                        } else {
                                                                            Toast.makeText(UnirseLiga.this, "Error al asignar monedas al usuario", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                        } else {
                                                            Toast.makeText(UnirseLiga.this, "Error al vincular la liga al usuario", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(UnirseLiga.this, "Error al añadirte a la liga", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                            break; // Ya hemos encontrado la liga
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error al buscar la liga: " + error.getMessage());
                        Toast.makeText(UnirseLiga.this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
