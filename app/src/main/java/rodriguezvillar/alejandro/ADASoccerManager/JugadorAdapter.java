package rodriguezvillar.alejandro.ADASoccerManager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JugadorAdapter extends RecyclerView.Adapter<JugadorAdapter.JugadorViewHolder> {

    private final List<Jugador> listaJugadores;
    private final boolean mostrarBotonComprar;

    public JugadorAdapter(List<Jugador> listaJugadores, boolean mostrarBotonComprar) {
        this.listaJugadores = listaJugadores;
        this.mostrarBotonComprar = mostrarBotonComprar;
    }

    // Mapa fijo: equipo → camiseta (todos los equipos tienen una)
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

    @NonNull
    @Override
    public JugadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_jugador, parent, false);
        return new JugadorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull JugadorViewHolder holder, int position) {
        Jugador jugador = listaJugadores.get(position);

        holder.tvNombre.setText(jugador.getNombre());
        holder.tvEquipo.setText(jugador.getEquipo());
        holder.tvPosicion.setText(jugador.getPosicion());
        holder.tvPrecio.setText("Precio: " + jugador.getPrecio());
        holder.tvPuntos.setText("Puntos: " + jugador.getPuntos());

        String estado = jugador.getEstado().toLowerCase();
        holder.tvEstado.setText(jugador.getEstado());

        switch (estado) {
            case "disponible":
                holder.tvEstado.setTextColor(Color.parseColor("#4CAF50")); // verde
                break;
            case "en venta":
                holder.tvEstado.setTextColor(Color.parseColor("#2196F3")); // azul
                break;
            case "en propiedad":
                holder.tvEstado.setTextColor(Color.parseColor("#9C27B0")); // morado
                break;
            default:
                holder.tvEstado.setTextColor(Color.parseColor("#888888")); // gris por defecto
                break;
        }

        Integer camisetaRes = camisetaPorEquipo.get(jugador.getEquipo());
        if (camisetaRes != null) {
            holder.imgCamiseta.setImageResource(camisetaRes);
        }

        // Mostrar u ocultar botón Comprar
        holder.btnComprar.setVisibility(mostrarBotonComprar ? View.VISIBLE : View.GONE);

        if (mostrarBotonComprar) {
            holder.btnComprar.setOnClickListener(v -> {
                // Llamar a comprarJugador del contexto si es MercadoActivity
                if (holder.itemView.getContext() instanceof MercadoActivity) {
                    ((MercadoActivity) holder.itemView.getContext()).comprarJugador(jugador);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "No se puede comprar aquí", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnComprar.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    public static class JugadorViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEquipo, tvPosicion, tvPrecio, tvPuntos, tvEstado;
        ImageView imgCamiseta;
        Button btnComprar;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvPuntos = itemView.findViewById(R.id.tvPuntos);
            tvEstado = itemView.findViewById(R.id.tvEstadoJugador);
            imgCamiseta = itemView.findViewById(R.id.imgCamiseta);
            btnComprar = itemView.findViewById(R.id.buttonComprar);
        }
    }
}
