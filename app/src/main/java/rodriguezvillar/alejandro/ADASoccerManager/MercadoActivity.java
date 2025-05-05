package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

public class MercadoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mercado);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
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
                    startActivity(new Intent(MercadoActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(MercadoActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Aquí se inicia la actividad Login cuando se cierra sesión
                    Intent intent = new Intent(MercadoActivity.this, LoginActivity.class);
                    startActivity(intent);
                    //finish(); // Cierra esta actividad para que el usuario no pueda regresar
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Aquí cambiamos a resaltar el ítem correcto para la actividad del mercado
        bottomNavigationView.setSelectedItemId(R.id.nav_market); // Resaltar el botón "Mercado"

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(MercadoActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_leagues) {
                    startActivity(new Intent(MercadoActivity.this, LigasActivity.class));
                    return true;
                } else if (id == R.id.nav_my_team) {
                    startActivity(new Intent(MercadoActivity.this, EquipoActivity.class));
                    return true;
                } else if (id == R.id.nav_market) {
                    // Muestra un Toast si ya estás en la pantalla del mercado
                    Toast.makeText(MercadoActivity.this, "Ya estás en Mercado", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        });
    }
}
