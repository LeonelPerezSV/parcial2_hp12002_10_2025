package com.example.appintegradambaas.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appintegradambaas.data.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(User user);

    @Query("SELECT * FROM users WHERE email=:email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users")
    List<User> getAll();

    @Update
    void update(User user);

    @Query("UPDATE users SET pendingSync=:pending WHERE email=:email")
    void markPending(String email, boolean pending);

    @Query("DELETE FROM users")
    void deleteAll();
}
