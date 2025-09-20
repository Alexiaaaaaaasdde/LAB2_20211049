package com.example.telecatapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class FinishActivity extends AppCompatActivity {

    private LinearLayout layoutHistorial;
    private Button btnReplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        layoutHistorial = findViewById(R.id.layoutHistorial);
        btnReplay = findViewById(R.id.btnReplay);

        ArrayList<String> historial = getIntent().getStringArrayListExtra("HISTORIAL");

        if (historial != null) {
            for (String item : historial) {
                TextView tv = new TextView(this);
                tv.setText(item);
                tv.setTextSize(16f);
                layoutHistorial.addView(tv);
            }
        }

        btnReplay.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage("¿Está seguro que desea volver a jugar?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Intent intent = new Intent(FinishActivity.this, MainActivity.class);
                        intent.putStringArrayListExtra("HISTORIAL", historial);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}
