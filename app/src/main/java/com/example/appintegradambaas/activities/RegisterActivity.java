package com.example.appintegradambaas.activities;

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
import com.example.appintegradambaas.utils.NetworkUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText inputName, inputEmail, inputPassword;
    private AppDatabase db;
    private FirebaseFirestore fs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        db = AppDatabase.get(this);
        fs = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister(){
        String name = String.valueOf(inputName.getText()).trim();
        String email = String.valueOf(inputEmail.getText()).trim();
        String pass = String.valueOf(inputPassword.getText()).trim();

        if (name.length() < 7){
            Toast.makeText(this, "Nombre mínimo 7 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 8 || !pass.matches(".*[A-Za-z].*") || !pass.matches(".*\\d.*")){
            Toast.makeText(this, "Contraseña mínima 8 caracteres alfanuméricos", Toast.LENGTH_SHORT).show();
            return;
        }

        String hash = sha256(pass);
        boolean online = NetworkUtils.isOnline(this);

        User u = new User(email, name, hash, 13.71622, -89.20323, !online, System.currentTimeMillis());

        AsyncTask.execute(() -> {
            db.userDao().upsert(u);
        });

        if (online){
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("fullName", name);
            data.put("passwordHash", hash);
            data.put("lat", u.lat);
            data.put("lng", u.lng);
            fs.collection("users").document(email).set(data)
                .addOnSuccessListener(a -> Toast.makeText(this, "Registrado (nube y local)", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Guardado local, error nube", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Sin internet: guardado local y marcado para sincronizar", Toast.LENGTH_LONG).show();
        }

        finish();
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
