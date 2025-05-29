package rodriguezvillar.alejandro.ADASoccerManager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JugadorAdapter extends RecyclerView.Adapter<JugadorAdapter.JugadorViewHolder> {

    private final List<Jugador> listaJugadores;

    public JugadorAdapter(List<Jugador> listaJugadores) {
        this.listaJugadores = listaJugadores;
    }

    // Mapa fijo: equipo → camiseta
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
        holder.tvEstado.setText(jugador.getEstado());

        switch (jugador.getEstado().toLowerCase()) {
            case "disponible":
                holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "lesionado":
                holder.tvEstado.setTextColor(Color.parseColor("#F44336"));
                break;
            case "sancionado":
                holder.tvEstado.setTextColor(Color.parseColor("#FF9800"));
                break;
            default:
                holder.tvEstado.setTextColor(Color.parseColor("#888888"));
                break;
        }

        // No hay fallback: todos los equipos tienen camiseta
        holder.imgCamiseta.setImageResource(camisetaPorEquipo.get(jugador.getEquipo()));
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    public static class JugadorViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEquipo, tvPosicion, tvPrecio, tvPuntos, tvEstado;
        ImageView imgCamiseta;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvPuntos = itemView.findViewById(R.id.tvPuntos);
            tvEstado = itemView.findViewById(R.id.tvEstadoJugador);
            imgCamiseta = itemView.findViewById(R.id.imgCamiseta);
        }
    }
}
