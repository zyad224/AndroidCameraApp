

/**
 * Created by Zeyad on 12/17/2017.
 */

package com.example.zeyad.cameraapplication.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {Image.class, Location.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ImageDao imageDao();




    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE Image "
                    + " ADD COLUMN imageLength TEXT");
            database.execSQL("ALTER TABLE Image "
                    + " ADD COLUMN imageWidth TEXT");
        }
    };
}
