package com.example.appintegradambaas.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.data.db.AppDatabase;
import com.example.appintegradambaas.data.entities.User;
import com.example.appintegradambaas.utils.NetworkUtils;
import com.example.appintegradambaas.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText inputName, inputEmail;
    private Button btnSave, btnCancel, btnBackHome;
    private AppDatabase db;
    private User current;
    private SessionManager sm;
    private FirebaseFirestore fs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // 🔹 Ajustar márgenes automáticos (evita que se tape con barra superior)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancelProfile);
        btnBackHome = findViewById(R.id.btnBackHome);

        db = AppDatabase.get(this);
        sm = new SessionManager(this);
        fs = FirebaseFirestore.getInstance();

        // 🔹 Cargar datos del usuario actual desde Room
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
        btnBackHome.setOnClickListener(v -> {
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void saveProfile() {
        if (current == null) return;
        String newName = String.valueOf(inputName.getText()).trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        current.fullName = newName;
        current.pendingSync = true; // 🔹 marcar para sincronización local-remota

        // 🔹 Guardar localmente
        AsyncTask.execute(() -> {
            db.userDao().upsert(current);
            runOnUiThread(() -> Toast.makeText(this, "Perfil actualizado localmente", Toast.LENGTH_SHORT).show());
        });

        // 🔹 Guardar remotamente si hay conexión
        if (NetworkUtils.isOnline(this)) {
            fs.collection("users")
                    .document(current.email)
                    .set(current)
                    .addOnSuccessListener(a -> Toast.makeText(this, "Perfil sincronizado con Firebase", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al sincronizar con Firebase", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Sin conexión. Se sincronizará después.", Toast.LENGTH_SHORT).show();
        }
    }
}
