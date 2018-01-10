package com.example.zeyad.cameraapplication;

import java.io.File;
import java.io.Serializable;


/**
 *
 * Creating ImageElement class to use saving our images
 * in an image object list.
 *
 * This class includes all of the image details
 * and also getters and setter for these details.
 * Thus, we can use these information when we need.
 *
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
    private String imageLength;
    private String imageWidth;

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
    public String getImageLength(){return imageLength;}
    public String getImageWidth(){return imageWidth;}

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
    public void setImageLength(String imageLength){this.imageLength=imageLength;}
    public void setImageWidth(String imageWidth){this.imageWidth=imageWidth;}

}
