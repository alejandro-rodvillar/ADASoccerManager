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
import java.util.List;

public class ListaJugadoresFirebaseActivity extends AppCompatActivity {

    private Toolbar toolbarBusqueda;
    private EditText etBuscarJugador;
    private RecyclerView recyclerView;
    private TextView tvSinResultados;
    private JugadorAdapter adapter;

    private List<Jugador> listaJugadores = new ArrayList<>();
    private List<Jugador> listaFiltrada = new ArrayList<>();

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

        adapter = new JugadorAdapter(listaFiltrada);
        recyclerView.setAdapter(adapter);

        cargarJugadoresDeFirebase();

        // Foco automático y mostrar teclado
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

    private void cargarJugadoresDeFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadores.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Jugador jugador = ds.getValue(Jugador.class);
                    if (jugador != null) {
                        listaJugadores.add(jugador);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(listaJugadores, Comparator.comparing(j -> j.getNombre().toLowerCase()));
                }

                listaFiltrada.clear();
                listaFiltrada.addAll(listaJugadores);
                adapter.notifyDataSetChanged();

                actualizarVistaSinResultados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar error si es necesario
            }
        });
    }

    private void filtrarJugadores(String texto) {
        texto = texto.toLowerCase();
        listaFiltrada.clear();

        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaJugadores);
        } else {
            for (Jugador j : listaJugadores) {
                if (j.getNombre().toLowerCase().contains(texto)) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            volverAMercado();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void volverAMercado() {
        Intent intent = new Intent(this, MercadoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
