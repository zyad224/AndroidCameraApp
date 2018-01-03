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


    public int getId() {
        return id;
    }

    public void setImagepath(String imagepath){this.imagepath=imagepath;}
    public String getImagepath(){return imagepath;}

    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate(){return date;}

    public void setDate(String date){this.date=date;}

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImageLength(){return imageLength;}

    public void setImageLength(String imageLength){this.imageLength=imageLength;}

    public String getImageWidth(){return imageWidth;}

    public void setImageWidth(String imageWidth){this.imageWidth=imageWidth;}
}
