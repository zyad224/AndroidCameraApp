
/**
 * Created by Zeyad on 12/17/2017.
 */

package com.example.zeyad.cameraapplication.database;

import android.arch.persistence.room.*;

/**
 * The class that provides to save the details of locations.
 *
 * In this class, we have primary keys for adding unique location id
 * With using this primary id, we are adding locations information in database.
 *
 */

@Entity
public class Location {

    @Ignore
    private static int idCounter=144;

    @PrimaryKey
    public int id=0;

    public double latitude;
    public double longitude;
    public double accuracy;

    /**
     * The constructor of the location class that we can create the object of the location class
     * with these params
     *
     * @param latitude
     * @param longitude
     * @param accuracy
     */
    public Location(double latitude, double longitude, double accuracy) {
        this.latitude= latitude;
        this.longitude= longitude;
        this.accuracy= accuracy;
        this.id=idCounter++;
    }

    /**
     * Method gets the location id for the image
     *
     * @return the location id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Method gets the longitude of the image
     *
     * @return the longitude of the image
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Method gets the latitude of the image
     *
     * @return the latitude of the image
     */
    public double getLatitude(){return this.latitude;}

}
