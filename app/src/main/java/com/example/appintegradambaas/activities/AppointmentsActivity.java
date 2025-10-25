package com.example.appintegradambaas.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.adapters.AppointmentAdapter;
import com.example.appintegradambaas.data.db.AppDatabase;
import com.example.appintegradambaas.data.entities.Appointment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private static final int REQUEST_EDIT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments);

        // Ajustar márgenes automáticamente para evitar solaparse con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        // 🔹 Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_appointments);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Citas médicas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_revert);
        }

        db = AppDatabase.get(this);
        recyclerView = findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAppointments();

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentFormActivity.class);
            startActivityForResult(intent, REQUEST_EDIT);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 🔹 Regresar al HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        return true;
    }

    private void loadAppointments() {
        AsyncTask.execute(() -> {
            List<Appointment> list = db.appointmentDao().getAll();
            runOnUiThread(() -> {
                adapter = new AppointmentAdapter(list, this::onEditClicked, this::onDeleteClicked);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private void onEditClicked(Appointment appointment) {
        Intent i = new Intent(this, AppointmentFormActivity.class);
        i.putExtra("id", appointment.id);
        i.putExtra("patient", appointment.patientName);
        i.putExtra("date", appointment.date);
        i.putExtra("time", appointment.time);
        i.putExtra("desc", appointment.description);
        startActivityForResult(i, REQUEST_EDIT);
    }

    private void onDeleteClicked(Appointment appointment) {
        AsyncTask.execute(() -> {
            appointment.pendingDelete = true;
            AppDatabase db = AppDatabase.get(this);
            db.appointmentDao().update(appointment); // 🔹 Primero marcar como pendiente

            runOnUiThread(() -> {
                Toast.makeText(this, "Cita eliminada localmente", Toast.LENGTH_SHORT).show();
                loadAppointments();
            });

            // Intentar sincronizar eliminación con Firebase
            HomeActivity.SyncManager.syncAppointments(this);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadAppointments();
    }
}
