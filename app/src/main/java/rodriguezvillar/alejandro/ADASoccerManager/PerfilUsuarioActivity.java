package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usuariosRef;

    private TextView textViewNombreUsuario, textViewCorreo, textViewNombreGuardado;
    private EditText editTextNombre;
    private Button buttonGuardarNombre, buttonCambiarContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);

        // Enlazar vistas
        textViewNombreUsuario = findViewById(R.id.textViewNombreUsuario);
        textViewCorreo = findViewById(R.id.textViewCorreo);
        textViewNombreGuardado = findViewById(R.id.textViewNombreGuardado);
        editTextNombre = findViewById(R.id.editTextNombre);
        buttonGuardarNombre = findViewById(R.id.buttonGuardarNombre);
        buttonCambiarContrasena = findViewById(R.id.buttonCambiarContrasena);

        // Mostrar información
        String correo = currentUser.getEmail();
        textViewCorreo.setText("Correo: " + correo);

        usuariosRef.child("nombre").get().addOnSuccessListener(dataSnapshot -> {
            String nombre = dataSnapshot.getValue(String.class);
            if (nombre != null && !nombre.isEmpty()) {
                textViewNombreUsuario.setText("Nombre de usuario: " + nombre);
                textViewNombreGuardado.setText(nombre);
            }
        });

        // Guardar nuevo nombre
        buttonGuardarNombre.setOnClickListener(v -> {
            String nuevoNombre = editTextNombre.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            usuariosRef.child("nombre").setValue(nuevoNombre)
                    .addOnSuccessListener(aVoid -> {
                        textViewNombreUsuario.setText("Nombre de usuario: " + nuevoNombre);
                        textViewNombreGuardado.setText(nuevoNombre);
                        Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show();
                        editTextNombre.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        });

        // Cambiar contraseña: enviar correo
        buttonCambiarContrasena.setOnClickListener(v -> {
            String correoUsuario = currentUser.getEmail();

            if (correoUsuario != null) {
                mAuth.sendPasswordResetEmail(correoUsuario)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Correo enviado para restablecer la contraseña", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Error al enviar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("PerfilUsuario", "Error al enviar email", task.getException());
                            }
                        });
            } else {
                Toast.makeText(this, "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
