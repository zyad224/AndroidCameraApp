package com.example.zeyad.cameraapplication;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Zeyad on 12/19/2017.
 */

public class ImageElement implements Serializable {

    int image=-1;
    File file=null;

    private static final long serialVersionUID = 1L;
    private String title;
    private String description;
    private String date;
    private double longitude;
    private double latitude;
    private String imagePath;

    //drawable
    public ImageElement(int image) {
        this.image = image;

    }

    //file
    public ImageElement(File fileX) {
         file= fileX;
    }

    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public String getDate(){
        return date;
    }
    public double getLongitude(){return longitude;}
    public double getLatitude() {return  latitude;}
    public String getImagePath(){return imagePath;}

    public void setTitle(String title){
        this.title=title;
    }
    public void setLongitude(double longitude){this.longitude=longitude;}
    public void setLatitude(double latitude){this.latitude=latitude;}
    public void setDescription(String description){
        this.description=description;
    }
    public void setDate(String date){
        this.date=date;
    }
    public void setImagePath(String imagePath){this.imagePath=imagePath;}

}
