package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VentaJugadoresActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private RecyclerView recyclerViewVenta;
    private JugadorAdapter adapter;
    private List<Jugador> listaJugadoresUsuario = new ArrayList<>();
    private DatabaseReference userEquipoRef;
    private DatabaseReference userRef;
    private TextView textViewMonedas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_jugadores);

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_team);
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
                startActivity(new Intent(this, MercadoActivity.class));
                return true;
            }
            return false;
        });

        recyclerViewVenta = findViewById(R.id.recyclerViewVenta);
        recyclerViewVenta.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JugadorAdapter(listaJugadoresUsuario, R.layout.item_jugador_2, true, jugador -> venderJugador(jugador));
        recyclerViewVenta.setAdapter(adapter);


        textViewMonedas = findViewById(R.id.textViewMonedas);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEquipoRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("equipoUsuario");
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        cargarJugadoresUsuario();
        cargarMonedasUsuario();
    }

    private void cargarJugadoresUsuario() {
        userEquipoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadoresUsuario.clear();
                for (DataSnapshot jugadorSnap : snapshot.getChildren()) {
                    Jugador jugador = jugadorSnap.getValue(Jugador.class);
                    if (jugador != null) {
                        jugador.setId(jugadorSnap.getKey());
                        listaJugadoresUsuario.add(jugador);
                    }
                }

                // ordenar por posición: portero, defensa, centrocampista, delantero
                Collections.sort(listaJugadoresUsuario, new Comparator<Jugador>() {
                    @Override
                    public int compare(Jugador j1, Jugador j2) {
                        return Integer.compare(prioridadPosicion(j1.getPosicion()), prioridadPosicion(j2.getPosicion()));
                    }
                });

                adapter.notifyDataSetChanged();

                if (listaJugadoresUsuario.isEmpty()) {
                    Toast.makeText(VentaJugadoresActivity.this, "No tienes jugadores en tu equipo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VentaJugadoresActivity.this, "Error al cargar tus jugadores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int prioridadPosicion(String posicion) {
        if (posicion == null) return 999; // al final si es nulo
        switch (posicion.toLowerCase()) {
            case "portero":
                return 1;
            case "defensa":
                return 2;
            case "centrocampista":
                return 3;
            case "delantero":
                return 4;
            default:
                return 999;
        }
    }

    private void cargarMonedasUsuario() {
        userRef.child("monedas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long monedas = snapshot.getValue(Long.class);
                    if (monedas != null) {
                        textViewMonedas.setText("Monedas: " + monedas);
                    } else {
                        textViewMonedas.setText("Monedas: 0");
                    }
                } else {
                    textViewMonedas.setText("Monedas no disponibles");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textViewMonedas.setText("Error al cargar monedas");
            }
        });
    }

    public void venderJugador(Jugador jugador) {
        new android.app.AlertDialog.Builder(VentaJugadoresActivity.this)
                .setTitle("Confirmar venta")
                .setMessage("¿Estás seguro de que quieres vender a " + jugador.getNombre() + "?")
                .setPositiveButton("Vender", (dialog, which) -> {
                    String jugadorId = jugador.getId();
                    if (jugadorId == null) {
                        Toast.makeText(VentaJugadoresActivity.this, "Error: jugador sin ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference refEquipoUsuario = FirebaseDatabase.getInstance()
                            .getReference("usuarios").child(uid).child("equipoUsuario").child(jugadorId);

                    DatabaseReference refJugador = FirebaseDatabase.getInstance()
                            .getReference("jugadores").child(jugadorId).child("estado");

                    DatabaseReference refMonedasUsuario = FirebaseDatabase.getInstance()
                            .getReference("usuarios").child(uid).child("monedas");

                    refEquipoUsuario.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            refJugador.setValue("disponible").addOnCompleteListener(taskEstado -> {
                                if (taskEstado.isSuccessful()) {
                                    // actualizar las monedas del usuario
                                    refMonedasUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            long monedasActuales = 0;
                                            if (snapshot.exists()) {
                                                Long valor = snapshot.getValue(Long.class);
                                                if (valor != null) {
                                                    monedasActuales = valor;
                                                }
                                            }
                                            long precioJugador = jugador.getPrecio();
                                            long monedasNuevas = monedasActuales + precioJugador;

                                            refMonedasUsuario.setValue(monedasNuevas).addOnCompleteListener(taskUpdate -> {
                                                if (taskUpdate.isSuccessful()) {
                                                    textViewMonedas.setText("Monedas: " + monedasNuevas);
                                                    Toast.makeText(VentaJugadoresActivity.this, "Jugador vendido correctamente", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(VentaJugadoresActivity.this, "Error al actualizar monedas", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(VentaJugadoresActivity.this, "Error al leer monedas", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {
                                    Toast.makeText(VentaJugadoresActivity.this, "Error al actualizar estado del jugador", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(VentaJugadoresActivity.this, "Error al eliminar jugador del equipo", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
