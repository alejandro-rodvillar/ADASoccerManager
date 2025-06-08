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
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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
    private TextView textViewMonedas;

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
            } else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MercadoActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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
        textViewMonedas = findViewById(R.id.textViewMonedas);

        recyclerViewMercado.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JugadorAdapter(listaAleatoria, R.layout.item_jugador, true, jugador -> comprarJugador(jugador));
        recyclerViewMercado.setAdapter(adapter);

        mercadoRef = FirebaseDatabase.getInstance().getReference("mercado");

        cargarMonedasUsuario();

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

    private ValueEventListener monedasListener;

    private void cargarMonedasUsuario() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        monedasListener = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("monedas")) {
                    Long monedas = snapshot.child("monedas").getValue(Long.class);
                    textViewMonedas.setText("Monedas: " + monedas);
                } else {
                    textViewMonedas.setText("Aún no perteneces a una liga");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textViewMonedas.setText("Error al cargar monedas");
            }
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

                mercadoRef.child("ultimaActualizacion").setValue(timestamp);
                mercadoRef.child("jugadoresEnVenta").setValue(idsEnVenta).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cargarJugadoresDesdeCache();
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

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long monedas = snapshot.child("monedas").getValue(Long.class);
                String nombreUsuario = snapshot.child("nombre").getValue(String.class);

                if (monedas == null || nombreUsuario == null) {
                    Toast.makeText(MercadoActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (monedas < jugadorComprado.getPrecio()) {
                    Toast.makeText(MercadoActivity.this, "No tienes monedas suficientes", Toast.LENGTH_SHORT).show();
                    return;
                }

                // CONTAR POSICIONES
                int porteros = 0, defensas = 0, centrocampistas = 0, delanteros = 0;
                DataSnapshot equipoSnapshot = snapshot.child("equipoUsuario");

                for (DataSnapshot jugadorSnap : equipoSnapshot.getChildren()) {
                    String posicion = jugadorSnap.child("posicion").getValue(String.class);
                    if (posicion == null) continue;

                    switch (posicion.toLowerCase()) {
                        case "portero":
                            porteros++;
                            break;
                        case "defensa":
                            defensas++;
                            break;
                        case "centrocampista":
                            centrocampistas++;
                            break;
                        case "delantero":
                            delanteros++;
                            break;
                    }
                }

                // VERIFICAR SI LA POSICIÓN ESTÁ COMPLETA
                String posicionJugador = jugadorComprado.getPosicion();
                if (posicionJugador != null) {
                    boolean posicionLlena =
                            (posicionJugador.equalsIgnoreCase("portero") && porteros >= 1) ||
                                    (posicionJugador.equalsIgnoreCase("defensa") && defensas >= 4) ||
                                    (posicionJugador.equalsIgnoreCase("centrocampista") && centrocampistas >= 3) ||
                                    (posicionJugador.equalsIgnoreCase("delantero") && delanteros >= 3);

                    if (posicionLlena) {
                        Toast.makeText(MercadoActivity.this, "Ya tienes suficientes jugadores en la posición: " + posicionJugador, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // CONTINÚA CON LA COMPRA
                DatabaseReference jugadorRef = FirebaseDatabase.getInstance().getReference("jugadores").child(jugadorComprado.getId());

                jugadorRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Jugador currentJugador = currentData.getValue(Jugador.class);
                        if (currentJugador == null) return Transaction.success(currentData);

                        if (!"en venta".equalsIgnoreCase(currentJugador.getEstado())) {
                            return Transaction.abort();
                        }

                        currentJugador.setEstado("en propiedad de " + nombreUsuario);
                        currentData.setValue(currentJugador);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        if (!committed) {
                            Toast.makeText(MercadoActivity.this, "El jugador acaba de ser comprado", Toast.LENGTH_SHORT).show();
                            cargarJugadoresDesdeCache();
                            return;
                        }

                        // Restar monedas
                        userRef.child("monedas").setValue(monedas - jugadorComprado.getPrecio());

                        // Añadir jugador al equipo
                        jugadorComprado.setEstado("en propiedad de " + nombreUsuario);
                        userRef.child("equipoUsuario").child(jugadorComprado.getId()).setValue(jugadorComprado);

                        // Quitar del mercado
                        mercadoRef.child("jugadoresEnVenta").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<String> idsActualizadas = new ArrayList<>();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String id = ds.getValue(String.class);
                                    if (id != null && !id.equals(jugadorComprado.getId())) {
                                        idsActualizadas.add(id);
                                    }
                                }
                                mercadoRef.child("jugadoresEnVenta").setValue(idsActualizadas);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                        Toast.makeText(MercadoActivity.this, "Jugador comprado con éxito", Toast.LENGTH_SHORT).show();
                        listaAleatoria.remove(jugadorComprado);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MercadoActivity.this, "Error al acceder a la base de datos del usuario", Toast.LENGTH_SHORT).show();
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
