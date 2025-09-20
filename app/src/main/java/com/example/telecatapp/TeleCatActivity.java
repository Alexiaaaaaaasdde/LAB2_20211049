package com.example.telecatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeleCatActivity extends AppCompatActivity {

    private TextView tvCountdown, tvCantidad, tvSubtitulo, tvTitulo;
    private ImageView imgCat;
    private Button btnNext;

    private int cantidad;
    private int indiceActual = 0;
    private CountDownTimer timer;
    private ExecutorService executor;

    private String opcionTexto;
    private String texto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telecat);

        initViews();
        initData();
        setupUI();
        startImageDisplay();
    }

    private void initViews() {
        tvCountdown = findViewById(R.id.tvCountdown);
        tvCantidad = findViewById(R.id.tvCantidad);
        tvSubtitulo = findViewById(R.id.tvSubtitulo);
        tvTitulo = findViewById(R.id.tvTitulo);
        imgCat = findViewById(R.id.imgCat);
        btnNext = findViewById(R.id.btnNext);

        executor = Executors.newSingleThreadExecutor();
    }

    private void initData() {
        cantidad = getIntent().getIntExtra("CANTIDAD", 1);
        opcionTexto = getIntent().getStringExtra("OPCION_TEXTO");
        texto = getIntent().getStringExtra("TEXTO");

        Log.d("TeleCat", "Cantidad: " + cantidad + ", Opción: " + opcionTexto + ", Texto: " + texto);
    }

    private void setupUI() {
        tvCantidad.setText("Cantidad = " + cantidad);

        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);

        btnNext.setOnClickListener(v -> {
            if (timer != null) {
                timer.cancel();
            }

            // Obtener historial existente (puede venir de partidas anteriores)
            ArrayList<String> historial = getIntent().getStringArrayListExtra("HISTORIAL");
            if (historial == null) {
                historial = new ArrayList<>();
            }

            // Crear descripción de la partida actual
            String descripcionPartida = "Interacción " + (historial.size() + 1) + ": " + cantidad + " imágenes";
            if ("Sí".equals(opcionTexto) && texto != null && !texto.trim().isEmpty()) {
                descripcionPartida += " con texto: '" + texto + "'";
            }

            // Agregar la partida actual al historial
            historial.add(descripcionPartida);

            // Pasar historial actualizado a la pantalla final
            Intent intent = new Intent(TeleCatActivity.this, FinishActivity.class);
            intent.putStringArrayListExtra("HISTORIAL", historial);
            startActivity(intent);
            finish();
        });
    }

    private void startImageDisplay() {
        long totalTime = cantidad * 4000L;

        cargarNuevaImagen();

        timer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int segundos = (int) (millisUntilFinished / 1000);
                int minutos = segundos / 60;
                int segs = segundos % 60;

                tvCountdown.setText(String.format("%02d:%02d", minutos, segs));

                long tiempoTranscurrido = totalTime - millisUntilFinished;
                int imagenDebeMostrar = (int) (tiempoTranscurrido / 4000) + 1;

                if (imagenDebeMostrar > indiceActual && imagenDebeMostrar <= cantidad) {
                    cargarNuevaImagen();
                }
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("00:00");
                habilitarBotonSiguiente();
            }
        };

        timer.start();
    }

    private void cargarNuevaImagen() {
        if (indiceActual >= cantidad) return;

        indiceActual++;

        String urlStr;
        try {
            if ("Sí".equals(opcionTexto) && texto != null && !texto.trim().isEmpty()) {
                String textoEncoded = URLEncoder.encode(texto.trim(), "UTF-8");
                urlStr = "https://cataas.com/cat/says/" + textoEncoded +
                        "?fontSize=30&fontColor=white&ts=" + System.currentTimeMillis();
            } else {
                urlStr = "https://cataas.com/cat?ts=" + System.currentTimeMillis();
            }
        } catch (Exception e) {
            urlStr = "https://cataas.com/cat?ts=" + System.currentTimeMillis();
        }

        cargarImagenAsync(urlStr);
    }

    private void cargarImagenAsync(String urlStr) {
        executor.execute(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "TeleCatApp/1.0");

                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();

                    runOnUiThread(() -> {
                        imgCat.setImageBitmap(bitmap);
                        imgCat.setAlpha(0f);
                        imgCat.animate().alpha(1f).setDuration(300).start();
                    });
                }

                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(TeleCatActivity.this,
                                "Error cargando imagen " + indiceActual, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void habilitarBotonSiguiente() {
        btnNext.setEnabled(true);
        btnNext.setAlpha(1.0f);
        btnNext.setText("✅ Siguiente");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        if (executor != null) executor.shutdown();
    }
}
