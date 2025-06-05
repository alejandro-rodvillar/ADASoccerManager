package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class UnirseLiga extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText etLeagueCode;
    private Button btnJoinLeague;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;

    private static final String TAG = "UnirseLiga";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_liga);

        etLeagueCode = findViewById(R.id.etLeagueCode);
        btnJoinLeague = findViewById(R.id.btnJoinLeague);
        dbRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Configurar Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar Navigation Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar Bottom Navigation
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

        // Botón para unirse a la liga
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

                            dbRef.child("ligas").child(ligaKey).child("jugadores").child(uid).setValue(true)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DatabaseReference usuarioRef = dbRef.child("usuarios").child(uid);
                                            usuarioRef.child("ligaId").setValue(codigoLiga)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            usuarioRef.child("puntos").setValue(0);
                                                            usuarioRef.child("monedas").setValue(1000)
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, PerfilUsuarioActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
