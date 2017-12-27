
/**
 * Created by Zeyad on 12/17/2017.
 */

package com.example.zeyad.cameraapplication.database;



import android.arch.persistence.room.*;

@Entity
public class Location {

    @Ignore
    private static int idCounter=144;

    @PrimaryKey
    public int id=0;

    public double latitude;
    public double longitude;
    public double accuracy;

    public Location(double latitude, double longitude, double accuracy) {
        this.latitude= latitude;
        this.longitude= longitude;
        this.accuracy= accuracy;
        this.id=idCounter++;
    }

    public int getId() {
        return this.id;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public double getLatitude(){return this.latitude;}


}
