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
import java.util.Collections;
import java.util.List;

public class MercadoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView recyclerViewMercado;
    private JugadorAdapter adapter;
    private List<Jugador> listaAleatoria = new ArrayList<>();
    private DatabaseReference dbRef;
    private DatabaseReference mercadoRef;
    private TextView textViewTiempoRestante;

    private Handler handler = new Handler();
    private Runnable updateTimerRunnable;

    private static final long TIEMPO_ESPERA_MS = 2 * 60 * 1000; // 2 minutos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mercado);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        recyclerViewMercado = findViewById(R.id.recyclerViewMercado);
        textViewTiempoRestante = findViewById(R.id.textViewTiempoRestante);

        recyclerViewMercado.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JugadorAdapter(listaAleatoria, true);
        recyclerViewMercado.setAdapter(adapter);

        mercadoRef = FirebaseDatabase.getInstance().getReference("mercado");

        inicializarUltimaActualizacion(() -> {
            mercadoRef.child("ultimaActualizacion").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long ultimaActualizacion = 0;
                    if (snapshot.exists()) {
                        ultimaActualizacion = snapshot.getValue(Long.class);
                    }
                    actualizarTiempoRestante(ultimaActualizacion);
                    cargarJugadoresDesdeCache();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MercadoActivity.this, "Error al obtener la última actualización", Toast.LENGTH_SHORT).show();
                    cargarJugadoresDesdeCache();
                }
            });
        });
    }

    private void inicializarUltimaActualizacion(Runnable onComplete) {
        mercadoRef.child("ultimaActualizacion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    mercadoRef.child("ultimaActualizacion").setValue(System.currentTimeMillis())
                            .addOnCompleteListener(task -> onComplete.run());
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
                List<Jugador> disponibles = new ArrayList<>();
                listaAleatoria.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null && jugador.getNombre() != null) {
                        jugador.setId(ds.getKey());

                        if ("en venta".equals(jugador.getEstado())) {
                            jugador.setEstado("disponible");
                            dbRef.child(jugador.getId()).setValue(jugador);
                        }

                        if ("disponible".equals(jugador.getEstado())) {
                            disponibles.add(jugador);
                        }
                    }
                }

                Collections.shuffle(disponibles);
                List<Jugador> seleccionados = disponibles.subList(0, Math.min(5, disponibles.size()));

                List<String> idsEnVenta = new ArrayList<>();
                for (Jugador jugador : seleccionados) {
                    jugador.setEstado("en venta");
                    dbRef.child(jugador.getId()).setValue(jugador);
                    idsEnVenta.add(jugador.getId());
                }

                // Guardar timestamp y lista de jugadores en venta en mercado
                mercadoRef.child("ultimaActualizacion").setValue(timestamp);
                mercadoRef.child("jugadoresEnVenta").setValue(idsEnVenta).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cargarJugadoresDesdeCache(); // Refrescar inmediatamente
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MercadoActivity.this, "Error al actualizar mercado", Toast.LENGTH_SHORT).show();
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
        mercadoRef.child("jugadoresEnVenta").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> idsEnVenta = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        idsEnVenta.add(ds.getValue(String.class));
                    }
                }
                if (idsEnVenta.isEmpty()) {
                    listaAleatoria.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotJugadores) {
                        listaAleatoria.clear();
                        for (String id : idsEnVenta) {
                            DataSnapshot jugadorSnap = snapshotJugadores.child(id);
                            Jugador jugador = jugadorSnap.getValue(Jugador.class);
                            if (jugador != null) {
                                jugador.setId(id);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MercadoActivity.this, "Error al cargar jugadores en caché", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarTiempoRestante(long ultimaActualizacionLocal) {
        updateTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long ahora = System.currentTimeMillis();
                long siguienteActualizacion = ultimaActualizacionLocal + TIEMPO_ESPERA_MS;
                long restante = siguienteActualizacion - ahora;

                if (restante <= 0) {
                    mercadoRef.child("ultimaActualizacion").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long ultimaActualizacionReal = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
                            if (ultimaActualizacionReal <= ultimaActualizacionLocal) {
                                long nuevoTimestamp = ahora;
                                actualizarMercadoYTimestamp(nuevoTimestamp);
                                actualizarTiempoRestante(nuevoTimestamp);
                            } else {
                                actualizarTiempoRestante(ultimaActualizacionReal);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            actualizarTiempoRestante(ultimaActualizacionLocal);
                        }
                    });
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
