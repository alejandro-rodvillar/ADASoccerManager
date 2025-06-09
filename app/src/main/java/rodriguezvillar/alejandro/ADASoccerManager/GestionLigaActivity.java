package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class GestionLigaActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private DatabaseReference mDatabase;

    private TextView tvNombreLiga, tvCreadorLiga, tvParticipantes, tvCodigoLiga;
    private Button btnCompartirCodigo, btnSalirLiga, btnEliminarLiga;

    private String ligaIdActual;
    private boolean esCreador = false;

    private FirebaseUser currentUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_liga);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        uid = currentUser.getUid();

        // enlaces UI
        tvNombreLiga = findViewById(R.id.tvNombreLiga);
        tvCreadorLiga = findViewById(R.id.tvCreadorLiga);
        tvParticipantes = findViewById(R.id.tvParticipantes);
        tvCodigoLiga = findViewById(R.id.tvCodigoLiga);
        btnCompartirCodigo = findViewById(R.id.btnCompartirCodigo);
        btnSalirLiga = findViewById(R.id.btnSalirLiga);
        btnEliminarLiga = findViewById(R.id.btnEliminarLiga);

        // buscar liga a la que pertenece el usuario
        mDatabase.child("ligas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ligaSnapshot : snapshot.getChildren()) {
                    if (ligaSnapshot.child("jugadores").hasChild(uid)) {
                        ligaIdActual = ligaSnapshot.getKey();
                        String nombreLiga = ligaSnapshot.child("nombre").getValue(String.class);
                        String creadorUid = ligaSnapshot.child("creador").getValue(String.class);

                        esCreador = uid.equals(creadorUid);
                        tvNombreLiga.setText("Nombre de la Liga: " + nombreLiga);
                        tvCodigoLiga.setText("Código de la Liga: " + ligaIdActual);
                        tvParticipantes.setText("Participantes: " + ligaSnapshot.child("jugadores").getChildrenCount());

                        mDatabase.child("usuarios").child(creadorUid).child("nombre")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot nombreSnapshot) {
                                        String nombreCreador = nombreSnapshot.getValue(String.class);
                                        if (nombreCreador == null || nombreCreador.isEmpty()) {
                                            nombreCreador = "Desconocido";
                                        }
                                        tvCreadorLiga.setText("Creador: " + nombreCreador);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        tvCreadorLiga.setText("Creador: " + creadorUid);
                                    }
                                });

                        if (esCreador) {
                            btnEliminarLiga.setVisibility(View.VISIBLE);
                            btnSalirLiga.setVisibility(View.GONE);
                        } else {
                            btnSalirLiga.setVisibility(View.VISIBLE);
                            btnEliminarLiga.setVisibility(View.GONE);
                        }

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionLigaActivity.this, "Error cargando liga", Toast.LENGTH_SHORT).show();
            }
        });

        btnCompartirCodigo.setOnClickListener(v -> {
            if (ligaIdActual != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "¡Únete a mi liga con este código: " + ligaIdActual + "!");
                startActivity(Intent.createChooser(intent, "Compartir código con:"));
            }
        });

        btnSalirLiga.setOnClickListener(v -> {
            if (ligaIdActual != null) {
                salirDeLiga(ligaIdActual, uid);
            }
        });

        btnEliminarLiga.setOnClickListener(v -> {
            if (ligaIdActual != null) {
                new android.app.AlertDialog.Builder(GestionLigaActivity.this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que quieres eliminar esta liga? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar", (dialog, which) -> eliminarLiga(ligaIdActual))
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PerfilUsuarioActivity.class));
            }
            else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                prefs.edit().clear().apply();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(this, MercadoActivity.class));
                return true;
            }
            return false;
        });
    }

    private void salirDeLiga(String ligaId, String userId) {
        // acceder al equipo del usuario (equipoUsuario)
        mDatabase.child("usuarios").child(userId).child("equipoUsuario")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot equipoSnapshot) {
                        if (equipoSnapshot.exists()) {
                            for (DataSnapshot jugadorIdSnap : equipoSnapshot.getChildren()) {
                                String jugadorId = jugadorIdSnap.getKey();

                                // cambiar estado del jugador a "disponible" y limpiar propietario
                                mDatabase.child("jugadores").child(jugadorId).child("estado").setValue("disponible");
                                mDatabase.child("jugadores").child(jugadorId).child("propietarioNombre").removeValue();
                            }
                        }

                        // borrar datos del usuario relacionados con la liga
                        mDatabase.child("usuarios").child(userId).child("ligaId").removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                mDatabase.child("usuarios").child(userId).child("puntos").removeValue();
                                mDatabase.child("usuarios").child(userId).child("monedas").removeValue();
                                mDatabase.child("usuarios").child(userId).child("equipoUsuario").removeValue();

                                mDatabase.child("ligas").child(ligaId).child("jugadores").child(userId).removeValue()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                Toast.makeText(GestionLigaActivity.this, "Has salido de la liga", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(GestionLigaActivity.this, "Error al salir de la liga", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(GestionLigaActivity.this, "Error al actualizar usuario", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GestionLigaActivity.this, "Error accediendo al equipo del usuario", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void eliminarLiga(String ligaId) {
        mDatabase.child("ligas").child(ligaId).child("jugadores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalJugadores = (int) snapshot.getChildrenCount();
                    if (totalJugadores == 0) {
                        actualizarTodosLosJugadoresADisponible(() -> borrarLigaYTerminar(ligaId));
                        return;
                    }

                    final int[] eliminados = {0};
                    for (DataSnapshot jugadorSnap : snapshot.getChildren()) {
                        String jugadorId = jugadorSnap.getKey();
                        mDatabase.child("usuarios").child(jugadorId).child("equipoUsuario").removeValue();
                        mDatabase.child("usuarios").child(jugadorId).child("puntos").removeValue();
                        mDatabase.child("usuarios").child(jugadorId).child("monedas").removeValue();
                        mDatabase.child("usuarios").child(jugadorId).child("ligaId").removeValue()
                                .addOnCompleteListener(task -> {
                                    eliminados[0]++;
                                    if (eliminados[0] == totalJugadores) {
                                        actualizarTodosLosJugadoresADisponible(() -> borrarLigaYTerminar(ligaId));
                                    }
                                });
                    }
                } else {
                    actualizarTodosLosJugadoresADisponible(() -> borrarLigaYTerminar(ligaId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionLigaActivity.this, "Error leyendo jugadores: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarTodosLosJugadoresADisponible(Runnable onComplete) {
        mDatabase.child("jugadores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = (int) snapshot.getChildrenCount();
                if (total == 0) {
                    onComplete.run();
                    return;
                }

                final int[] actualizados = {0};
                for (DataSnapshot jugadorSnap : snapshot.getChildren()) {
                    jugadorSnap.getRef().child("estado").setValue("disponible");
                    actualizados[0]++;
                    if (actualizados[0] == total) {
                        onComplete.run();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionLigaActivity.this, "Error actualizando estado jugadores", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void borrarLigaYTerminar(String ligaId) {
        mDatabase.child("ligas").child(ligaId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(GestionLigaActivity.this, "Liga eliminada y jugadores liberados", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(GestionLigaActivity.this, "Error eliminando liga", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
