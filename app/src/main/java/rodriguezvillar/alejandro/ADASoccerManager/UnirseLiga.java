package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.UUID;

public class UnirseLiga extends AppCompatActivity {

    private EditText etLeagueCode;
    private Button btnJoinLeague;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;
    private static final String TAG = "UnirseLiga";

    private BottomNavigationView bottomNavigation;

    // Menú lateral
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_liga);

        etLeagueCode = findViewById(R.id.etLeagueCode);
        btnJoinLeague = findViewById(R.id.btnJoinLeague);
        dbRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Configurar Toolbar y Navigation Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PerfilUsuarioActivity.class));
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

        // Inicializar BottomNavigationView
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(UnirseLiga.this, MainActivity.class));
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
                                            DatabaseReference usuarioRef = dbRef.child("usuarios").child(uid);
                                            usuarioRef.child("ligaId").setValue(codigoLiga)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            usuarioRef.child("equipoUsuario").setValue(teamId);
                                                            usuarioRef.child("puntos").setValue(0);
                                                            usuarioRef.child("monedas").setValue(100000)
                                                                    .addOnCompleteListener(task3 -> {
                                                                        if (task3.isSuccessful()) {
                                                                            Toast.makeText(UnirseLiga.this, "Te has unido a la liga correctamente", Toast.LENGTH_SHORT).show();
                                                                            finish();
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
                            break;
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
