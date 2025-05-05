package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Registrarse extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarse);

        EditText usuario = findViewById(R.id.nomregis);
        EditText email = findViewById(R.id.correoregis);
        EditText contrasena = findViewById(R.id.contraregis);
        CheckBox checkBox = findViewById(R.id.checkBox);
        Button btnRegistrarse = findViewById(R.id.btnregis);
        Button btnVolver = findViewById(R.id.volvregis);

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = usuario.getText().toString().trim();
                String correo = email.getText().toString().trim();
                String pass = contrasena.getText().toString().trim();

                if (nombreUsuario.isEmpty()) {
                    usuario.setError("Este campo es obligatorio");
                    usuario.requestFocus();
                    return;
                }

                if (correo.isEmpty()) {
                    email.setError("Este campo es obligatorio");
                    email.requestFocus();
                    return;
                }

                if (pass.isEmpty()) {
                    contrasena.setError("Este campo es obligatorio");
                    contrasena.requestFocus();
                    return;
                }

                if (!checkBox.isChecked()) {
                    Toast.makeText(Registrarse.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Si todo está completo
                Toast.makeText(Registrarse.this, "Registro completo", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Registrarse.this, LoginActivity.class);  // Redirige a la pantalla de login
                startActivity(intent);
                finish();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Registrarse.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}