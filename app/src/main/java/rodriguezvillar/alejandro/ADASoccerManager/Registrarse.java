package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Registrarse extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etNombre;
    private FirebaseFirestore db;
    private static final String TAG = "RegistroFirebase";
    CheckBox checkBox;
    Button btnRegistrarse;
    Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarse);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        etNombre = findViewById(R.id.nomregis);
        etEmail = findViewById(R.id.correoregis);
        etPassword = findViewById(R.id.contraregis);
        checkBox = findViewById(R.id.checkBox);
        btnRegistrarse = findViewById(R.id.btnregis);
        btnVolver = findViewById(R.id.volvregis);
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

                            // Se utiliza la clase Usuario
                            Usuario usuario = new Usuario(nombreUsuario, correo);

                            db.collection("usuarios").document(uid)
                                    .set(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Registrarse.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            // Se redirige a la primera página después del registro
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(this, "Se ha enviado un correo de verificación. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "No se pudo enviar el correo de verificación.", Toast.LENGTH_SHORT).show();
                                        }

                                        // Después de enviar el correo, redirige al login
                                        Intent intent = new Intent(Registrarse.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });

                        }
                    } else {
                        Toast.makeText(this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
        });
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Registrarse.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }*/
}
