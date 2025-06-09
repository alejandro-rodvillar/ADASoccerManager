package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import rodriguezvillar.alejandro.ADASoccerManager.Registrarse;

public class TerminosCondicionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos_ycondiciones);

        Button btnAceptar = findViewById(R.id.btnAceptar);

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // aqui se cambia TerminosCondicionesActivity.class por la actividad de registro
                Intent intent = new Intent(TerminosCondicionesActivity.this, Registrarse.class);
                startActivity(intent);
                finish(); // para cerrar esta pantalla
            }
        });
    }
}
