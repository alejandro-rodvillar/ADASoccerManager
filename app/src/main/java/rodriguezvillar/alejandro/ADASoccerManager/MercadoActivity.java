package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MercadoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView recyclerViewMercado;
    private JugadorAdapter adapter;
    private List<Jugador> listaJugadores = new ArrayList<>();
    private List<Jugador> listaAleatoria = new ArrayList<>();
    private DatabaseReference dbRef;
    private DatabaseReference mercadoRef; // referencia a nodo mercado
    private TextView textViewTiempoRestante;

    private Handler handler = new Handler();
    private Runnable updateTimerRunnable;

    // Cambiado a 2 minutos (120.000 ms)
    private static final long TIEMPO_ESPERA_MS = 2 * 60 * 1000; // 2 minutos

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
        adapter = new JugadorAdapter(listaAleatoria, true);
        recyclerViewMercado.setAdapter(adapter);

        mercadoRef = FirebaseDatabase.getInstance().getReference("mercado");

        // Asegurar que el nodo ultimaActualizacion existe, y luego continuar
        inicializarUltimaActualizacion(() -> {
            // Ahora que ultimaActualizacion existe, procedemos con la carga
            mercadoRef.child("ultimaActualizacion").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long ultimaActualizacion = 0;
                    if (snapshot.exists()) {
                        ultimaActualizacion = snapshot.getValue(Long.class);
                    }
                    long ahora = System.currentTimeMillis();

                    if (ahora - ultimaActualizacion >= TIEMPO_ESPERA_MS) {
                        actualizarMercadoYTimestamp(ahora);
                    } else {
                        cargarJugadoresDesdeCache();
                    }

                    actualizarTiempoRestante(ultimaActualizacion);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MercadoActivity.this, "Error al obtener la última actualización", Toast.LENGTH_SHORT).show();
                    cargarJugadoresDesdeCache();
                }
            });
        });
    }

    // Método que crea el nodo mercado/ultimaActualizacion si no existe
    private void inicializarUltimaActualizacion(Runnable onComplete) {
        mercadoRef.child("ultimaActualizacion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    mercadoRef.child("ultimaActualizacion").setValue(System.currentTimeMillis())
                            .addOnCompleteListener(task -> {
                                onComplete.run();
                            });
                } else {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

    private void actualizarMercadoYTimestamp(long timestamp) {
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadores.clear();
                listaAleatoria.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null && jugador.getNombre() != null) {
                        jugador.setId(ds.getKey());
                        listaJugadores.add(jugador);
                        if ("en venta".equals(jugador.getEstado())) {
                            listaAleatoria.add(jugador);
                        }
                    }
                }
                adapter.notifyDataSetChanged();

                // Actualizamos el timestamp global en mercado
                mercadoRef.child("ultimaActualizacion").setValue(timestamp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MercadoActivity.this, "Error al cargar jugadores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void comprarJugador(final Jugador jugadorComprado) {
        if (jugadorComprado == null || jugadorComprado.getId() == null) return;

        jugadorComprado.setEstado("propiedad");

        dbRef.child(jugadorComprado.getId()).setValue(jugadorComprado)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MercadoActivity.this, "Jugador comprado con éxito", Toast.LENGTH_SHORT).show();
                        listaAleatoria.remove(jugadorComprado);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MercadoActivity.this, "Error al comprar jugador", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarJugadoresDesdeCache() {
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAleatoria.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null && "en venta".equals(jugador.getEstado())) {
                        jugador.setId(ds.getKey());
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

    private void actualizarTiempoRestante(long ultimaActualizacion) {
        updateTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long ahora = System.currentTimeMillis();
                long siguienteActualizacion = ultimaActualizacion + TIEMPO_ESPERA_MS;
                long restante = siguienteActualizacion - ahora;

                if (restante <= 0) {
                    // Actualizar mercado y timestamp
                    actualizarMercadoYTimestamp(System.currentTimeMillis());
                    actualizarTiempoRestante(System.currentTimeMillis());
                } else {
                    long segundos = (restante / 1000) % 60;
                    long minutos = (restante / 1000) / 60;
                    String tiempoFormateado = String.format("Próxima actualización en: %02d:%02ds", minutos, segundos);
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
