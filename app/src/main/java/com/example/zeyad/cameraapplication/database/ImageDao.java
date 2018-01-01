

/**
 * Created by Zeyad on 12/17/2017.
 */


package com.example.zeyad.cameraapplication.database;


import android.arch.persistence.room.*;
import android.provider.MediaStore;
import java.util.List;



@Dao
public interface ImageDao {

    @Query("SELECT * FROM Image "
            + "INNER JOIN Location ON location.id = Image.location_id "
            + "WHERE location.latitude <= (:latitude + :radius) "
            + " AND location.latitude >= (:latitude - :radius) "
            + " AND location.longitude <= (:longitude + :radius) "
            + " AND location.longitude >= (:longitude - :radius) "
    )
    public List<Image> findImagesByArea(double latitude, double longitude, int radius);

    @Query("SELECT * FROM Image WHERE title=:title")
    public List<Image> findImageByTitle (String title);

    @Query("SELECT * FROM Location WHERE id=:id")
    public Location findLocationById (int id);

    @Query("SELECT * FROM Image WHERE location_id=:id")
    public Image findImageByLocationID (int id);

    ////////////////////////////////////////////////////
    @Query("SELECT * FROM Image WHERE imagepath LIKE :path")
    public Image findImageByPath (String path);

    ////////////////////////////////////////////////////
    @Query("UPDATE Image SET title=:title, description=:description WHERE imagepath in(:path) ")
    void updateImageDetails(String path, String title, String description);

    //// for all pictures
    @Query("SELECT * FROM Image")
    public List<Image> loadImages();

    @Query("SELECT * FROM Location")
    public List<Location> loadLocations();

    @Query("SELECT COUNT(*) FROM Image")
    public int imageCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImage(Image image);


    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(Location location);

    @Delete
    void deleteImage(Image image);
}
