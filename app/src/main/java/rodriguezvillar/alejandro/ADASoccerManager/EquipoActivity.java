package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EquipoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView tvMonedas;

    private static final Map<String, Integer> camisetaPorEquipo = new HashMap<String, Integer>() {{
        put("Rayo Glacial", R.drawable.camiseta_azul);
        put("Trueno Rojo", R.drawable.camiseta_roja);
        put("Aurora FC", R.drawable.camiseta_amarilla);
        put("Dragones del Norte", R.drawable.camiseta_naranja);
        put("Atlético Eclipse", R.drawable.camiseta_morada);
        put("Estrella del Sur", R.drawable.camiseta_verde);
        put("Titanes del Este", R.drawable.camiseta_turquesa);
        put("Leones del Viento", R.drawable.camiseta_azul_clara);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipo);

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
                startActivity(new Intent(EquipoActivity.this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EquipoActivity.this, LoginActivity.class);
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
                startActivity(new Intent(EquipoActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                startActivity(new Intent(EquipoActivity.this, LigasActivity.class));
                return true;
            } else if (id == R.id.nav_my_team) {
                Toast.makeText(EquipoActivity.this, "Ya estás en Mi Equipo", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(EquipoActivity.this, MercadoActivity.class));
                return true;
            }
            return false;
        });

        TextView tvVenderJugador = findViewById(R.id.tv_vender_jugador);
        if (tvVenderJugador != null) {
            tvVenderJugador.setPaintFlags(tvVenderJugador.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            tvVenderJugador.setOnClickListener(v -> {
                Intent intent = new Intent(EquipoActivity.this, VentaJugadoresActivity.class);
                startActivity(intent);
            });
        }

        // Inicializamos el TextView para monedas
        tvMonedas = findViewById(R.id.textViewMonedas);

        cargarMonedas();

        cargarJugadores();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    private String normalizarPosicion(String posicion) {
        posicion = posicion.toLowerCase();
        if (posicion.equals("centrocampista")) {
            return "mediocampista";
        }
        return posicion;
    }

    private void cargarMonedas() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference refMonedas = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("monedas");

        refMonedas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long monedas = snapshot.getValue(Long.class);
                if (monedas == null) {
                    tvMonedas.setText("Aún no perteneces a una liga");
                } else {
                    tvMonedas.setText("Monedas: " + monedas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EquipoActivity.this, "Error al cargar monedas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarJugadores() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference refEquipo = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("equipoUsuario");

        String[] posiciones = {
                "portero",
                "defensa1", "defensa2", "defensa3", "defensa4",
                "mediocampista1", "mediocampista2", "mediocampista3",
                "delantero1", "delantero2", "delantero3"
        };

        Map<String, Boolean> ocupados = new HashMap<>();
        for (String pos : posiciones) {
            int idNombre = getResources().getIdentifier("tv_nombre_" + pos, "id", getPackageName());
            TextView tvNombre = findViewById(idNombre);
            if (tvNombre != null) {
                tvNombre.setText("");
            }

            int idCamiseta = getResources().getIdentifier("img_camiseta_" + pos, "id", getPackageName());
            ImageView ivCamiseta = findViewById(idCamiseta);
            if (ivCamiseta != null) {
                ivCamiseta.setImageDrawable(null);
            }

            ocupados.put(pos, false);
        }

        refEquipo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(EquipoActivity.this, "No hay jugadores en el equipo", Toast.LENGTH_SHORT).show();

                    for (String pos : posiciones) {
                        int idNombre = getResources().getIdentifier("tv_nombre_" + pos, "id", getPackageName());
                        TextView tvNombre = findViewById(idNombre);
                        if (tvNombre != null) {
                            tvNombre.setText("Vacío");
                        }

                        int idCamiseta = getResources().getIdentifier("img_camiseta_" + pos, "id", getPackageName());
                        ImageView ivCamiseta = findViewById(idCamiseta);
                        if (ivCamiseta != null) {
                            ivCamiseta.setImageDrawable(null);
                        }
                    }
                    return;
                }

                for (DataSnapshot jugadorSnap : snapshot.getChildren()) {
                    String nombre = jugadorSnap.child("nombre").getValue(String.class);
                    String posicion = jugadorSnap.child("posicion").getValue(String.class);
                    String equipo = jugadorSnap.child("equipo").getValue(String.class);

                    if (nombre != null && posicion != null && equipo != null) {
                        String posicionNormalizada = normalizarPosicion(posicion);
                        boolean puesto = false;

                        for (String posSlot : posiciones) {
                            if (posSlot.startsWith(posicionNormalizada) && !ocupados.get(posSlot)) {
                                int idNombre = getResources().getIdentifier("tv_nombre_" + posSlot, "id", getPackageName());
                                TextView tvNombre = findViewById(idNombre);

                                int idCamiseta = getResources().getIdentifier("img_camiseta_" + posSlot, "id", getPackageName());
                                ImageView ivCamiseta = findViewById(idCamiseta);

                                if (tvNombre != null && ivCamiseta != null) {
                                    tvNombre.setText(nombre);

                                    Integer camisetaRes = camisetaPorEquipo.get(equipo);
                                    if (camisetaRes != null) {
                                        ivCamiseta.setImageResource(camisetaRes);
                                    } else {
                                        ivCamiseta.setImageDrawable(null);
                                    }

                                    ocupados.put(posSlot, true);
                                    puesto = true;
                                    break;
                                }
                            }
                        }

                        if (!puesto) {
                            Toast.makeText(EquipoActivity.this,
                                    "No hay slot disponible para " + nombre + " en posición " + posicion,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                for (String pos : posiciones) {
                    if (!ocupados.get(pos)) {
                        int idNombre = getResources().getIdentifier("tv_nombre_" + pos, "id", getPackageName());
                        TextView tvNombre = findViewById(idNombre);
                        if (tvNombre != null) {
                            tvNombre.setText("Vacío");
                        }

                        int idCamiseta = getResources().getIdentifier("img_camiseta_" + pos, "id", getPackageName());
                        ImageView ivCamiseta = findViewById(idCamiseta);
                        if (ivCamiseta != null) {
                            ivCamiseta.setImageDrawable(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EquipoActivity.this, "Error al cargar equipo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
