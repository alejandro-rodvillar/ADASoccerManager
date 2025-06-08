package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Firebase Realtime Database
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registrarse extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etNombre;
    private static final String TAG = "RegistroFirebase";
    private DatabaseReference mDatabase;
    CheckBox checkBox;
    Button btnRegistrarse;
    Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarse);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Referencia a Realtime DB

        etNombre = findViewById(R.id.nomregis);
        etEmail = findViewById(R.id.correoregis);
        etPassword = findViewById(R.id.contraregis);
        checkBox = findViewById(R.id.checkBox);
        btnRegistrarse = findViewById(R.id.btnregis);
        btnVolver = findViewById(R.id.volvregis);
        TextView textTerminos = findViewById(R.id.textTerminos);

        textTerminos.setPaintFlags(textTerminos.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        textTerminos.setOnClickListener(v -> {
            Intent intent = new Intent(Registrarse.this, TerminosCondicionesActivity.class);
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(Registrarse.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void registrar(View v) {
        String nombreUsuario = etNombre.getText().toString().trim();
        String correo = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (nombreUsuario.isEmpty()) {
            etNombre.setError("Este campo es obligatorio");
            etNombre.requestFocus();
            return;
        }
        if (correo.isEmpty()) {
            etEmail.setError("Este campo es obligatorio");
            etEmail.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            etPassword.setError("Este campo es obligatorio");
            etPassword.requestFocus();
            return;
        }
        if (!checkBox.isChecked()) {
            Toast.makeText(Registrarse.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // Crear objeto Usuario
                            Usuario usuario = new Usuario(nombreUsuario, correo);

                            // Guardar en Realtime Database en lugar de Firestore
                            mDatabase.child("usuarios").child(uid).setValue(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Enviar verificación por correo
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(this, "Se ha enviado un correo de verificación. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "No se pudo enviar el correo de verificación.", Toast.LENGTH_SHORT).show();
                                        }

                                        // Redirigir a login
                                        Intent intent = new Intent(Registrarse.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error al registrar: La dirección de correo no es válida", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
