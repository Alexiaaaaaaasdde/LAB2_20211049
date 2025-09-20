package com.example.telecatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etCantidad, etTexto;
    private Button btnCheck, btnStart;
    private Spinner spTexto;
    private boolean internetOk = false;

    // Variable para mantener el historial entre partidas
    private ArrayList<String> historialGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSpinner();
        setupClickListeners();

        // Recuperar historial si viene de una partida anterior
        historialGlobal = getIntent().getStringArrayListExtra("HISTORIAL");
        if (historialGlobal == null) {
            historialGlobal = new ArrayList<>();
        }
    }

    private void initViews() {
        etCantidad = findViewById(R.id.etCantidad);
        etTexto = findViewById(R.id.etTexto);
        spTexto = findViewById(R.id.spTexto);
        btnCheck = findViewById(R.id.btnCheck);
        btnStart = findViewById(R.id.btnStart);
    }

    private void setupSpinner() {
        // Opciones del spinner
        String[] opciones = {"Elegir", "No", "Sí"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, opciones) {

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setPadding(32, 20, 32, 20);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTexto.setAdapter(adapter);

        spTexto.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String seleccion = spTexto.getSelectedItem().toString();

                // Habilitar campo de texto solo si selecciona "Sí"
                boolean habilitar = seleccion.equals("Sí");
                etTexto.setEnabled(habilitar);

                if (!habilitar) {
                    etTexto.setText("");
                }

                // Cambiar opacidad visualmente
                etTexto.setAlpha(habilitar ? 1.0f : 0.5f);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        // Botón Comprobar Conexión
        btnCheck.setOnClickListener(v -> {
            btnCheck.setText("🔄 Verificando...");
            btnCheck.setEnabled(false);

            v.postDelayed(() -> {
                internetOk = NetworkUtils.hasInternet(this);

                if (internetOk) {
                    Toast.makeText(this, "✅ Conexión establecida", Toast.LENGTH_SHORT).show();
                    btnCheck.setText("✅ Conexión OK");
                    btnStart.setEnabled(true);
                    btnStart.setText("▶ Comenzar");
                } else {
                    Toast.makeText(this, "❌ Sin conexión a internet", Toast.LENGTH_SHORT).show();
                    btnCheck.setText("⚠️ Comprobar\nConexión");
                    btnStart.setEnabled(false);
                }

                btnCheck.setEnabled(true);
            }, 1500);
        });

        // Botón Comenzar → Navegar a TeleCatActivity
        btnStart.setOnClickListener(v -> {
            if (validarFormulario()) {
                int cantidad = Integer.parseInt(etCantidad.getText().toString().trim());
                String opcionTexto = spTexto.getSelectedItem().toString();
                String texto = etTexto.getText().toString().trim();

                Intent intent = new Intent(MainActivity.this, TeleCatActivity.class);
                intent.putExtra("CANTIDAD", cantidad);
                intent.putExtra("OPCION_TEXTO", opcionTexto);
                intent.putExtra("TEXTO", texto);
                // AGREGADO: Pasar el historial actual
                intent.putStringArrayListExtra("HISTORIAL", historialGlobal);
                startActivity(intent);
            }
        });
    }

    private boolean validarFormulario() {
        String cantStr = etCantidad.getText().toString().trim();

        // Validar cantidad
        if (cantStr.isEmpty()) {
            etCantidad.setError("Ingresa una cantidad");
            etCantidad.requestFocus();
            Toast.makeText(this, "⚠️ Debes ingresar la cantidad", Toast.LENGTH_SHORT).show();
            return false;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantStr);
        } catch (NumberFormatException e) {
            etCantidad.setError("Número inválido");
            etCantidad.requestFocus();
            Toast.makeText(this, "⚠️ Ingresa un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cantidad <= 0) {
            etCantidad.setError("Debe ser mayor a 0");
            etCantidad.requestFocus();
            Toast.makeText(this, "⚠️ La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cantidad > 10) {
            etCantidad.setError("Máximo 10 imágenes");
            etCantidad.requestFocus();
            Toast.makeText(this, "⚠️ Máximo 10 imágenes por vez", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar texto si está habilitado
        String spinnerSeleccion = spTexto.getSelectedItem().toString();
        if (spinnerSeleccion.equals("Elegir")) {
            Toast.makeText(this, "⚠️ Selecciona una opción de texto", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerSeleccion.equals("Sí")) {
            String texto = etTexto.getText().toString().trim();
            if (texto.isEmpty()) {
                etTexto.setError("Escribe el texto");
                etTexto.requestFocus();
                Toast.makeText(this, "⚠️ Debes escribir el texto", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (texto.length() < 2) {
                etTexto.setError("Muy corto");
                etTexto.requestFocus();
                Toast.makeText(this, "⚠️ El texto es muy corto", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}
