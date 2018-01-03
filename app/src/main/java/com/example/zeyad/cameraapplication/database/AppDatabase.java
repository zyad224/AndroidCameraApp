

/**
 * Created by Zeyad on 12/17/2017.
 */

package com.example.zeyad.cameraapplication.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {Image.class, Location.class}, version = 13)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ImageDao imageDao();




    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {



        }
    };
}
