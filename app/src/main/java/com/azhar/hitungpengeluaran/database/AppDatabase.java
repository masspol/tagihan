package com.azhar.hitungpengeluaran.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.azhar.hitungpengeluaran.database.dao.DatabaseDao;
import com.azhar.hitungpengeluaran.model.ModelDatabase;

@Database(entities = {ModelDatabase.class}, version = 2, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {
    public abstract DatabaseDao databaseDao();
}
