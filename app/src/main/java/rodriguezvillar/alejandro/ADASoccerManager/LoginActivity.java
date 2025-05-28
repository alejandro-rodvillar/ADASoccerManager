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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.usuario); // CAMBIA el hint a “Correo electrónico” en XML
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
            String correo = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (correo.isEmpty()) {
                etEmail.setError("Ingresa tu correo");
                etEmail.requestFocus();
                return;
            }

            if (pass.isEmpty()) {
                etPassword.setError("Ingresa tu contraseña");
                etPassword.requestFocus();
                return;
            }

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
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // O puedes dejarlo vacío si quieres bloquear el botón atrás
    }
}
