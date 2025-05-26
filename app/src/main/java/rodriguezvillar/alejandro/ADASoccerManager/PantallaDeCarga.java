package rodriguezvillar.alejandro.ADASoccerManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class PantallaDeCarga extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalladecarga);

        progressBar = findViewById(R.id.progressBar);

        // Simulación de la carga durante 4 segundos
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Parada del hilo para simular la carga
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Al terminar la carga, se redirige a la siguiente actividad
                Intent intent = new Intent(PantallaDeCarga.this, PantallaDeEspera.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
