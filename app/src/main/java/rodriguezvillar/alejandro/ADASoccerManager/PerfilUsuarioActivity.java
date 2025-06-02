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
    private Button buttonGuardarNombre, buttonCambiarContrasena;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean estaEditando = false;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        textViewNombreUsuario = findViewById(R.id.textViewNombreUsuario);
        textViewCorreo = findViewById(R.id.textViewCorreo);
        textViewNombreGuardado = findViewById(R.id.textViewNombreGuardado);
        editTextNombre = findViewById(R.id.editTextNombre);
        buttonGuardarNombre = findViewById(R.id.buttonGuardarNombre);
        buttonCambiarContrasena = findViewById(R.id.buttonCambiarContrasena);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            textViewCorreo.setText("Correo: " + currentUser.getEmail());

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

        String keyNombreUsuario = currentUser != null ? "nombreGuardado_" + currentUser.getUid() : null;
        String nombreGuardadoLocal = "";
        if (keyNombreUsuario != null) {
            nombreGuardadoLocal = sharedPreferences.getString(keyNombreUsuario, "");
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

                if (keyNombreUsuario != null) {
                    sharedPreferences.edit().putString(keyNombreUsuario, nombreNuevo).apply();
                }

                textViewNombreGuardado.setText(nombreNuevo);
                editTextNombre.setEnabled(false);
                buttonGuardarNombre.setText("Editar");
                estaEditando = false;

                Toast.makeText(PerfilUsuarioActivity.this, "Nombre guardado", Toast.LENGTH_SHORT).show();
            } else {
                editTextNombre.setEnabled(true);
                editTextNombre.requestFocus();
                buttonGuardarNombre.setText("Guardar");
                estaEditando = true;
            }
        });

        buttonCambiarContrasena.setOnClickListener(v -> {
            if (currentUser != null) {
                String email = currentUser.getEmail();
                if (email != null && !email.isEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Correo de cambio de contraseña enviado a " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Error al enviar el correo de cambio de contraseña", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Correo de usuario no disponible", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
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

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                Toast.makeText(this, "Ya estás en Perfil", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }
}
