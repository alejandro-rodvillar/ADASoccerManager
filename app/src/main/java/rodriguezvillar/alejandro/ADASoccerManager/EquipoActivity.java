package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class EquipoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipo);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawerLayout); // Asegúrate de que el ID coincida
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Configurar ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Manejar clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(EquipoActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(EquipoActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Aquí se inicia la actividad Login cuando se cierra sesión
                    Intent intent = new Intent(EquipoActivity.this, LoginActivity.class);
                    startActivity(intent);
                    //finish(); // Cierra esta actividad para que el usuario no pueda regresar
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_team); // Resaltar el botón "Mi Equipo"

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(EquipoActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_leagues) {
                    startActivity(new Intent(EquipoActivity.this, LigasActivity.class));
                    return true;
                } else if (id == R.id.nav_my_team) {
                    Toast.makeText(EquipoActivity.this, "Ya estás en Mi Equipo", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_market) {
                    startActivity(new Intent(EquipoActivity.this, MercadoActivity.class));
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState(); // Sincronizar el estado del toggle
    }
}
