package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MercadoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private RecyclerView recyclerViewMercado;
    private JugadorAdapter adapter;
    private List<Jugador> listaJugadores = new ArrayList<>();
    private List<Jugador> listaAleatoria = new ArrayList<>();

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mercado);

        // TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DRAWER + TOGGLE
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(MercadoActivity.this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MercadoActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(MercadoActivity.this, LoginActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // BOTTOM NAVIGATION
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_market);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                Toast.makeText(this, "Ya estás en Mercado", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // CONFIGURAR RECYCLERVIEW
        recyclerViewMercado = findViewById(R.id.recyclerViewMercado);
        recyclerViewMercado.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JugadorAdapter(listaAleatoria);
        recyclerViewMercado.setAdapter(adapter);

        // CARGAR JUGADORES
        cargarJugadoresDeFirebase();
    }

    private void cargarJugadoresDeFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadores.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null) {
                        listaJugadores.add(jugador);
                    }
                }
                Collections.sort(listaJugadores, (j1, j2) ->
                        j1.getNombre().toLowerCase().compareTo(j2.getNombre().toLowerCase()));
                elegirJugadoresAleatorios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MercadoActivity.this, "Error al cargar jugadores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void elegirJugadoresAleatorios() {
        listaAleatoria.clear();

        if (listaJugadores.size() <= 5) {
            listaAleatoria.addAll(listaJugadores);
        } else {
            Random random = new Random();
            List<Integer> indicesUsados = new ArrayList<>();
            while (listaAleatoria.size() < 5) {
                int index = random.nextInt(listaJugadores.size());
                if (!indicesUsados.contains(index)) {
                    indicesUsados.add(index);
                    listaAleatoria.add(listaJugadores.get(index));
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, ListaJugadoresFirebaseActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
