package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ReestablecerContrasena extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reestablecercontrasena);

        Button btn = findViewById(R.id.volverlogin);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se redirige a la pantalla de login
                Intent intent = new Intent(ReestablecerContrasena.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
