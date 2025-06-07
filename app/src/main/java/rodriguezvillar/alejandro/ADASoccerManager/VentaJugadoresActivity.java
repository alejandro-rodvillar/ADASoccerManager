package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class VentaJugadoresActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_jugadores);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer Layout & NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(VentaJugadoresActivity.this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(VentaJugadoresActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_team);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(VentaJugadoresActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(VentaJugadoresActivity.this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(VentaJugadoresActivity.this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(VentaJugadoresActivity.this, MercadoActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }
}
