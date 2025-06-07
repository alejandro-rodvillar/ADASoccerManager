package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Map;
import java.util.UUID;

public class CrearLigaActivity extends AppCompatActivity {

    // UI y Firebase
    private DrawerLayout drawerLayout;
    private FirebaseUser currentUser;
    private EditText etLeagueName, etParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_liga);

        // Inicializa los campos de entrada de texto
        etLeagueName = findViewById(R.id.etLeagueName);
        etParticipants = findViewById(R.id.etParticipants);

        // Obtiene el usuario actual
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Asocia el botón de crear liga con la función crearLiga()
        findViewById(R.id.btnSubmit).setOnClickListener(v -> crearLiga());

        // Configura la barra superior y el menú lateral
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Acciones del menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CrearLigaActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Acciones del menú inferior
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

    // Función principal para crear una liga
    private void crearLiga() {
        String leagueName = etLeagueName.getText().toString().trim();
        String participantsStr = etParticipants.getText().toString().trim();

        // Validación del nombre
        if (leagueName.isEmpty()) {
            etLeagueName.setError("Ingresa el nombre de la liga");
            etLeagueName.requestFocus();
            return;
        }

        // Validación del número de participantes
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
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
        DatabaseReference ligasRefRoot = FirebaseDatabase.getInstance().getReference("ligas");

        // Paso 1: comprobar si ya existe alguna liga
        ligasRefRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ligasSnapshot) {
                if (ligasSnapshot.exists()) {
                    Toast.makeText(CrearLigaActivity.this, "Ya existe una liga. Solo puedes unirte mediante código.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Paso 2: comprobar si el usuario ya pertenece a una liga
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userSnapshot) {
                        if (userSnapshot.exists() && userSnapshot.hasChild("ligaId")) {
                            Toast.makeText(CrearLigaActivity.this, "Ya perteneces a una liga. No puedes crear otra.", Toast.LENGTH_LONG).show();
                        } else {
                            // crear la liga
                            String ligaId = UUID.randomUUID().toString().substring(0, 8);
                            // generar un ID aleatorio corto para teamId
                            String teamId = UUID.randomUUID().toString().substring(0, 8);
                            DatabaseReference nuevaLigaRef = ligasRefRoot.child(ligaId);

                            Map<String, Object> ligaData = new HashMap<>();
                            ligaData.put("nombre", leagueName);
                            ligaData.put("maxParticipantes", participants);
                            ligaData.put("creador", uid);
                            ligaData.put("ligaId", ligaId);

                            Map<String, Boolean> jugadores = new HashMap<>();
                            jugadores.put(uid, true);
                            ligaData.put("jugadores", jugadores);

                            nuevaLigaRef.setValue(ligaData).addOnSuccessListener(unused -> {
                                userRef.child("ligaId").setValue(ligaId);
                                userRef.child("monedas").setValue(100000);
                                userRef.child("puntos").setValue(0);
                                userRef.child("equipoUsuario").setValue(teamId);
                                Toast.makeText(CrearLigaActivity.this, "Liga creada con éxito. Código: " + ligaId, Toast.LENGTH_LONG).show();
                                etLeagueName.setText("");
                                etParticipants.setText("");
                                startActivity(new Intent(CrearLigaActivity.this, MainActivity.class));
                                finish();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(CrearLigaActivity.this, "Error al crear la liga: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(CrearLigaActivity.this, "Error al comprobar usuario: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CrearLigaActivity.this, "Error al verificar ligas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
