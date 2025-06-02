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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PerfilPrefs";

    private TextView textViewNombreUsuario, textViewCorreo, textViewNombreGuardado;
    private EditText editTextNombre;
    private Button buttonGuardarNombre;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean estaEditando = false; // Empieza bloqueado, botón dice "Editar"

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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            textViewCorreo.setText("Correo: " + currentUser.getEmail());

            // Mostrar nombre de usuario desde Firestore para referencia (no se actualiza)
            db.collection("usuarios").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String nombreRegistrado = document.getString("nombre");
                                if (nombreRegistrado != null && !nombreRegistrado.isEmpty()) {
                                    textViewNombreUsuario.setText("Nombre de usuario: " + nombreRegistrado);
                                } else {
                                    textViewNombreUsuario.setText("Nombre de usuario: no disponible");
                                }
                            } else {
                                textViewNombreUsuario.setText("Nombre de usuario: no disponible");
                            }
                        } else {
                            Toast.makeText(PerfilUsuarioActivity.this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
                            textViewNombreUsuario.setText("Nombre de usuario: no disponible");
                        }
                    });
        } else {
            textViewCorreo.setText("Correo: no disponible");
            textViewNombreUsuario.setText("Nombre de usuario: no disponible");
        }

        // Crear una key única para cada usuario para guardar su nombre localmente
        String keyNombreUsuario = currentUser != null ? "nombreGuardado_" + currentUser.getUid() : null;

        // Cargar el nombre guardado localmente para este usuario
        String nombreGuardadoLocal = "";
        if (keyNombreUsuario != null) {
            nombreGuardadoLocal = sharedPreferences.getString(keyNombreUsuario, "");
        }
        textViewNombreGuardado.setText(nombreGuardadoLocal);
        editTextNombre.setText(nombreGuardadoLocal);

        // Estado inicial según si hay nombre guardado o no
        if (nombreGuardadoLocal.isEmpty()) {
            // No hay nombre guardado, permitir editar y botón "Guardar"
            editTextNombre.setEnabled(true);
            buttonGuardarNombre.setText("Guardar");
            estaEditando = true;
        } else {
            // Hay nombre guardado, bloqueado y botón "Editar"
            editTextNombre.setEnabled(false);
            buttonGuardarNombre.setText("Editar");
            estaEditando = false;
        }

        setupUI();

        buttonGuardarNombre.setOnClickListener(v -> {
            if (estaEditando) {
                // Guardar nombre en SharedPreferences
                String nombreNuevo = editTextNombre.getText().toString().trim();

                if (nombreNuevo.isEmpty()) {
                    editTextNombre.setError("Introduce un nombre");
                    return;
                }
                if (!nombreNuevo.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                    editTextNombre.setError("Solo se permiten letras y espacios");
                    return;
                }

                if (keyNombreUsuario != null) {
                    // Guardar en SharedPreferences usando la key del usuario actual
                    sharedPreferences.edit().putString(keyNombreUsuario, nombreNuevo).apply();
                }

                // Mostrar en UI
                textViewNombreGuardado.setText(nombreNuevo);

                // Bloquear edición y cambiar texto botón
                editTextNombre.setEnabled(false);
                buttonGuardarNombre.setText("Editar");
                estaEditando = false;

                Toast.makeText(PerfilUsuarioActivity.this, "Nombre guardado", Toast.LENGTH_SHORT).show();
            } else {
                // Permitir edición
                editTextNombre.setEnabled(true);
                editTextNombre.requestFocus();

                // Cambiar botón a "Guardar"
                buttonGuardarNombre.setText("Guardar");
                estaEditando = true;
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
