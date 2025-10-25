package com.example.appintegradambaas.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.example.appintegradambaas.data.entities.Appointment;

@Dao
public interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Appointment appointment);

    @Update
    void update(Appointment appointment);

    @Delete
    void delete(Appointment appointment);

    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    List<Appointment> getAll();

    @Query("DELETE FROM appointments WHERE id = :id")
    void deleteById(int id);
}
