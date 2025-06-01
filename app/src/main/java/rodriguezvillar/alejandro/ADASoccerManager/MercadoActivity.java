package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Arrays;
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
    private TextView textViewTiempoRestante;

    private Handler handler = new Handler();
    private Runnable updateTimerRunnable;

    // SharedPreferences
    private static final String PREFS_NAME = "MercadoPrefs";
    private static final String KEY_JUGADORES = "JugadoresMercado";
    private static final String KEY_TIMESTAMP = "UltimaActualizacion";
    private static final long TIEMPO_ESPERA_MS = 24 * 60 * 60 * 1000; // 24 horas

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

        // VIEWS
        recyclerViewMercado = findViewById(R.id.recyclerViewMercado);
        textViewTiempoRestante = findViewById(R.id.textViewTiempoRestante);

        recyclerViewMercado.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JugadorAdapter(listaAleatoria);
        recyclerViewMercado.setAdapter(adapter);

        // COMPROBAR SI SE DEBE CARGAR NUEVA LISTA
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long ultimaActualizacion = prefs.getLong(KEY_TIMESTAMP, 0);
        long ahora = System.currentTimeMillis();

        if (ahora - ultimaActualizacion >= TIEMPO_ESPERA_MS) {
            cargarJugadoresDeFirebase();
        } else {
            cargarJugadoresDesdeCache();
        }

        actualizarTiempoRestante();
    }

    private void cargarJugadoresDeFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadores.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null && jugador.getNombre() != null) {
                        listaJugadores.add(jugador);
                    }
                }

                List<Jugador> jugadoresValidos = new ArrayList<>();
                for (Jugador j : listaJugadores) {
                    if (j.getNombre() != null) {
                        jugadoresValidos.add(j);
                    }
                }
                Collections.sort(jugadoresValidos, (j1, j2) ->
                        j1.getNombre().toLowerCase().compareTo(j2.getNombre().toLowerCase()));

                listaJugadores.clear();
                listaJugadores.addAll(jugadoresValidos);

                elegirJugadoresAleatorios();

                // Guardar en cache
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                StringBuilder sb = new StringBuilder();
                for (Jugador j : listaAleatoria) {
                    sb.append(j.getNombre()).append(",");
                }
                editor.putString(KEY_JUGADORES, sb.toString());
                editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
                editor.apply();
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

    private void cargarJugadoresDesdeCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jugadoresNombres = prefs.getString(KEY_JUGADORES, "");
        if (jugadoresNombres.isEmpty()) return;

        String[] nombres = jugadoresNombres.split(",");
        List<String> nombresLista = Arrays.asList(nombres);

        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAleatoria.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null && nombresLista.contains(jugador.getNombre())) {
                        listaAleatoria.add(jugador);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MercadoActivity.this, "Error al cargar jugadores en caché", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarTiempoRestante() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long ultimaActualizacion = prefs.getLong(KEY_TIMESTAMP, 0);
        long siguienteActualizacion = ultimaActualizacion + TIEMPO_ESPERA_MS;

        updateTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long ahora = System.currentTimeMillis();
                long restante = siguienteActualizacion - ahora;

                if (restante <= 0) {
                    textViewTiempoRestante.setText("¡Mercado actualizado!");
                } else {
                    long horas = restante / (1000 * 60 * 60);
                    long minutos = (restante / (1000 * 60)) % 60;
                    long segundos = (restante / 1000) % 60;

                    String tiempoFormateado = String.format("Próxima actualización en: %02dh %02dm %02ds", horas, minutos, segundos);
                    textViewTiempoRestante.setText(tiempoFormateado);

                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.post(updateTimerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimerRunnable);
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
