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

        // Enlaces UI
        tvNombreLiga = findViewById(R.id.tvNombreLiga);
        tvCreadorLiga = findViewById(R.id.tvCreadorLiga);
        tvParticipantes = findViewById(R.id.tvParticipantes);
        tvCodigoLiga = findViewById(R.id.tvCodigoLiga);
        btnCompartirCodigo = findViewById(R.id.btnCompartirCodigo);
        btnSalirLiga = findViewById(R.id.btnSalirLiga);
        btnEliminarLiga = findViewById(R.id.btnEliminarLiga);

        // Buscar liga a la que pertenece el usuario
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

                        // Aquí hacemos la consulta para obtener el nombre del creador
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
                                        // Si hay error, mostramos el UID como fallback
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

        // Compartir código
        btnCompartirCodigo.setOnClickListener(v -> {
            if (ligaIdActual != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "¡Únete a mi liga con este código: " + ligaIdActual + "!");
                startActivity(Intent.createChooser(intent, "Compartir código con:"));
            }
        });

        // Salir de liga
        btnSalirLiga.setOnClickListener(v -> {
            if (ligaIdActual != null) {
                salirDeLiga(ligaIdActual, uid);
            }
        });

        // Eliminar liga
        btnEliminarLiga.setOnClickListener(v -> {
            if (ligaIdActual != null) {
                eliminarLiga(ligaIdActual);
            }
        });

        // Toolbar y menú lateral
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
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
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
        // Eliminar campo ligaId del usuario
        mDatabase.child("usuarios").child(userId).child("ligaId").removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Eliminar usuario de la lista de jugadores en la liga
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

    private void eliminarLiga(String ligaId) {
        // Obtener lista de jugadores
        mDatabase.child("ligas").child(ligaId).child("jugadores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalJugadores = (int) snapshot.getChildrenCount();
                    if (totalJugadores == 0) {
                        borrarLigaYTerminar(ligaId);
                        return;
                    }

                    final int[] eliminados = {0};
                    for (DataSnapshot jugadorSnap : snapshot.getChildren()) {
                        String jugadorId = jugadorSnap.getKey();
                        mDatabase.child("usuarios").child(jugadorId).child("ligaId").removeValue()
                                .addOnCompleteListener(task -> {
                                    eliminados[0]++;
                                    if (eliminados[0] == totalJugadores) {
                                        borrarLigaYTerminar(ligaId);
                                    }
                                });
                    }
                } else {
                    // No hay jugadores, eliminar liga directamente
                    borrarLigaYTerminar(ligaId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionLigaActivity.this, "Error leyendo jugadores: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void borrarLigaYTerminar(String ligaId) {
        mDatabase.child("ligas").child(ligaId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(GestionLigaActivity.this, "Liga eliminada y usuarios actualizados", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(GestionLigaActivity.this, "Error eliminando liga", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
