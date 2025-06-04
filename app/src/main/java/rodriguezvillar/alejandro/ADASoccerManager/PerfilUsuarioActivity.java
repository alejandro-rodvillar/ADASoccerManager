package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class PerfilUsuarioActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PerfilPrefs";

    private TextView textViewNombreUsuario, textViewCorreo, textViewNombreGuardado;
    private EditText editTextNombre;
    private Button buttonGuardarNombre, buttonCambiarContrasena;

    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    private boolean estaEditando = false;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        // Referencias UI
        textViewNombreUsuario = findViewById(R.id.textViewNombreUsuario);
        textViewCorreo = findViewById(R.id.textViewCorreo);
        textViewNombreGuardado = findViewById(R.id.textViewNombreGuardado);

        editTextNombre = findViewById(R.id.editTextNombre);
        buttonGuardarNombre = findViewById(R.id.buttonGuardarNombre);
        buttonCambiarContrasena = findViewById(R.id.buttonCambiarContrasena);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (currentUser != null) {
            textViewCorreo.setText("Correo: " + currentUser.getEmail());

            // Obtener nombre de usuario desde Firebase Realtime Database
            DatabaseReference usuarioRef = databaseReference.child("usuarios")
                    .child(currentUser.getUid())
                    .child("nombre");

            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String nombreRegistrado = snapshot.getValue(String.class);
                    if (nombreRegistrado != null && !nombreRegistrado.isEmpty()) {
                        textViewNombreUsuario.setText("Nombre de usuario: " + nombreRegistrado);
                    } else {
                        textViewNombreUsuario.setText("Nombre de usuario: no disponible");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(PerfilUsuarioActivity.this, "Error al obtener nombre", Toast.LENGTH_SHORT).show();
                    textViewNombreUsuario.setText("Nombre de usuario: no disponible");
                }
            });
        } else {
            textViewCorreo.setText("Correo: no disponible");
            textViewNombreUsuario.setText("Nombre de usuario: no disponible");
        }

        // Clave para almacenar nombre en SharedPreferences, por usuario
        String keyNombreLocal = currentUser != null ? "nombreLocal_" + currentUser.getUid() : null;

        String nombreGuardadoLocal = "";
        if (keyNombreLocal != null) {
            nombreGuardadoLocal = sharedPreferences.getString(keyNombreLocal, "");
        }
        textViewNombreGuardado.setText(nombreGuardadoLocal);
        editTextNombre.setText(nombreGuardadoLocal);

        if (nombreGuardadoLocal.isEmpty()) {
            editTextNombre.setEnabled(true);
            buttonGuardarNombre.setText("Guardar");
            estaEditando = true;
        } else {
            editTextNombre.setEnabled(false);
            buttonGuardarNombre.setText("Editar");
            estaEditando = false;
        }

        setupUI();

        buttonGuardarNombre.setOnClickListener(v -> {
            if (estaEditando) {
                String nombreNuevo = editTextNombre.getText().toString().trim();

                if (nombreNuevo.isEmpty()) {
                    editTextNombre.setError("Introduce un nombre");
                    return;
                }
                if (!nombreNuevo.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                    editTextNombre.setError("Solo se permiten letras y espacios");
                    return;
                }

                if (keyNombreLocal != null) {
                    sharedPreferences.edit().putString(keyNombreLocal, nombreNuevo).apply();
                }

                // Aquí NO se actualiza Firebase, solo se guarda localmente y se actualiza UI

                textViewNombreGuardado.setText(nombreNuevo);
                editTextNombre.setEnabled(false);
                buttonGuardarNombre.setText("Editar");
                estaEditando = false;

            } else {
                editTextNombre.setEnabled(true);
                editTextNombre.requestFocus();
                buttonGuardarNombre.setText("Guardar");
                estaEditando = true;
            }
        });

        // Botón cambiar contraseña: enviar email restablecimiento
        buttonCambiarContrasena.setOnClickListener(v -> {
            if (currentUser != null) {
                String email = currentUser.getEmail();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PerfilUsuarioActivity.this,
                                        "Email de restablecimiento enviado a " + email,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PerfilUsuarioActivity.this,
                                        "Error al enviar email de restablecimiento",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(PerfilUsuarioActivity.this,
                        "Usuario no autenticado",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(PerfilUsuarioActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(PerfilUsuarioActivity.this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(PerfilUsuarioActivity.this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(PerfilUsuarioActivity.this, MercadoActivity.class));
                return true;
            }
            return false;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                Toast.makeText(PerfilUsuarioActivity.this, "Ya estás en Perfil", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(PerfilUsuarioActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(PerfilUsuarioActivity.this, LoginActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }
}
