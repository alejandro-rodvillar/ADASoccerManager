package rodriguezvillar.alejandro.ADASoccerManager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JugadorAdapter extends RecyclerView.Adapter<JugadorAdapter.JugadorViewHolder> {

    private List<Jugador> listaJugadores;

    public JugadorAdapter(List<Jugador> listaJugadores) {
        this.listaJugadores = listaJugadores;
    }

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

        // Cambiar color del texto del estado según el valor
        switch (jugador.getEstado().toLowerCase()) {
            case "disponible":
                holder.tvEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "lesionado":
                holder.tvEstado.setTextColor(Color.parseColor("#F44336")); // Rojo
                break;
            case "sancionado":
                holder.tvEstado.setTextColor(Color.parseColor("#FF9800")); // Naranja
                break;
            default:
                holder.tvEstado.setTextColor(Color.parseColor("#888888")); // Gris por defecto
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    public static class JugadorViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEquipo, tvPosicion, tvPrecio, tvPuntos, tvEstado;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvPuntos = itemView.findViewById(R.id.tvPuntos);
            tvEstado = itemView.findViewById(R.id.tvEstadoJugador);
        }
    }
}
