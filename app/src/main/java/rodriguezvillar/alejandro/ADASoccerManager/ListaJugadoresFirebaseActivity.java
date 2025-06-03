package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListaJugadoresFirebaseActivity extends AppCompatActivity {

    private Toolbar toolbarBusqueda;
    private EditText etBuscarJugador;
    private RecyclerView recyclerView;
    private TextView tvSinResultados;
    private JugadorAdapter adapter;

    private List<Jugador> listaJugadores = new ArrayList<>();
    private List<Jugador> listaFiltrada = new ArrayList<>();
    private Set<String> jugadoresEnVenta = new HashSet<>();


    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_jugadores_firebase);

        toolbarBusqueda = findViewById(R.id.toolbarBusqueda);
        setSupportActionBar(toolbarBusqueda);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        }

        etBuscarJugador = findViewById(R.id.etBuscarJugador);
        recyclerView = findViewById(R.id.recyclerViewJugadores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvSinResultados = findViewById(R.id.tvSinResultados);
        tvSinResultados.setVisibility(View.GONE);

        adapter = new JugadorAdapter(listaFiltrada, false);
        recyclerView.setAdapter(adapter);

        cargarJugadoresEnVenta();

        etBuscarJugador.requestFocus();
        etBuscarJugador.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etBuscarJugador, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        etBuscarJugador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarJugadores(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAMercado();
            }
        });
    }

    private void cargarJugadoresEnVenta() {
        FirebaseDatabase.getInstance().getReference("jugadoresEnVenta")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        jugadoresEnVenta.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String nombre = ds.getKey();
                            if (nombre != null) {
                                jugadoresEnVenta.add(normalizar(nombre));
                            }
                        }
                        cargarJugadoresDeFirebase();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void cargarJugadoresDeFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadores.clear();
                Set<String> nombresUnicos = new HashSet<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null && jugador.getNombre() != null && nombresUnicos.add(jugador.getNombre())) {
                        String nombreNormalizado = normalizar(jugador.getNombre());
                        if (jugadoresEnVenta.contains(nombreNormalizado)) {
                            jugador.setEstado("en venta");
                        }
                        listaJugadores.add(jugador);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    listaJugadores.removeIf(j -> j.getNombre() == null);
                    listaJugadores.sort(Comparator.comparing(j -> j.getNombre().toLowerCase()));
                }

                listaFiltrada.clear();
                listaFiltrada.addAll(listaJugadores);
                adapter.notifyDataSetChanged();

                actualizarVistaSinResultados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void filtrarJugadores(String texto) {
        texto = normalizar(texto);
        listaFiltrada.clear();

        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaJugadores);
        } else {
            for (Jugador j : listaJugadores) {
                String nombre = j.getNombre() != null ? normalizar(j.getNombre()) : "";
                String equipo = j.getEquipo() != null ? normalizar(j.getEquipo()) : "";

                if (nombre.contains(texto) || equipo.contains(texto)) {
                    listaFiltrada.add(j);
                }
            }
        }
        adapter.notifyDataSetChanged();
        actualizarVistaSinResultados();
    }


    private void actualizarVistaSinResultados() {
        if (listaFiltrada.isEmpty()) {
            tvSinResultados.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvSinResultados.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void volverAMercado() {
        Intent intent = new Intent(this, MercadoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String normalizar(String texto) {
        return texto.trim().toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            volverAMercado();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
