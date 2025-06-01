package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

public class CrearLigaActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;
    private EditText etLeagueName, etParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_liga);

        etLeagueName = findViewById(R.id.etLeagueName);
        etParticipants = findViewById(R.id.etParticipants);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearLiga();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(CrearLigaActivity.this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(CrearLigaActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(CrearLigaActivity.this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(CrearLigaActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(CrearLigaActivity.this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(CrearLigaActivity.this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(CrearLigaActivity.this, MercadoActivity.class));
                return true;
            }
            return false;
        });
    }

    private void crearLiga() {
        String leagueName = etLeagueName.getText().toString().trim();
        String participantsStr = etParticipants.getText().toString().trim();

        if (leagueName.isEmpty()) {
            etLeagueName.setError("Ingresa el nombre de la liga");
            etLeagueName.requestFocus();
            return;
        }

        if (participantsStr.isEmpty()) {
            etParticipants.setError("Ingresa el número de participantes");
            etParticipants.requestFocus();
            return;
        }

        int participants;
        try {
            participants = Integer.parseInt(participantsStr);
        } catch (NumberFormatException e) {
            etParticipants.setError("Debe ser un número");
            return;
        }

        if (participants < 2 || participants > 5) {
            etParticipants.setError("Debe ser entre 2 y 5 participantes");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Comprobamos si el usuario ya tiene una liga
        databaseRef.child("usuarios").child(uid).child("ligaId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(CrearLigaActivity.this, "Ya perteneces a una liga. Solo puedes unirte o crear una.", Toast.LENGTH_LONG).show();
                } else {
                    String ligaId = UUID.randomUUID().toString().substring(0, 8);

                    HashMap<String, Object> ligaData = new HashMap<>();
                    ligaData.put("nombre", leagueName);
                    ligaData.put("maxParticipantes", participants);
                    ligaData.put("creador", uid);
                    HashMap<String, Boolean> jugadores = new HashMap<>();
                    jugadores.put(uid, true);
                    ligaData.put("jugadores", jugadores);

                    databaseRef.child("ligas").child(ligaId).setValue(ligaData);
                    databaseRef.child("usuarios").child(uid).child("ligaId").setValue(ligaId);

                    Toast.makeText(CrearLigaActivity.this, "Liga creada con éxito. Código: " + ligaId, Toast.LENGTH_LONG).show();

                    etLeagueName.setText("");
                    etParticipants.setText("");

                    startActivity(new Intent(CrearLigaActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CrearLigaActivity.this, "Error al comprobar tu liga: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
