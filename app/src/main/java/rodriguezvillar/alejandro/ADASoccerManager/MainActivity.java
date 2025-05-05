package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegúrate de que esta es la actividad principal

        // Botón para crear una nueva liga
        findViewById(R.id.btnCreateLeague).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CrearLigaActivity.class));
            }
        });


        // Configurar Toolbar y botón de menú
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Configurar botón de menú (☰)
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
                    startActivity(new Intent(MainActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Aquí se inicia la actividad Login cuando se cierra sesión
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    //finish(); // Cierra esta actividad para que el usuario no pueda regresar
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Configurar navegación en BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Mostrar un Toast si ya estás en la pantalla de inicio
                    Toast.makeText(MainActivity.this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show();
                    return true; // Indica que el item fue manejado
                } else if (id == R.id.nav_leagues) {
                    startActivity(new Intent(MainActivity.this, LigasActivity.class));
                    return true;
                } else if (id == R.id.nav_my_team) {
                    startActivity(new Intent(MainActivity.this, EquipoActivity.class));
                    return true;
                } else if (id == R.id.nav_market) {
                    startActivity(new Intent(MainActivity.this, MercadoActivity.class));
                    return true;
                }

                return false; // Indica que el item no fue manejado
            }
        });
    }
}

