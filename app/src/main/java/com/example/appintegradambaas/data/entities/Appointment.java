package com.example.appintegradambaas.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String patientName;
    public String date;
    public String time;
    public String description;
    public boolean pendingSync;

    public boolean pendingDelete;

    public Appointment() {
        this.pendingSync = true;
        this.pendingDelete = false;
    }
}
