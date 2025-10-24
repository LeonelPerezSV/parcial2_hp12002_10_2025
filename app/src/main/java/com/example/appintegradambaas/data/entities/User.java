package com.example.appintegradambaas.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey @NonNull
    public String email;
    public String fullName;
    public String passwordHash;
    public double lat;
    public double lng;
    public boolean pendingSync;
    public long createdAt;

    public User(@NonNull String email, String fullName, String passwordHash, double lat, double lng, boolean pendingSync, long createdAt) {
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.lat = lat;
        this.lng = lng;
        this.pendingSync = pendingSync;
        this.createdAt = createdAt;
    }
}
