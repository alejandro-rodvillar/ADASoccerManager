package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReestablecerContrasena extends AppCompatActivity {

    private EditText etCorreo1, etCorreo2;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reestablecercontrasena);

        etCorreo1 = findViewById(R.id.editTextText);
        etCorreo2 = findViewById(R.id.editTextText4);
        Button btnAceptar = findViewById(R.id.btnaceptar);
        Button btnVolver = findViewById(R.id.volverlogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnAceptar.setOnClickListener(v -> {
            String correo1 = etCorreo1.getText().toString().trim().toLowerCase();
            String correo2 = etCorreo2.getText().toString().trim().toLowerCase();

            if (correo1.isEmpty() || correo2.isEmpty()) {
                Toast.makeText(this, "Ambos campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!correo1.equals(correo2)) {
                Toast.makeText(this, "Los correos no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Buscar en Firestore por el campo "email"
            db.collection("usuarios")
                    .whereEqualTo("email", correo1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Email encontrado en Firestore, enviamos correo de recuperación
                            mAuth.sendPasswordResetEmail(correo1)
                                    .addOnCompleteListener(resetTask -> {
                                        if (resetTask.isSuccessful()) {
                                            Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Error al enviar: " + resetTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Email no encontrado
                            Toast.makeText(this, "Ese correo no está registrado", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(ReestablecerContrasena.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
