package com.example.delivgo.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LivraisonLocal.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LivraisonDao livraisonDao();

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "delivgo_db"
            ).allowMainThreadQueries().build();
        }
        return instance;
    }
}