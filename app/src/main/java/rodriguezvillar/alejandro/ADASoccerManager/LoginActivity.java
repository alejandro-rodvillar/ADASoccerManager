package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Referencias a los campos de texto
        EditText usuario = findViewById(R.id.usuario);  // Asegúrate que este ID es correcto
        EditText contrasena = findViewById(R.id.etcontraseña);    // Asegúrate que este ID es correcto

        Button btnRegistro = findViewById(R.id.btnregistro);
        Button btnIniciarSesion = findViewById(R.id.btnLogin);  // Asegúrate que este ID es correcto

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

                // Si ambos campos están llenos, procedemos al inicio de sesión
                Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);  // Cambia MainActivity por la siguiente ventana
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // No permitir ir atrás desde la pantalla de login
        super.onBackPressed();
    }
}
