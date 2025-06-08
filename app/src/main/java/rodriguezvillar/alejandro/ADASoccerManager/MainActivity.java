package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private DatabaseReference mDatabase;
    private static final String TAG = "FirebaseData";

    // Comprobación de si el usuario pertenece a una liga
    private void comprobarSiPerteneceALiga(Button btnManageLeague, Button btnJoinLeague) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            btnManageLeague.setVisibility(View.GONE);
            btnJoinLeague.setVisibility(View.VISIBLE);
            return;
        }

        String uid = currentUser.getUid();

        mDatabase.child("ligas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean pertenece = false;

                for (DataSnapshot ligaSnapshot : snapshot.getChildren()) {
                    DataSnapshot jugadoresSnapshot = ligaSnapshot.child("jugadores");

                    if (jugadoresSnapshot.hasChild(uid)) {
                        Boolean perteneceBool = jugadoresSnapshot.child(uid).getValue(Boolean.class);
                        if (perteneceBool != null && perteneceBool) {
                            pertenece = true;
                            break;
                        }
                    }
                }

                if (pertenece) {
                    Log.d(TAG, "Usuario pertenece a una liga.");
                    btnManageLeague.setVisibility(View.VISIBLE);
                    btnJoinLeague.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Usuario NO pertenece a una liga.");
                    btnManageLeague.setVisibility(View.GONE);
                    btnJoinLeague.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al comprobar liga: " + error.getMessage());
                btnManageLeague.setVisibility(View.GONE);
                btnJoinLeague.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Botón crear liga
        findViewById(R.id.btnCreateLeague).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CrearLigaActivity.class));
        });

        // Botón gestionar liga
        Button btnManageLeague = findViewById(R.id.btnManageLeague);
        btnManageLeague.setVisibility(View.GONE);
        btnManageLeague.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GestionLigaActivity.class));
        });

        // Botón unirse a liga
        Button btnJoinLeague = findViewById(R.id.btnJoinLeague);
        btnJoinLeague.setVisibility(View.GONE);
        btnJoinLeague.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UnirseLiga.class));
        });

        // Mostrar botón correspondiente según estado del usuario
        comprobarSiPerteneceALiga(btnManageLeague, btnJoinLeague);

        // Menú superior
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Toggle del menú lateral
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Acciones menú lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Menú inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(MainActivity.this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show();
                return true;
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
            return false;
        });
    }
}
