package com.example.appintegradambaas.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.data.db.AppDatabase;
import com.example.appintegradambaas.data.entities.Appointment;
import com.example.appintegradambaas.activities.HomeActivity.SyncManager;

import java.util.Calendar;

public class AppointmentFormActivity extends AppCompatActivity {

    private EditText inputPatient, inputDate, inputTime, inputDescription;
    private int appointmentId = 0; // si viene en edición

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_form);

        // Ajustar márgenes automáticamente para evitar solaparse con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });


        inputPatient = findViewById(R.id.inputPatient);
        inputDate = findViewById(R.id.inputDate);
        inputTime = findViewById(R.id.inputTime);
        inputDescription = findViewById(R.id.inputDescription);
        Button btnSave = findViewById(R.id.btnSave);

        inputDate.setOnClickListener(v -> showDatePicker());
        inputTime.setOnClickListener(v -> showTimePicker());

        // 🔹 Cargar datos si es edición
        if (getIntent().hasExtra("id")) {
            appointmentId = getIntent().getIntExtra("id", 0);
            inputPatient.setText(getIntent().getStringExtra("patient"));
            inputDate.setText(getIntent().getStringExtra("date"));
            inputTime.setText(getIntent().getStringExtra("time"));
            inputDescription.setText(getIntent().getStringExtra("desc"));
        }

        btnSave.setOnClickListener(v -> saveAppointment());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH), d = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, (v, year, month, day) ->
                inputDate.setText(String.format("%02d/%02d/%04d", day, month + 1, year)),
                y, m, d).show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY), min = c.get(Calendar.MINUTE);
        new TimePickerDialog(this, (v, hour, minute) ->
                inputTime.setText(String.format("%02d:%02d", hour, minute)),
                h, min, true).show();
    }

    private void saveAppointment() {
        String patient = inputPatient.getText().toString().trim();
        String date = inputDate.getText().toString().trim();
        String time = inputTime.getText().toString().trim();
        String desc = inputDescription.getText().toString().trim();

        if (patient.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Appointment a = new Appointment();
        a.id = appointmentId; // si es 0, Room lo genera nuevo
        a.patientName = patient;
        a.date = date;
        a.time = time;
        a.description = desc;
        a.pendingSync = true;
        a.pendingDelete = false;

        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            if (appointmentId == 0)
                db.appointmentDao().insert(a);
            else
                db.appointmentDao().update(a);

            runOnUiThread(() -> {
                Toast.makeText(this, appointmentId == 0 ? "Cita creada" : "Cita actualizada", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });

           HomeActivity.SyncManager.syncAppointments(this);
        });
    }
}
