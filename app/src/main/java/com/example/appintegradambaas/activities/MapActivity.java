package com.example.appintegradambaas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.fragments.MapFragment;
import com.google.android.gms.maps.GoogleMap;

public class MapActivity extends AppCompatActivity {

    private MapFragment mapFragment;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

// Ajustar márgenes automáticamente para evitar solaparse con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });
        // 🔹 Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);

        // 🔹 Habilitar botón de regreso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_media_previous);
            getSupportActionBar().setTitle("Mapa interactivo");
        }

        // 🔹 Cargar el fragmento del mapa
        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();
        }

        // 🔹 Botón para volver al Home
        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // 🔹 Acción del botón "volver"
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // 🔹 Menú superior con tipos de mapa
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tipo_mapa, menu);
        return true;
    }

    // 🔹 Manejo de acciones del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mapFragment == null || mapFragment.getMap() == null)
            return super.onOptionsItemSelected(item);

        mMap = mapFragment.getMap();
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.mapa_normal) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        } else if (id == R.id.mapa_satellite) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        } else if (id == R.id.mapa_hybrid) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        } else if (id == R.id.mapa_terrain) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
