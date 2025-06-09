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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JugadorAdapter extends RecyclerView.Adapter<JugadorAdapter.JugadorViewHolder> {

    public interface OnJugadorButtonClickListener {
        void onButtonClick(Jugador jugador);
    }

    private final List<Jugador> listaJugadores;
    private final int layoutResId;
    private final OnJugadorButtonClickListener listener;
    private final boolean mostrarBoton;

    public JugadorAdapter(List<Jugador> listaJugadores, int layoutResId, boolean mostrarBoton, OnJugadorButtonClickListener listener) {
        this.listaJugadores = listaJugadores;
        this.layoutResId = layoutResId;
        this.mostrarBoton = mostrarBoton;
        this.listener = listener;
    }

    // mapa fijo: equipo → camiseta (todos los equipos tienen una)
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
                .inflate(layoutResId, parent, false);
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

        String estado = jugador.getEstado() != null ? jugador.getEstado().toLowerCase() : "";
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

        // mostrar u ocultar botón según flag
        holder.btnComprar.setVisibility(mostrarBoton ? View.VISIBLE : View.GONE);

        if (mostrarBoton) {
            holder.btnComprar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onButtonClick(jugador);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "No hay acción definida", Toast.LENGTH_SHORT).show();
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
