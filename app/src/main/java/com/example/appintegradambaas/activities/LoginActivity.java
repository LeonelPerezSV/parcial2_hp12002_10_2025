package com.example.appintegradambaas.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.data.db.AppDatabase;
import com.example.appintegradambaas.data.entities.User;
import com.example.appintegradambaas.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword;
    private Chip chipRemember;
    private AppDatabase db;
    private FirebaseFirestore fs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        chipRemember = findViewById(R.id.chipRemember);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);

        db = AppDatabase.get(this);
        fs = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> doLogin());
        btnGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = String.valueOf(inputEmail.getText()).trim();
        String pass = String.valueOf(inputPassword.getText()).trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 8 || !pass.matches(".*[A-Za-z].*") || !pass.matches(".*\\d.*")) {
            Toast.makeText(this, "Contraseña mínima 8 caracteres alfanuméricos", Toast.LENGTH_SHORT).show();
            return;
        }

        String hash = sha256(pass);

        AsyncTask.execute(() -> {
            User local = db.userDao().findByEmail(email);
            if (local != null && hash.equals(local.passwordHash)) {
                // ✅ Login local
                runOnUiThread(() -> {
                    new SessionManager(this).saveLogin(email, chipRemember.isChecked());
                    Toast.makeText(this, "Inicio de sesión local exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
            } else {
                // 🔍 Intentar obtener desde Firestore
                fs.collection("users").whereEqualTo("email", email).get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                for (QueryDocumentSnapshot d : query) {
                                    String ph = d.getString("passwordHash");
                                    if (hash.equals(ph)) {
                                        // ✅ Usuario válido desde Firestore → guardar local
                                        User u = new User(
                                                email,
                                                d.getString("fullName"),
                                                ph,
                                                d.getDouble("lat") == null ? 0 : d.getDouble("lat"),
                                                d.getDouble("lng") == null ? 0 : d.getDouble("lng"),
                                                false,
                                                System.currentTimeMillis()
                                        );
                                        AsyncTask.execute(() -> db.userDao().upsert(u));
                                        new SessionManager(this).saveLogin(email, chipRemember.isChecked());
                                        Toast.makeText(this, "Inicio de sesión remoto exitoso", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, HomeActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            } else {
                                // 🚀 Usuario no existe en Firestore → crear automáticamente
                                User newUser = new User(email, "Nuevo Usuario", hash, 0, 0, false, System.currentTimeMillis());
                                fs.collection("users").document(email).set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            AsyncTask.execute(() -> db.userDao().upsert(newUser));
                                            new SessionManager(this).saveLogin(email, chipRemember.isChecked());
                                            Toast.makeText(this, "Usuario creado y sincronizado con Firestore", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, HomeActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Error al crear usuario en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }


    private static String sha256(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ return ""; }
    }
}
