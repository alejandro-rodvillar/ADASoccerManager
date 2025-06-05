package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import rodriguezvillar.alejandro.ADASoccerManager.UsuarioPuntuacion;


import java.util.List;

public class ClasificacionAdapter extends RecyclerView.Adapter<ClasificacionAdapter.ViewHolder> {

    private final List<UsuarioPuntuacion> usuarios;
    private final String usuarioActualUID;
    private final Context context;


    public ClasificacionAdapter(List<UsuarioPuntuacion> usuarios, String usuarioActualUID) {
        this.usuarios = usuarios;
        this.usuarioActualUID = usuarioActualUID;
        this.context = null;
    }

    public ClasificacionAdapter(List<UsuarioPuntuacion> usuarios, String usuarioActualUID, Context context) {
        this.usuarios = usuarios;
        this.usuarioActualUID = usuarioActualUID;
        this.context = context;
    }

    @NonNull
    @Override
    public ClasificacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout de cada fila
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clasificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClasificacionAdapter.ViewHolder holder, int position) {
        UsuarioPuntuacion usuario = usuarios.get(position);

        // Posición (empezando en 1)
        holder.tvPosicion.setText(String.valueOf(position + 1));
        holder.tvNombre.setText(usuario.nombre);
        holder.tvPuntos.setText(String.valueOf(usuario.puntos));

        // Resaltar usuario actual (fondo gris claro y texto en azul, por ejemplo)
        if (usuario.uid.equals(usuarioActualUID)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D0E8FF")); // azul claro
            holder.tvNombre.setTextColor(Color.parseColor("#0077CC")); // azul fuerte
            holder.tvPosicion.setTextColor(Color.parseColor("#0077CC"));
            holder.tvPuntos.setTextColor(Color.parseColor("#0077CC"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.tvNombre.setTextColor(Color.BLACK);
            holder.tvPosicion.setTextColor(Color.BLACK);
            holder.tvPuntos.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosicion, tvNombre, tvPuntos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPuntos = itemView.findViewById(R.id.tvPuntos);
        }
    }
}
