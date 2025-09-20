package com.example.telecatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Uso de LLM: Claude ayudó con la mejora del diseño UI, manejo de hilos,
// carga de imágenes asíncrona y validaciones del contador
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

    // Lista para guardar historial (para el Ejercicio 3)
    private List<String> historial = new ArrayList<>();

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
        // Recuperar extras desde MainActivity
        cantidad = getIntent().getIntExtra("CANTIDAD", 1);
        opcionTexto = getIntent().getStringExtra("OPCION_TEXTO");
        texto = getIntent().getStringExtra("TEXTO");

        Log.d("TeleCat", "Cantidad: " + cantidad + ", Opción: " + opcionTexto + ", Texto: " + texto);
    }

    private void setupUI() {
        tvCantidad.setText("Cantidad = " + cantidad);

        // Deshabilitar botón inicialmente
        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);

        btnNext.setOnClickListener(v -> {
            if (timer != null) {
                timer.cancel();
            }
            // TODO: Navegar a FinishActivity con historial
            // Intent intent = new Intent(TeleCatActivity.this, FinishActivity.class);
            // intent.putStringArrayListExtra("HISTORIAL", new ArrayList<>(historial));
            // startActivity(intent);
            Toast.makeText(this, "Navegando a pantalla final...", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startImageDisplay() {
        long totalTime = cantidad * 4000L; // 4 segundos por imagen

        // Mostrar primera imagen inmediatamente
        cargarNuevaImagen();

        // Iniciar el contador
        timer = new CountDownTimer(totalTime, 100) { // Update cada 100ms para mejor precisión
            @Override
            public void onTick(long millisUntilFinished) {
                int segundos = (int) (millisUntilFinished / 1000);
                int minutos = segundos / 60;
                int segs = segundos % 60;

                tvCountdown.setText(String.format("%02d:%02d", minutos, segs));

                // Mostrar nueva imagen cada 4 segundos
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

                // Agregar al historial que terminó el tiempo
                historial.add("⏰ Tiempo completado - Total: " + cantidad + " imágenes mostradas");
            }
        };

        timer.start();
    }

    private void cargarNuevaImagen() {
        if (indiceActual >= cantidad) {
            return;
        }

        indiceActual++;

        // Construir URL de la API de cataas.com
        String urlStr;
        try {
            if ("Sí".equals(opcionTexto) && texto != null && !texto.trim().isEmpty()) {
                // URL con texto personalizado
                String textoEncoded = URLEncoder.encode(texto.trim(), "UTF-8");
                urlStr = "https://cataas.com/cat/says/" + textoEncoded +
                        "?fontSize=30&fontColor=white&ts=" + System.currentTimeMillis();
            } else {
                // URL sin texto
                urlStr = "https://cataas.com/cat?ts=" + System.currentTimeMillis();
            }
        } catch (Exception e) {
            Log.e("TeleCat", "Error encoding texto", e);
            urlStr = "https://cataas.com/cat?ts=" + System.currentTimeMillis();
        }

        // Agregar al historial
        String tipoImagen = "Sí".equals(opcionTexto) ? "con texto: '" + texto + "'" : "sin texto";
        historial.add("🐱 Imagen " + indiceActual + "/" + cantidad + " (" + tipoImagen + ")");

        // Cargar imagen en hilo separado
        cargarImagenAsync(urlStr);
    }

    private void cargarImagenAsync(String urlStr) {
        executor.execute(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000); // 5 segundos timeout
                conn.setReadTimeout(10000); // 10 segundos timeout
                conn.setRequestProperty("User-Agent", "TeleCatApp/1.0");

                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();

                    if (bitmap != null) {
                        // Actualizar UI en hilo principal
                        runOnUiThread(() -> {
                            imgCat.setImageBitmap(bitmap);
                            // Pequeña animación de entrada
                            imgCat.setAlpha(0f);
                            imgCat.animate().alpha(1f).setDuration(300).start();
                        });
                    } else {
                        throw new Exception("No se pudo decodificar la imagen");
                    }
                } else {
                    throw new Exception("HTTP Error: " + conn.getResponseCode());
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e("TeleCat", "Error cargando imagen: " + urlStr, e);
                runOnUiThread(() -> {
                    Toast.makeText(TeleCatActivity.this,
                            "Error cargando imagen " + indiceActual, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void habilitarBotonSiguiente() {
        btnNext.setEnabled(true);
        btnNext.setAlpha(1.0f);
        btnNext.setText("✅ Siguiente");

        // Pequeña animación para llamar la atención
        btnNext.animate()
                .scaleX(1.1f).scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() ->
                        btnNext.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Limpiar recursos
        if (timer != null) {
            timer.cancel();
        }

        if (executor != null) {
            executor.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // El contador debe continuar según las especificaciones
        // No pausamos el timer
        Log.d("TeleCat", "Activity pausada, timer continúa");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TeleCat", "Activity resumida");
    }

    // Método para manejar cambios de configuración (rotación)
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("TeleCat", "Configuración cambiada, timer continúa");
        // El timer debe continuar según las especificaciones
    }
}
