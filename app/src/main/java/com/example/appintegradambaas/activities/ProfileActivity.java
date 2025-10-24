package com.example.appintegradambaas.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.data.db.AppDatabase;
import com.example.appintegradambaas.data.entities.User;
import com.example.appintegradambaas.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText inputName, inputEmail;
    private Button btnSave, btnCancel;
    private AppDatabase db;
    private User current;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancelProfile);

        db = AppDatabase.get(this);
        sm = new SessionManager(this);

        AsyncTask.execute(() -> {
            current = db.userDao().findByEmail(sm.getEmail());
            runOnUiThread(() -> {
                if (current != null) {
                    inputName.setText(current.fullName);
                    inputEmail.setText(current.email);
                    inputEmail.setEnabled(false);
                }
            });
        });

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveProfile() {
        if (current == null) return;
        String newName = String.valueOf(inputName.getText()).trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        current.fullName = newName;

        AsyncTask.execute(() -> {
            db.userDao().upsert(current);
            runOnUiThread(() ->
                    Toast.makeText(this, "Perfil actualizado localmente", Toast.LENGTH_SHORT).show());
        });
    }
}
