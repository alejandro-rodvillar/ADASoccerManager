package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReestablecerContrasena extends AppCompatActivity {

    private EditText etCorreo1, etCorreo2;
    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reestablecercontrasena);

        etCorreo1 = findViewById(R.id.editTextText);
        etCorreo2 = findViewById(R.id.editTextText4);
        Button btnAceptar = findViewById(R.id.btnaceptar);
        Button btnVolver = findViewById(R.id.volverlogin);

        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

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

            // buscar en Realtime Database por el campo "email"
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean encontrado = false;

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        if (email != null && email.equalsIgnoreCase(correo1)) {
                            encontrado = true;
                            break;
                        }
                    }

                    if (encontrado) {
                        mAuth.sendPasswordResetEmail(correo1)
                                .addOnCompleteListener(resetTask -> {
                                    if (resetTask.isSuccessful()) {
                                        Toast.makeText(ReestablecerContrasena.this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ReestablecerContrasena.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ReestablecerContrasena.this, "Error al enviar: " + resetTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ReestablecerContrasena.this, "Ese correo no está registrado", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ReestablecerContrasena.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(ReestablecerContrasena.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
