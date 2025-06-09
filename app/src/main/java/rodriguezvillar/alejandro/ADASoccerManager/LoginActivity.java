package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;
    private EditText etIdentificador, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // si ya hay sesión iniciada y no han pasado 5 días, ir a MainActivity
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        long lastLoginTime = prefs.getLong("last_login_time", 0);
        long fiveDaysMillis = 5 * 24 * 60 * 60 * 1000L;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && System.currentTimeMillis() - lastLoginTime <= fiveDaysMillis) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        etIdentificador = findViewById(R.id.usuario); // puede ser correo o nombre de usuario
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
                iniciarSesion(identificador, pass);
            } else {
                // es un nombre de usuario, buscar en Realtime DB
                usuariosRef.orderByChild("nombre").equalTo(identificador)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        String correo = child.child("email").getValue(String.class);
                                        iniciarSesion(correo, pass);
                                        break;
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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
                            // guardar timestamp del login
                            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong("last_login_time", System.currentTimeMillis());
                            editor.apply();

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
