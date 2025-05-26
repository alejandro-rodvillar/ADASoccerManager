package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Se referencian los campos de texto
        EditText usuario = findViewById(R.id.usuario);
        EditText contrasena = findViewById(R.id.etContrasena);

        Button btnRegistro = findViewById(R.id.btnregistro);
        Button btnIniciarSesion = findViewById(R.id.btnLogin);

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, Registrarse.class);
                startActivity(intent);
                finish();
            }
        });

        TextView textView = findViewById(R.id.textolvcontra);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ReestablecerContrasena.class);
                startActivity(intent);
            }
        });

        // Listener para el botón de Iniciar Sesión
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreUsuario = usuario.getText().toString().trim();
                String pass = contrasena.getText().toString().trim();

                if (nombreUsuario.isEmpty()) {
                    usuario.setError("Ingresa tu nombre de usuario");
                    usuario.requestFocus();
                    return;
                }

                if (pass.isEmpty()) {
                    contrasena.setError("Ingresa tu contraseña");
                    contrasena.requestFocus();
                    return;
                }

                // Se busca el usuario por nombre en Firestore
                db.collection("usuarios")
                        .whereEqualTo("nombre", nombreUsuario)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Si se encuentra el ususario:
                                String correo = queryDocumentSnapshots.getDocuments().get(0).getString("email");

                                // Se inicia sesión con correo y contraseña en Firebase Auth
                                mAuth.signInWithEmailAndPassword(correo, pass)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Si no se encuentra el nombre de usuario:
                                Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(LoginActivity.this, "Error en la base de datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        // No se permite ir atrás desde la pantalla de login
        super.onBackPressed();
    }
}
