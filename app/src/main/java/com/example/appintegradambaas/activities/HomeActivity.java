package com.example.appintegradambaas.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appintegradambaas.R;
import com.example.appintegradambaas.data.db.AppDatabase;
import com.example.appintegradambaas.data.entities.User;
import com.example.appintegradambaas.utils.NetworkUtils;
import com.example.appintegradambaas.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private AppDatabase db;
    private FirebaseFirestore fs;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.get(this);
        fs = FirebaseFirestore.getInstance();
        sm = new SessionManager(this);

        String email = sm.getEmail();
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Bienvenido, " + (email != null ? email : ""));

        // Try to sync pending
        syncPendingIfAny();
    }

    private void syncPendingIfAny(){
        if (!NetworkUtils.isOnline(this)) return;
        AsyncTask.execute(() -> {
            String email = sm.getEmail();
            if (email == null) return;
            User u = db.userDao().findByEmail(email);
            if (u != null && u.pendingSync){
                fs.collection("users").document(u.email)
                        .set(new java.util.HashMap<String, Object>() {{
                            put("email", u.email);
                            put("fullName", u.fullName);
                            put("passwordHash", u.passwordHash);
                            put("lat", u.lat);
                            put("lng", u.lng);
                        }}).addOnSuccessListener(a ->
                                AsyncTask.execute(() -> db.userDao().markPending(u.email, false)));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_map) {
            startActivity(new Intent(this, MapActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            new SessionManager(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
