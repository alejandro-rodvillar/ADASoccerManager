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

        // Se configura el ListView
        ListView leagueListView = findViewById(R.id.leagueListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leagueTeams);
        leagueListView.setAdapter(adapter);

        // Se configura el clic en los equipos de la liga
        leagueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Cuando se pulse un equipo se puede manejar una acción
                String selectedTeam = (String) parent.getItemAtPosition(position);
            }
        });

        // Se configura el Toolbar y el botón de menú
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Se onfigura el botón de menú (las tres rayas)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Se resalta el ítem de Ligas en el BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.nav_leagues);

        // Controles de los clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(LigasActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(LigasActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Cuanndo se cierra la sesión se vuelve a la pantalla de Login
                    Intent intent = new Intent(LigasActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Se cierra la actividad actual para que el usuario no pueda regresar
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Configuración de la navegación en BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Se muestra un Toast si ya estás en la pantalla de inicio
                    startActivity(new Intent(LigasActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_leagues) {
                    Toast.makeText(LigasActivity.this, "Ya estás en Ligas", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_my_team) {
                    startActivity(new Intent(LigasActivity.this, EquipoActivity.class));
                    return true;
                } else if (id == R.id.nav_market) {
                    startActivity(new Intent(LigasActivity.this, MercadoActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
}