/**
 * Created by Zeyad on 12/17/2017.
 */


package com.example.zeyad.cameraapplication.database;



import android.arch.persistence.room.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import static android.arch.persistence.room.ForeignKey.CASCADE;


/**
 * The class that provides to save the details of images.
 *
 * In this class, we have foreign keys for providing relationship
 * between location class and image class. With using this relationship,
 * we are adding images information in database.
 *
 */
@Entity(foreignKeys = @ForeignKey(entity = Location.class,
        parentColumns = "id",
        childColumns = "location_id",
        onDelete=CASCADE))


public class Image {


    @Ignore
    private static int idCounter=1234;

    @PrimaryKey
    public int id;


    public String title;
    public String description;
    public String date;
    public String imagepath;
    public String imageLength;
    public String imageWidth;
    public String offline;
    @ColumnInfo(name = "location_id")
    public int locationId;

    @Ignore
    Bitmap picture;
    @Ignore
    Bitmap thumbnail;


    /**
     * The constructor of the image class that we can create the object of the image class
     * with these params
     *
     * @param id
     * @param title
     * @param description
     * @param date
     * @param imagepath
     * @param imageLength
     * @param imageWidth
     * @param locationId
     */
    public Image(int id, String title, String description,String date,
                 String imagepath,String imageLength,String imageWidth, int locationId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.imagepath=imagepath;
        this.imageLength=imageLength;
        this.imageWidth=imageWidth;
        this.locationId = locationId;

    }

    /**
     * The constructor of the image class that we can create the object of the image class
     * with these params
     *
     * @param context
     * @param title
     * @param description
     * @param date
     * @param imagepath
     * @param imageLength
     * @param imageWidth
     * @param offline
     * @param locationId
     */
    public Image(Context context, String title, String description,String date,
                 String imagepath,String imageLength,String imageWidth,String offline,int locationId) {
        this.title= title;
        this.description= description;
        this.date=date;
        this.imagepath=imagepath;
        this.imageLength=imageLength;
        this.imageWidth=imageWidth;
        this.offline=offline;
        this.locationId=locationId;
        this.id=idCounter++;

    }


    /**
     * Method gets the image id
     *
     * @return the image id
     */
    public int getId() {
        return id;
    }

    /**
     *  Method sets the image path
     *
     * @param imagepath
     */
    public void setImagepath(String imagepath){this.imagepath=imagepath;}

    /**
     *Method gets the image path
     *
     * @return the image path
     */
    public String getImagepath(){return imagepath;}

    /**
     * Method sets the image id
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * Method gets the image title
     *
     * @return the image title
     */
    public String getTitle() {
        return title;
    }


    /**
     * Method sets the image title
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Method gets the image description
     *
     * @return the image description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Method sets the image description
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Method gets the date of saving image
     *
     * @return the date of saving image
     */
    public String getDate(){return date;}

    /**
     * Method sets the date of saving image
     *
     * @param date
     */
    public void setDate(String date){this.date=date;}

    /**
     * Method gets location id
     *
     * @return location id
     */
    public int getLocationId() {
        return locationId;
    }

    /**
     * Method sets location id
     *
     * @param locationId
     */
    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    /**
     * Method gets the bitmap of the image
     *
     * @return picture
     */
    public Bitmap getPicture() {
        return picture;
    }

    /**
     * Method sets the bitmap of the image
     *
     * @param picture
     */
    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }


    /**
     * Method gets the image length
     *
     * @return the image length
     */
    public String getImageLength(){return imageLength;}

    /**
     * Method sets the image length
     *
     * @param imageLength
     */
    public void setImageLength(String imageLength){this.imageLength=imageLength;}

    /**
     * Method gets the image width
     *
     * @return the image width
     */
    public String getImageWidth(){return imageWidth;}

    /**
     * Method sets the image width
     *
     * @param imageWidth
     */
    public void setImageWidth(String imageWidth){this.imageWidth=imageWidth;}
}
