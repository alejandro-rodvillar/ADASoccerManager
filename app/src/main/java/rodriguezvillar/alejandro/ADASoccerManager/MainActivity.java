package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private DatabaseReference mDatabase;
    private static final String TAG = "FirebaseData";

    private void mostrarTodosLosDatos() {
        mDatabase.child("jugadores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "========== TODOS LOS JUGADORES ==========");

                for (DataSnapshot jugadorSnapshot : dataSnapshot.getChildren()) {
                    String key = jugadorSnapshot.getKey();

                    String nombre = jugadorSnapshot.child("nombre").getValue(String.class);
                    String posicion = jugadorSnapshot.child("posicion").getValue(String.class);
                    String equipo = jugadorSnapshot.child("equipo").getValue(String.class);
                    Long precio = jugadorSnapshot.child("precio").getValue(Long.class);
                    Long puntos = jugadorSnapshot.child("puntos").getValue(Long.class);
                    String estado = jugadorSnapshot.child("estado").getValue(String.class);

                    Log.d(TAG, "ID: " + key);
                    Log.d(TAG, "Nombre: " + nombre);
                    Log.d(TAG, "Posición: " + posicion);
                    Log.d(TAG, "Equipo: " + equipo);
                    Log.d(TAG, "Precio: " + precio);
                    Log.d(TAG, "Puntos: " + puntos);
                    Log.d(TAG, "Estado: " + estado);
                    Log.d(TAG, "----------------------------------------");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al leer datos: " + databaseError.getMessage());
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


        Log.d("HOLA", "hola");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mostrarTodosLosDatos();

        // Botón para crear una nueva liga
        findViewById(R.id.btnCreateLeague).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CrearLigaActivity.class));
            }
        });


        // Se configura el Toolbar y botón de menú
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Se configura el botón de menú (las tres rayas)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Controles de los clics en el menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, PerfilUsuarioActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Borrar preferencias de login
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear(); // Elimina los datos de login persistente
                    editor.apply();

                    // Cerrar sesión de Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Redirigir a Login y cerrar la actividad actual
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Configuración de navegación en BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Se muestra un Toast si ya estás en la pantalla de inicio
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
            }
        });
    }
}
