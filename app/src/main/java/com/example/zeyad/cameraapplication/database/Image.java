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

    @ColumnInfo(name = "location_id")
    public int locationId;

    @Ignore
    Bitmap picture;
    @Ignore
    Bitmap thumbnail;

    public Image(int id, String title, String description, int locationId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.locationId = locationId;
    }

    public Image(Context context, int drawable, String title, String description, int locationId) {
        this.title= title;
        this.description= description;

        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), drawable, null);
        this.picture= ((BitmapDrawable) vectorDrawable).getBitmap();
        // create the thumbnail yourself!
        //

        // assign the foreign ley id
        this.locationId=locationId;
        this.id=idCounter++;

    }


    public int getId() {
        return id;
    }

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
}