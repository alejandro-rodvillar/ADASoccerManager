package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class CrearLigaActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_liga); // Carga el layout de la actividad

        // Referencia a los campos de entrada
        EditText etLeagueName = findViewById(R.id.etLeagueName);
        EditText etParticipants = findViewById(R.id.etParticipants);

        // Botón para crear una nueva liga
        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los campos de entrada
                String leagueName = etLeagueName.getText().toString().trim();
                String participants = etParticipants.getText().toString().trim();

                // Validar los campos
                if (leagueName.isEmpty()) {
                    etLeagueName.setError("Ingresa el nombre de la liga");
                    etLeagueName.requestFocus();
                    return;
                }

                if (participants.isEmpty()) {
                    etParticipants.setError("Ingresa el número de participantes");
                    etParticipants.requestFocus();
                    return;
                }

                // Si ambos campos están llenos, proceder a crear la liga
                Toast.makeText(CrearLigaActivity.this, "Liga " + leagueName + " creada", Toast.LENGTH_SHORT).show();

                // Limpiar los campos después de crear la liga
                etLeagueName.setText("");
                etParticipants.setText("");
            }
        });

        // Configurar Toolbar y el botón de menú (☰)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar el DrawerLayout para el menú lateral
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Configurar el botón de menú lateral (hamburguesa)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Manejar clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(CrearLigaActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(CrearLigaActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Inicia la actividad Login al cerrar sesión
                    startActivity(new Intent(CrearLigaActivity.this, LoginActivity.class));
                    finish(); // Cierra la actividad actual para que el usuario no pueda regresar
                }

                drawerLayout.closeDrawers(); // Cierra el menú después de seleccionar una opción
                return true;
            }
        });

        // Configurar navegación en BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Ya estás en la pantalla de inicio
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
            }
        });
    }
}
