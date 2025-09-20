package com.example.telecatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class FinishActivity extends AppCompatActivity {

    private LinearLayout layoutHistorial;
    private Button btnReplay;
    private ArrayList<String> historial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        layoutHistorial = findViewById(R.id.layoutHistorial);
        btnReplay = findViewById(R.id.btnReplay);

        // Recuperar historial desde TeleCatActivity
        historial = getIntent().getStringArrayListExtra("HISTORIAL");

        if (historial != null && !historial.isEmpty()) {
            for (int i = 0; i < historial.size(); i++) {
                TextView tv = new TextView(this);
                tv.setText("Interacción " + (i + 1) + ": " + historial.get(i));
                tv.setTextSize(16f);
                tv.setTextColor(getResources().getColor(android.R.color.black));
                tv.setPadding(0, 10, 0, 10);
                layoutHistorial.addView(tv);
            }
        }

        btnReplay.setOnClickListener(v -> mostrarDialogoConfirmacion());
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro que desea volver a jugar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Intent intent = new Intent(FinishActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
