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

public class PerfilUsuarioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Desmarcar cualquier ítem cuando se entra en la actividad.
        //bottomNavigationView.setSelectedItemId(-1);  // Ningún ítem seleccionado al entrar

        // Establecer el listener de los ítems de navegación
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });

        // Manejar clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    Toast.makeText(PerfilUsuarioActivity.this, "Ya estás en Perfil", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(PerfilUsuarioActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Aquí se inicia la actividad Login cuando se cierra sesión
                    Intent intent = new Intent(PerfilUsuarioActivity.this, LoginActivity.class);
                    startActivity(intent);
                    //finish(); // Cierra esta actividad para que el usuario no pueda regresar
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}
