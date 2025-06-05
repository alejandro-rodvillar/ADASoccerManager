package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LigasActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView leagueListView;
    private TextView noLeagueTextView;
    private Button joinOrCreateButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef, ligasRef;

    private String currentUserId;
    private String currentLigaId;

    private List<Player> playersList = new ArrayList<>();
    private PlayerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ligas);

        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");
        ligasRef = FirebaseDatabase.getInstance().getReference("ligas");

        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        leagueListView = findViewById(R.id.leagueListView);
        noLeagueTextView = findViewById(R.id.noLeagueTextView);
        joinOrCreateButton = findViewById(R.id.joinOrCreateButton);

        // Configura Toolbar, Drawer y BottomNavigation igual que antes
        setupToolbarAndNavigation();

        // Configurar adaptador para la lista
        adapter = new PlayerAdapter();
        leagueListView.setAdapter(adapter);

        joinOrCreateButton.setOnClickListener(v -> {
            // Redirigir a MainActivity para crear o unirse a una liga
            startActivity(new Intent(LigasActivity.this, MainActivity.class));
            finish();
        });

        loadUserLigaAndPlayers();
    }

    private void setupToolbarAndNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView.setSelectedItemId(R.id.nav_leagues);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(LigasActivity.this, PerfilUsuarioActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(LigasActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(LigasActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(LigasActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_leagues) {
                Toast.makeText(LigasActivity.this, "Ya estás en Ligas", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_my_team) {
                startActivity(new Intent(LigasActivity.this, EquipoActivity.class));
                return true;
            } else if (id == R.id.nav_market) {
                startActivity(new Intent(LigasActivity.this, MercadoActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadUserLigaAndPlayers() {
        if (currentUserId == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Escuchar ligaId del usuario actual en tiempo real
        usuariosRef.child(currentUserId).child("ligaId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentLigaId = snapshot.getValue(String.class);
                if (currentLigaId == null || currentLigaId.isEmpty()) {
                    // No pertenece a ninguna liga
                    leagueListView.setVisibility(View.GONE);
                    noLeagueTextView.setVisibility(View.VISIBLE);
                    joinOrCreateButton.setVisibility(View.VISIBLE);
                } else {
                    // Pertenece a una liga, mostrar lista
                    leagueListView.setVisibility(View.VISIBLE);
                    noLeagueTextView.setVisibility(View.GONE);
                    joinOrCreateButton.setVisibility(View.GONE);

                    listenToLigaPlayers(currentLigaId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LigasActivity.this, "Error al cargar liga: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenToLigaPlayers(String ligaId) {
        ligasRef.child(ligaId).child("jugadores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot jugadoresSnapshot) {
                playersList.clear();

                if (!jugadoresSnapshot.exists()) {
                    Toast.makeText(LigasActivity.this, "No hay jugadores en esta liga", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Por cada UID jugador, obtener info de usuario
                List<String> jugadorUids = new ArrayList<>();
                for (DataSnapshot jugadorSnap : jugadoresSnapshot.getChildren()) {
                    String jugadorUid = jugadorSnap.getKey();
                    if (jugadorUid != null) {
                        jugadorUids.add(jugadorUid);
                    }
                }

                if (jugadorUids.isEmpty()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Leer info de usuarios en paralelo
                fetchPlayersInfo(jugadorUids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LigasActivity.this, "Error al cargar jugadores: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPlayersInfo(List<String> jugadorUids) {
        playersList.clear();

        final int totalPlayers = jugadorUids.size();
        final int[] loadedCount = {0};

        for (String uid : jugadorUids) {
            usuariosRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    if (userSnapshot.exists()) {
                        String nombre = userSnapshot.child("nombre").getValue(String.class);
                        Long puntosLong = userSnapshot.child("puntos").getValue(Long.class);
                        int puntos = puntosLong != null ? puntosLong.intValue() : 0;

                        playersList.add(new Player(uid, nombre, puntos));
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] == totalPlayers) {
                        // Todos cargados, ordenar y actualizar UI
                        Collections.sort(playersList, (p1, p2) -> Integer.compare(p2.puntos, p1.puntos));
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalPlayers) {
                        Collections.sort(playersList, (p1, p2) -> Integer.compare(p2.puntos, p1.puntos));
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(LigasActivity.this, "Error al leer jugador: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Clase interna para representar un jugador
    private static class Player {
        String uid;
        String nombre;
        int puntos;

        Player(String uid, String nombre, int puntos) {
            this.uid = uid;
            this.nombre = nombre != null ? nombre : "Sin nombre";
            this.puntos = puntos;
        }
    }

    // Adaptador personalizado para ListView
    private class PlayerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return playersList.size();
        }

        @Override
        public Object getItem(int position) {
            return playersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView tv = (TextView) view.findViewById(android.R.id.text1);
            Player player = playersList.get(position);

            // Mostrar posición, nombre y puntos
            String texto = (position + 1) + ". " + player.nombre + " - " + player.puntos + " Puntos";
            tv.setText(texto);

            // Resaltar al usuario actual
            if (player.uid.equals(currentUserId)) {
                tv.setTextColor(Color.parseColor("#FF4081")); // Color rosa/fucsia por ejemplo
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tv.setTextColor(Color.BLACK);
                tv.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            return view;
        }
    }
}
