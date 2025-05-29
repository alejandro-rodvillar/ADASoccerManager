package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etIdentificador, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etIdentificador = findViewById(R.id.usuario); // Puede ser correo o nombre de usuario
        etPassword = findViewById(R.id.etContrasena);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegistro = findViewById(R.id.btnregistro);
        TextView textOlvidado = findViewById(R.id.textolvcontra);

        btnRegistro.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, Registrarse.class));
            finish();
        });

        textOlvidado.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ReestablecerContrasena.class));
        });

        btnLogin.setOnClickListener(view -> {
            String identificador = etIdentificador.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (identificador.isEmpty()) {
                etIdentificador.setError("Ingresa tu nombre de usuario o correo");
                etIdentificador.requestFocus();
                return;
            }

            if (pass.isEmpty()) {
                etPassword.setError("Ingresa tu contraseña");
                etPassword.requestFocus();
                return;
            }

            if (Patterns.EMAIL_ADDRESS.matcher(identificador).matches()) {
                // Es un correo
                iniciarSesion(identificador, pass);
            } else {
                // Es un nombre de usuario
                db.collection("usuarios")
                        .whereEqualTo("nombre", identificador)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String correo = queryDocumentSnapshots.getDocuments().get(0).getString("email");
                                iniciarSesion(correo, pass);
                            } else {
                                Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void iniciarSesion(String correo, String pass) {
        mAuth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Verifica tu correo antes de iniciar sesión", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Puedes dejarlo vacío si quieres evitar volver atrás
    }
}
