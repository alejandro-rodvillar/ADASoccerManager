package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;

public class LigasActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    // Datos ficticios de los equipos (usuario y puntos)
    String[] leagueTeams = {
            "1. JaviCruz - 58 Puntos",
            "2. Luisito87 - 54 Puntos",
            "3. MikelBest - 50 Puntos",
            "4. PepeFutbol - 48 Puntos",
            "5. SergioR_90 - 45 Puntos",
            "6. AnaChampions - 42 Puntos",
            "7. Juanito10 - 40 Puntos",
            "8. MartaFutbol - 38 Puntos",
            "9. DavidKing - 35 Puntos",
            "10. TaniaSoccer - 30 Puntos"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ligas);

        // Configuración del ListView
        ListView leagueListView = findViewById(R.id.leagueListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leagueTeams);
        leagueListView.setAdapter(adapter);

        // Configuración del clic en los equipos de la liga
        leagueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Aquí puedes manejar la acción cuando se pulse en un equipo
                String selectedTeam = (String) parent.getItemAtPosition(position);
                // Mostrar un mensaje o hacer alguna acción con el equipo seleccionado
            }
        });

        // Configurar Toolbar y el botón del menú
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Configurar el botón de menú (☰)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Resaltar el ítem de Ligas en el BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.nav_leagues);

        // Manejar clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(LigasActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(LigasActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Aquí se inicia la actividad Login cuando se cierra sesión
                    Intent intent = new Intent(LigasActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Cierra esta actividad para que el usuario no pueda regresar
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
                    startActivity(new Intent(LigasActivity.this, MainActivity.class));
                    return true; // Indica que el item fue manejado
                } else if (id == R.id.nav_leagues) {
                    Toast.makeText(LigasActivity.this, "Ya estás en Ligas", Toast.LENGTH_SHORT).show();
                    return true; // Ya estás en la pantalla de ligas
                } else if (id == R.id.nav_my_team) {
                    startActivity(new Intent(LigasActivity.this, EquipoActivity.class));
                    return true;
                } else if (id == R.id.nav_market) {
                    startActivity(new Intent(LigasActivity.this, MercadoActivity.class));
                    return true;
                }

                return false; // Indica que el item no fue manejado
            }
        });
    }
}




