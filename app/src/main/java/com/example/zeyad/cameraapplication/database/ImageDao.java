

/**
 * Created by Zeyad on 12/17/2017.
 */


package com.example.zeyad.cameraapplication.database;


import android.arch.persistence.room.*;
import android.provider.MediaStore;
import java.util.List;



@Dao
public interface ImageDao {
    

    /**
     * The query gets the list of the images that depend on their title
     * from  database
     *
     * @param title is the title of the image
     * @return the list of images
     */
    @Query("SELECT * FROM Image WHERE title=:title")
    public List<Image> findImageByTitle (String title);

    /**
     * The query gets the locations that depend on their id from database
     *
     * @param id is the location id
     * @return the location(latitude and longitude)
     */
    @Query("SELECT * FROM Location WHERE id=:id")
    public Location findLocationById (int id);

    /**
     * The query gets the images that depend on their location id from database
     *
     * @param id is the location id of the image
     * @return the image details
     */
    @Query("SELECT * FROM Image WHERE location_id=:id")
    public Image findImageByLocationID (int id);


    /**
     * The query gets the images that depend on their paths from database
     *
     * @param path is the image path
     * @return the image details
     */
    @Query("SELECT * FROM Image WHERE imagepath LIKE :path")
    public Image findImageByPath (String path);

    /**
     * The query updates the title and description of the images
     * that depend on their paths from database
     *
     * @param path is the image path
     * @param title is the image title
     * @param description is the image description
     */
    @Query("UPDATE Image SET title=:title, description=:description WHERE imagepath in(:path) ")
    void updateImageDetails(String path, String title, String description);

    /**
     * The query updates the offline mode of images that depend on their paths
     * from database
     * @param path is the image path
     * @param offline is the offline status
     */
    @Query("UPDATE Image SET offline=:offline WHERE imagepath in(:path) ")
    void updateImageOffline(String path,String offline);

    /**
     * The query gets images that depend on their offline status
     * If offline is true, the image is failed to be sent to the server
     * If offline is false, the image is sent to the server
     *
     * @param mode (offline)
     *
     * @return the list of images
     */
    @Query("SELECT * FROM Image WHERE offline=:mode")
    public List<Image> loadOfflineImages(String mode);


    /**
     * The query that gets the all images from database
     *
     * @return the list of images
     */
    @Query("SELECT * FROM Image")
    public List<Image> loadImages();

    /**
     * The query that gets the all locations from database
     *
     *  @return the list of locations
     */
    @Query("SELECT * FROM Location")
    public List<Location> loadLocations();

    /**
     * The query that gets the count of images from database
     *
     * @return the number of images
     */
    @Query("SELECT COUNT(*) FROM Image")
    public int imageCount();

    /**
     * Method inserts the image in database
     *
     * @param image
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImage(Image image);

    /**
     * Method inserts the location in database
     *
     * @param location
     */
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(Location location);

    /**
     * Method deletes the image in database
     *
     * @param image
     */
    @Delete
    void deleteImage(Image image);
}
