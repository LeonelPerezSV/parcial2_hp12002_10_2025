package com.example.appintegradambaas.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.appintegradambaas.data.dao.UserDao;
import com.example.appintegradambaas.data.dao.AppointmentDao;
import com.example.appintegradambaas.data.entities.User;
import com.example.appintegradambaas.data.entities.Appointment;

@Database(entities = {User.class, Appointment.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract AppointmentDao appointmentDao(); // ✅ agregado

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "app_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
